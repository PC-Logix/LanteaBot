package pcl.lc.irc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.wolfram.alpha.WAEngine;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.job.TaskScheduler;
import pcl.lc.irc.job.WikiChangeWatcher;

public class IRCBot {

	private Connection connection = Database.getConnection();
	public static IRCBot instance;
	public static boolean isDebug;
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

	public static String getOurNick() {
		return ournick;
	}
	
	public static HashMap<String, String> commands = new HashMap<String, String>();
	public static HashMap<String, String> helpList = new HashMap<String, String>();
	/**
	 * Registers a command for output in %help
	 * @param command
	 * @param help 
	 */
	public static void registerCommand(String command, String help) {
		if (!commands.containsKey(command)) {
			commands.put(command, Thread.currentThread().getStackTrace()[2].getClassName());
			helpList.put(command, help);	
			log.info("Registering Command: " + command);
		} else {
			log.error("Attempted to register duplicate command! Command: " + command + " Duplicating class: " + Thread.currentThread().getStackTrace()[2].getClassName() + " Owning class " + commands.get(command));
		}
	}
	/**
	 * Registers a command for output in %help, doesn't include any actual help
	 * @param command
	 */
	@Deprecated
	public static void registerCommand(String command) {
		if (!commands.containsKey(command)) {
			commands.put(command, Thread.currentThread().getStackTrace()[2].getClassName());
			log.info("Registering Command: " + command);
		} else {
			log.error("Attempted to register duplicate command! Command: " + command + " Duplicating class: " + Thread.currentThread().getStackTrace()[2].getClassName() + " Owning class " + commands.get(command));
		}
	}

	public static void unregisterCommand(String command) {
		if (commands.containsKey(command)) {
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
			log.error(e.getMessage());
			return;
		}
		try {
			if (!initDatabase()) {
				log.error("Database Failure!");
				return;
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(Config.httpdEnable.equals("true")) {
			try {
				httpd.setup();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		loadOps();
		loadChannels();
		try {
			if(!Config.botConfig.get("wikiWatcherURL").equals("")) {
				WikiChangeWatcher WikiChange = new WikiChangeWatcher();
				WikiChange.start();
			}
			if(Config.httpdEnable.equals("true")) {
				httpd.start();
			}
			scheduler = new TaskScheduler();
			scheduler.start();
			bot = new PircBotX(Config.config.buildConfiguration());
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Database.setDBVer(Database.DB_VER);
	}

	private boolean initDatabase() throws SQLException {
			Database.init();
			Database.addStatement("CREATE TABLE IF NOT EXISTS Channels(name)");
			Database.addStatement("CREATE TABLE IF NOT EXISTS Info(key PRIMARY KEY, data)");
			Database.addStatement("CREATE TABLE IF NOT EXISTS OptionalHooks(hook, channel)");
			//Channels
			Database.addPreparedStatement("addChannel", "REPLACE INTO Channels (name) VALUES (?);");
			Database.addPreparedStatement("removeChannel","DELETE FROM Channels WHERE name = ?;");
			//Hooks
			Database.addPreparedStatement("enableHook", "INSERT INTO OptionalHooks(hook, channel) VALUES (?, ?);");
			Database.addPreparedStatement("disableHook","DELETE FROM OptionalHooks WHERE hook = ? AND channel = ?;");
			Database.addPreparedStatement("checkHook","SELECT hook, channel FROM OptionalHooks WHERE hook = ?;");
			return true;
	}

	public void sendMessage(String target, String message) {
		bot.sendIRC().message(target, message);
	}

	private void loadOps() {
		try {
			ResultSet readOps = Database.getConnection().createStatement().executeQuery("SELECT name FROM ops;");
			int rowCount = 0;
			while (readOps.next()) {
				rowCount++;
				ops.add(readOps.getString("name"));
			}
			if (rowCount == 0) {
				log.info("Please enter the primary nickserv name of the first person with op privileges for the bot:\n> ");
				String op = scanner.nextLine();
				ops.add(op);
				Database.preparedStatements.get("addOp").setString(1, op);
				Database.preparedStatements.get("addOp").executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void loadChannels() {
		try {
			ResultSet readChannels = Database.getConnection().createStatement().executeQuery("SELECT name FROM channels;");
			int rowCount = 0;
			while (readChannels.next()) {
				rowCount++;
				log.info(readChannels.getString("name"));
				Config.config.addAutoJoinChannel(readChannels.getString("name"));
			}
			if (rowCount == 0) {
				log.info("Please enter the first channel the bot should join eg #channelname:\n> ");
				String channel = scanner.nextLine();
				Config.config.addAutoJoinChannel(channel);
				Database.preparedStatements.get("addChannel").setString(1, channel);
				Database.preparedStatements.get("addChannel").executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public PreparedStatement getPreparedStatement(String statement) throws Exception {
		return Database.getPreparedStatement(statement);
	}

	public static IRCBot getInstance() {
		return instance;
	}

	public List<String> getOps() {
		return ops;
	}

	public static void setDebug(boolean b) {
		isDebug = b;
	}

	@Deprecated
	public static boolean isOp(PircBotX sourceBot, User user) {
		return Permissions.isOp(sourceBot, user);
	}  
	
}
