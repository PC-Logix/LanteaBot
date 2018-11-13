package pcl.lc.irc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.*;
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
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;
import pcl.lc.utils.InputThread;

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
	
	//Keep a list of invites recieved
	public static HashMap<String, String> invites = new HashMap<String, String>();
	//Keep a list of users, and what server they're connected from
	public static HashMap<String, String> users = new HashMap<String, String>();
	//Keep a list of authed users, this list is cleared on a timer set in Permissions.java
	public static HashMap<String, String> authed = new HashMap<String,String>();
	//List of bot admins
	public static HashMap<String, Integer> admins = new HashMap<String,Integer>();
	private final List<String> ops = new ArrayList<>();
	//List of ignored users
	public static ArrayList<String> ignoredUsers = new ArrayList<String>();
	public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	public static String ournick = null;
	private final Scanner scanner;

	public static final Logger log = LoggerFactory.getLogger(IRCBot.class);

	public static PircBotX bot;
	private TaskScheduler scheduler;

	public static String getDiscordID(String nick) {
		URL url;
		try {
			url = new URL("eos.pc-logix.com:9791/" + nick);
			Scanner s = new Scanner(url.openStream());
			return s.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nick;
	}
	
	public static httpd httpServer = new httpd();
	public static boolean isIgnored(String nick) {
		if (IRCBot.admins.containsKey(nick)) {
			return false;
		} else if (ignoredUsers.contains(nick)){
			return true;
		} else if (ignoredUsers.contains(getDiscordID(nick))) {
			return true;
		} else {
			return false;
		}
	}

	public static String getOurNick() {
		return ournick;
	}
	
	public static HashMap<String, Command> commands = new LinkedHashMap<>();
	public static HashMap<String, String> helpList = new LinkedHashMap<String, String>();

	public static void registerCommand(Command command) {
		registerCommand(command, command.getHelpText());
	}

	public static void registerCommand(Command command, String help) {
		if (!commands.containsKey(command.getCommand())) {
			commands.put(command.getCommand(), command);
			helpList.put(command.getCommand(), help);
			log.info("Registering Command: " + command.getCommand());
		} else {
			log.error("Attempted to register duplicate command! Command: " + command.getCommand() + " Duplicating class: " + command.getClassName() + " Owning class " + commands.get(command.getCommand()).getClassName());
		}
	}

	public static void setHelp(String command, String help) {
		if (helpList.containsKey(command)) {
			helpList.put(command, help);
		} else {
			log.error("Attempted to set help on non existent command");
		}
	}
	
	public static void registerCommand(String command, String help) {
		registerCommand(command, help, 0);
	}

	/**
	 * Registers a command for output in %help
	 * @param command String
	 * @param help String
	 * @param rateLimit Integer
	 */
	public static void registerCommand(String command, String help, Integer rateLimit) {
		if (!commands.containsKey(command)) {
			commands.put(command, new Command(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, false, true, null));
			helpList.put(command, help);	
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
		Helper.init();
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
				httpd.setBaseDomain(Config.httpdBaseDomain);
				
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
		//Database.setDBVer(Database.DB_VER);
		Database.updateDatabase();
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

			InputThread input = new InputThread();
			input.start();
			
			bot = new PircBotX(Config.config.buildConfiguration());
			bot.startBot();		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
			Database.addPreparedStatement("checkHookForChan","SELECT hook FROM OptionalHooks WHERE hook = ? AND channel = ?;");
			return true;
	}

	@Deprecated
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

	/**
	 * use Database.getPreparedStatement
	 * @param statement
	 * @return
	 * @throws Exception
	 */
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
	
	public static boolean getDebug() {
		return isDebug;
	}

	/**
	 * Use Premissions.isOp
	 * @param sourceBot
	 * @param user
	 * @return
	 */
	@Deprecated
	public static boolean isOp(PircBotX sourceBot, User user) {
		return Permissions.isOp(sourceBot, user);
	}

    public static File getThisJarFile() throws UnsupportedEncodingException
    {
      //Gets the path of the currently running Jar file
        String path = IRCBot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");

        //This is code especially written for running and testing this program in an IDE that doesn't compile to .jar when running.
        if (!decodedPath.endsWith(".jar"))
        {
            return new File("LanteaBot.jar");
        }
        return new File(decodedPath);   //We use File so that when we send the path to the ProcessBuilder, we will be using the proper System path formatting.
    }
	
}
