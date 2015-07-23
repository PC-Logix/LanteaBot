package pcl.lc.irc;

import java.io.IOException;
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
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.reflections.Reflections;

import com.wolfram.alpha.WAEngine;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.job.TaskScheduler;
import pcl.lc.irc.job.WikiChangeWatcher;

public class IRCBot {

	private Connection connection = null;
	private final Map<String, PreparedStatement> preparedStatements = new HashMap<>();
	public static Logger getLog() {
		return IRCBot.log;
	}
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

	public static ArrayList<String> ignoredUsers = new ArrayList<String>();
	public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	public static String ournick = null;

	public static Logger log = Logger.getLogger("lanteabot");
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
	public static void registerCommand(String command) {
		if (!commands.contains(command)) {
			commands.add(command);
			log.fine("Registering Command: " + command);
			System.out.println("Registering Command: " + command);
		}
	}

	public void unregisterCommand(String command) {
		if (commands.contains(command)) {
			commands.remove(command);
			log.fine("Removing Command: " + command);
			System.out.println("Removing Command: " + command);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IRCBot() {
		
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
        
		//Load all classes in the pcl.lc.irc.hooks package.
		Reflections plugins = new Reflections("pcl.lc.irc.hooks");
		Set<Class<? extends ListenerAdapter>> allClasses = plugins.getSubTypesOf(ListenerAdapter.class);
		for (Class<? extends Object> s : allClasses) {
			try {
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
			if(!Config.httpdport.isEmpty() && !Config.botConfig.get("httpDocRoot").equals("")) {
				httpServer.start();
			}

			if(!Config.botConfig.get("wikiWatcherURL").equals("")) {
				WikiChangeWatcher WikiChange = new WikiChangeWatcher();
				WikiChange.start();
			}

			scheduler = new TaskScheduler();
			scheduler.start();
			bot = new PircBotX(Config.config.buildConfiguration());
			Thread.sleep(2000);
			//bot.setInetAddress(InetAddress.getByName("206.255.162.30"));
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
    private boolean initDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:michibot.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Channels(name)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Ops(name)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Tells(sender, rcpt, channel, message)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Info(key PRIMARY KEY, data)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Quotes(user, data)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS LastSeen(user PRIMARY KEY, timestamp)");
            preparedStatements.put("addChannel", connection.prepareStatement("REPLACE INTO Channels (name) VALUES (?);"));
            preparedStatements.put("removeChannel",connection.prepareStatement("DELETE FROM Channels WHERE name = ?;"));
            preparedStatements.put("addOp",connection.prepareStatement("REPLACE INTO Ops (name) VALUES (?);"));
            preparedStatements.put("removeOp",connection.prepareStatement("DELETE FROM Ops WHERE name = ?;"));
            preparedStatements.put("addQuote",connection.prepareStatement("INSERT INTO Quotes(user, data) VALUES (?, ?);"));
            preparedStatements.put("getUserQuote",connection.prepareStatement("SELECT data FROM Quotes WHERE user = ? ORDER BY RANDOM () LIMIT 1;"));
            preparedStatements.put("getUserQuoteAll",connection.prepareStatement("SELECT data FROM Quotes WHERE user = ?;"));
            preparedStatements.put("getAnyQuote",connection.prepareStatement("SELECT user, data FROM Quotes ORDER BY RANDOM () LIMIT 1;"));
            preparedStatements.put("getSpecificQuote",connection.prepareStatement("SELECT data FROM Quotes WHERE user = ? AND data = ?;"));
            preparedStatements.put("removeQuote",connection.prepareStatement("DELETE FROM Quotes WHERE user = ? AND data = ?;"));
            preparedStatements.put("updateLastSeen",connection.prepareStatement("REPLACE INTO LastSeen(user, timestamp) VALUES (?, ?);"));
            preparedStatements.put("getLastSeen",connection.prepareStatement("SELECT timestamp FROM LastSeen WHERE user = ?;"));
            preparedStatements.put("updateInfo",connection.prepareStatement("REPLACE INTO Info(key, data) VALUES (?, ?);"));
            preparedStatements.put("getInfo",connection.prepareStatement("SELECT data FROM Info WHERE key = ?;"));
            preparedStatements.put("getInfoAll",connection.prepareStatement("SELECT key, data FROM Info;"));
            preparedStatements.put("removeInfo",connection.prepareStatement("DELETE FROM Info WHERE key = ?;"));
            preparedStatements.put("addTell",connection.prepareStatement("INSERT INTO Tells(sender, rcpt, channel, message) VALUES (?, ?, ?, ?);"));
            preparedStatements.put("getTells",connection.prepareStatement("SELECT rowid, sender, channel, message FROM Tells WHERE rcpt = ?;"));
            preparedStatements.put("removeTells",connection.prepareStatement("DELETE FROM Tells WHERE rcpt = ?;"));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String target, String message) {
        bot.sendIRC().message(target, message);
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
}
