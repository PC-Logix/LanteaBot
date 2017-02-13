package pcl.lc.irc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.WhoisEvent;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.wolfram.alpha.WAEngine;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.job.TaskScheduler;
import pcl.lc.irc.job.WikiChangeWatcher;

public class IRCBot {

	private Connection connection = null;
	private final Map<String, PreparedStatement> preparedStatements = new HashMap<>();
	//public static Logger getLog() {
	//	return IRCBot.log;
	//}
	public static IRCBot instance;

	//public static TimedHashMap messages = new TimedHashMap(600000, null );
	private static final int MAX_MESSAGES = 150;
	public static LinkedHashMap<UUID, List<String>> messages = new LinkedHashMap<UUID, List<String>>(MAX_MESSAGES + 1, .75F, false) {
		private static final long serialVersionUID = 3558133365599892107L;
		@SuppressWarnings("rawtypes")
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > MAX_MESSAGES;
		}
	};
	public static WAEngine engine = new WAEngine();
	public static HashMap<String, String> invites = new HashMap<String, String>();
	public static HashMap<String, String> users = new HashMap<String, String>();
	public static HashMap<String, String> authed = new HashMap<String,String>();
	public static HashMap<String, Integer> admins = new HashMap<String,Integer>();
	private final List<String> ops = new ArrayList<>();
	public static ArrayList<String> ignoredUsers = new ArrayList<String>();
	public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	public static String ournick = null;
	private final Scanner scanner;
	public static Map<UUID,ExpiringToken> userCache = new HashMap<>();

	//public static Logger log = Logger.getLogger("lanteabot");
	public static final Logger log = LoggerFactory.getLogger(IRCBot.class);

	public static PircBotX bot;
	private TaskScheduler scheduler;



	public static httpd httpServer = new httpd();



	public static boolean isIgnored(String nick) {
		if (IRCBot.admins.containsKey(nick)) {
			return false;
		} else if (authed.containsKey(nick) && ignoredUsers.contains(nick)) {
			return true;
		} else if (ignoredUsers.contains(nick)){
			return true;
		} else {
			return false;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ArrayList<String> commands = new ArrayList();
	public static HashMap<String, String> helpList = new HashMap<String, String>();
	public static void registerCommand(String command, String help) {
		if (!commands.contains(command)) {
			commands.add(command);
			helpList.put(command, help);	
			log.info("Registering Command: " + command);
		}
	}

	public static void registerCommand(String command) {
		if (!commands.contains(command)) {
			commands.add(command);
			log.info("Registering Command: " + command);
		}
	}

	public static void unregisterCommand(String command) {
		if (commands.contains(command)) {
			commands.remove(command);
			log.info("Removing Command: " + command);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IRCBot() {
		scanner = new Scanner(System.in);
		instance = this;		
		Config.setConfig();	
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		if (!initDatabase()) {
			System.err.println("Database Failure!");
			return;
		}

		loadOps();
		loadChannels();
		//Load all classes in the pcl.lc.irc.hooks package.
		Reflections plugins = new Reflections("pcl.lc.irc.hooks");
		Set<Class<? extends ListenerAdapter>> allClasses = plugins.getSubTypesOf(ListenerAdapter.class);
		for (Class<? extends Object> s : allClasses) {
			try {
				log.info("[DEPRECIATED] Loading " + s.getCanonicalName());
				Config.config.addListener((Listener) s.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Reflections plugins2 = new Reflections("pcl.lc.irc.hooks");
		Set<Class<? extends AbstractListener>> allClasses2 = plugins2.getSubTypesOf(AbstractListener.class);
		for (Class<? extends Object> s : allClasses2) {
			try {
				log.info("Loading " + s.getCanonicalName());
				Config.config.addListener((Listener) s.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			if(!Config.httpdport.isEmpty()) {
				httpServer.start();
			}

			if(!Config.botConfig.get("wikiWatcherURL").equals("")) {
				WikiChangeWatcher WikiChange = new WikiChangeWatcher();
				WikiChange.start();
			}

			scheduler = new TaskScheduler();
			scheduler.start();
			bot = new PircBotX(Config.config.buildConfiguration());
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean initDatabase() {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:michibot.db");
			Statement statement = connection.createStatement();
			statement.setPoolable(true);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Channels(name)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Ops(name, level)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Tells(id, sender, rcpt, channel, message, time)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Info(key PRIMARY KEY, data)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Quotes(id INTEGER PRIMARY KEY, user, data)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS LastSeen(user PRIMARY KEY, timestamp)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS OptionalHooks(hook, channel)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS IgnoredUers(nick)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Commands(command STRING UNIQUE PRIMARY KEY, return)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS InternetPoints(nick STRING UNIQUE PRIMARY KEY, points)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Announcements(channel, schedule, title, message)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Reminders(dest, nick, time, message)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Inventory(id INTEGER PRIMARY KEY, item_name, uses_left INTEGER)");
			preparedStatements.put("addChannel", connection.prepareStatement("REPLACE INTO Channels (name) VALUES (?);"));
			preparedStatements.put("removeChannel",connection.prepareStatement("DELETE FROM Channels WHERE name = ?;"));
			preparedStatements.put("enableHook", connection.prepareStatement("INSERT INTO OptionalHooks(hook, channel) VALUES (?, ?);"));
			preparedStatements.put("disableHook",connection.prepareStatement("DELETE FROM OptionalHooks WHERE hook = ? AND channel = ?;"));
			preparedStatements.put("checkHook",connection.prepareStatement("SELECT hook, channel FROM OptionalHooks WHERE hook = ?;"));
			preparedStatements.put("addOp",connection.prepareStatement("REPLACE INTO Ops (name) VALUES (?);"));
			preparedStatements.put("removeOp",connection.prepareStatement("DELETE FROM Ops WHERE name = ?;"));
			preparedStatements.put("addQuote",connection.prepareStatement("INSERT INTO Quotes(id, user, data) VALUES (NULL, ?, ?);", Statement.RETURN_GENERATED_KEYS));
			preparedStatements.put("getUserQuote",connection.prepareStatement("SELECT id, data FROM Quotes WHERE LOWER(user) = ? ORDER BY RANDOM () LIMIT 1;"));
			preparedStatements.put("getIdQuote",connection.prepareStatement("SELECT user, data FROM Quotes WHERE id = ? LIMIT 1;"));
			preparedStatements.put("getUserQuoteAll",connection.prepareStatement("SELECT id, data FROM Quotes WHERE LOWER(user) = ?;"));
			preparedStatements.put("getAnyQuote",connection.prepareStatement("SELECT id, user, data FROM Quotes ORDER BY RANDOM () LIMIT 1;"));
			preparedStatements.put("getAllQuotes",connection.prepareStatement("SELECT id, user, data FROM Quotes;"));
			preparedStatements.put("getSpecificQuote",connection.prepareStatement("SELECT id, data FROM Quotes WHERE user = ? AND data = ?;"));
			preparedStatements.put("removeQuote",connection.prepareStatement("DELETE FROM Quotes WHERE id = ?;"));
			preparedStatements.put("updateLastSeen",connection.prepareStatement("REPLACE INTO LastSeen(user, timestamp) VALUES (?, ?);"));
			preparedStatements.put("getLastSeen",connection.prepareStatement("SELECT timestamp FROM LastSeen WHERE LOWER(user) = ? GROUP BY LOWER(user) ORDER BY timestamp desc"));
			preparedStatements.put("updateInfo",connection.prepareStatement("REPLACE INTO Info(key, data) VALUES (?, ?);"));
			preparedStatements.put("getInfo",connection.prepareStatement("SELECT data FROM Info WHERE key = ?;"));
			preparedStatements.put("getInfoAll",connection.prepareStatement("SELECT key, data FROM Info;"));
			preparedStatements.put("removeInfo",connection.prepareStatement("DELETE FROM Info WHERE key = ?;"));
			preparedStatements.put("addTell",connection.prepareStatement("INSERT INTO Tells(sender, rcpt, channel, message) VALUES (?, ?, ?, ?);"));
			preparedStatements.put("getTells",connection.prepareStatement("SELECT rowid, sender, channel, message FROM Tells WHERE LOWER(rcpt) = ?;"));
			preparedStatements.put("removeTells",connection.prepareStatement("DELETE FROM Tells WHERE LOWER(rcpt) = ?;"));
			preparedStatements.put("getPoints", connection.prepareStatement("SELECT Points FROM InternetPoints WHERE nick = ?;"));
			preparedStatements.put("addPoints", connection.prepareStatement("INSERT OR REPLACE INTO InternetPoints VALUES (?, ?)"));
			preparedStatements.put("addCommand", connection.prepareStatement("INSERT INTO Commands(command, return) VALUES (?, ?);"));
			preparedStatements.put("searchCommands", connection.prepareStatement("SELECT command FROM Commands"));
			preparedStatements.put("getCommand", connection.prepareStatement("SELECT return FROM Commands WHERE command = ?"));
			preparedStatements.put("delCommand",connection.prepareStatement("DELETE FROM Commands WHERE command = ?;"));
			preparedStatements.put("addAnnounce", connection.prepareStatement("INSERT INTO Announcements(channel, schedule, message) VALUES (?,?,?);"));
			preparedStatements.put("getAnnounce", connection.prepareStatement("SELECT schedule, title, message FROM Announcements WHERE channel = ?;"));
			preparedStatements.put("delAnnounce", connection.prepareStatement("DELETE FROM Announcements WHERE title = ? AND channel = ?;"));
			preparedStatements.put("addReminder", connection.prepareStatement("INSERT INTO Reminders(dest, nick, time, message) VALUES (?,?,?,?);"));
			preparedStatements.put("getReminder", connection.prepareStatement("SELECT dest, nick, time, message FROM Reminders WHERE time <= ?;"));
			preparedStatements.put("listReminders", connection.prepareStatement("SELECT dest, nick, time, message FROM Reminders WHERE nick = ?;"));
			preparedStatements.put("delReminder", connection.prepareStatement("DELETE FROM Reminders WHERE time = ? AND nick = ?;"));
			preparedStatements.put("getItems", connection.prepareStatement("SELECT id, item_name, uses_left FROM Inventory;"));
			preparedStatements.put("getItem", connection.prepareStatement("SELECT id, item_name, uses_left FROM Inventory WHERE id = ?;"));
			preparedStatements.put("getRandomItem", connection.prepareStatement("SELECT id, item_name, uses_left FROM Inventory ORDER BY Random() LIMIT 1"));
			preparedStatements.put("addItem", connection.prepareStatement("INSERT INTO Inventory (id, item_name) VALUES (NULL, ?)"));
			preparedStatements.put("removeItemId", connection.prepareStatement("DELETE FROM Inventory WHERE id = ?"));
			preparedStatements.put("removeItemName", connection.prepareStatement("DELETE FROM Inventory WHERE item_name = ?"));
			preparedStatements.put("decrementUses", connection.prepareStatement("UPDATE Inventory SET uses_left = uses_left - 1 WHERE id = ?"));

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void sendMessage(String target, String message) {
		bot.sendIRC().message(target, message);
	}

	private void loadOps() {
		try {
			ResultSet readOps = connection.createStatement().executeQuery("SELECT name FROM ops;");
			int rowCount = 0;
			while (readOps.next()) {
				rowCount++;
				ops.add(readOps.getString("name"));
			}
			if (rowCount == 0) {
				System.out.print("Please enter the primary nickserv name of the first person with op privileges for the bot:\n> ");
				String op = scanner.nextLine();
				ops.add(op);
				preparedStatements.get("addOp").setString(1, op);
				preparedStatements.get("addOp").executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void loadChannels() {
		try {
			ResultSet readChannels = connection.createStatement().executeQuery("SELECT name FROM channels;");
			int rowCount = 0;
			while (readChannels.next()) {
				rowCount++;
				log.info(readChannels.getString("name"));
				Config.config.addAutoJoinChannel(readChannels.getString("name"));
			}
			if (rowCount == 0) {
				System.out.print("Please enter the first channel the bot should join eg #channelname:\n> ");
				String channel = scanner.nextLine();
				Config.config.addAutoJoinChannel(channel);
				preparedStatements.get("addChannel").setString(1, channel);
				preparedStatements.get("addChannel").executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public PreparedStatement getPreparedStatement(String statement) throws Exception {
		if (!preparedStatements.containsKey(statement)) {
			throw new Exception("Invalid statement!");
		}
		return preparedStatements.get(statement);
	}

	public static IRCBot getInstance() {
		return instance;
	}

	public List<String> getOps() {
		return ops;
	}

	@SuppressWarnings("rawtypes")
	public boolean isOp(PircBotX sourceBot, User user) {
		String nsRegistration = "";
		if (userCache.containsKey(user.getUserId()) && userCache.get(user.getUserId()).getExpiration().after(Calendar.getInstance().getTime())) {
			nsRegistration = userCache.get(user.getUserId()).getValue();
			System.out.println(user.getNick() + " is cached");
		} else {
			System.out.println(user.getNick() + " is NOT cached");
			user.isVerified();
			try {
				sourceBot.sendRaw().rawLine("WHOIS " + user.getNick() + " " + user.getNick());
				WaitForQueue waitForQueue = new WaitForQueue(sourceBot);
				WhoisEvent whoisEvent = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				if (whoisEvent.getRegisteredAs() != null) {
					nsRegistration = whoisEvent.getRegisteredAs();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!nsRegistration.isEmpty()) {
				Calendar future = Calendar.getInstance();
				future.add(Calendar.MINUTE,5);
				userCache.put(user.getUserId(), new ExpiringToken(future.getTime(),nsRegistration));
				System.out.println(user.getUserId().toString() + " added to cache: " + nsRegistration + " expires at " + future.toString());
			}
		}
		if (getOps().contains(nsRegistration)) {
			return true;
		} else {
			return false;
		}
	}  

	private class ExpiringToken
	{
		private final Date expiration;
		private final String value;

		private ExpiringToken(Date expiration, String value) {
			this.expiration = expiration;
			this.value = value;
		}

		public Date getExpiration() {
			return expiration;
		}

		public String getValue() {
			return value;
		}

	}
}
