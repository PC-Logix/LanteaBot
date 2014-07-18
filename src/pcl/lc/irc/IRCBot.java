package pcl.lc.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.cap.TLSCapHandler;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.reflections.Reflections;

import pcl.lc.irc.job.TaskScheduler;
import pcl.lc.utils.TimedHashMap;
import pcl.lc.httpd.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class IRCBot {

	private static IRCBot INSTANCE;

	public static Logger getLog() {
		return IRCBot.INSTANCE.log;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static TimedHashMap messages = new TimedHashMap(600000, null );
	public static HashMap<String, String> invites = new HashMap<String, String>();
	public static HashMap<String, String> users = new HashMap<String, String>();
	public static HashMap<String, String> authed = new HashMap<String,String>();
	public static HashMap<String, Integer> admins = new HashMap<String,Integer>();
	public final static String USER_AGENT = "Mozilla/5.0 (I lied this is Java)";
	public static String ournick = null;

	private Logger log = Logger.getLogger("lanteabot");
	public static PircBotX bot;
	private TaskScheduler scheduler;

	public static String server = null;
	public static String serverport = null;
	public static String serverpass = null;
	public static String nick = null;
	public static String nspass = null;
	public static String nsaccount = null;
	public static String channels = null;
	public static String commandprefix = null;
	public static String httpdport = null;
	public static String enablehttpd = null;
	public static String proxyhost = null;
	public static String proxyport = null;
	static String adminlist = null;
	@SuppressWarnings("rawtypes")
	public static Builder config = new Configuration.Builder();
	public static Properties prop = new Properties();

	public static void saveProps() {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream("config.properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			prop.store(output, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setConfig() {
		InputStream input = null;

		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);

			server = prop.getProperty("server", "irc.esper.net");
			serverport = prop.getProperty("serverport", "6667");
			serverpass = prop.getProperty("serverpass");
			nick = prop.getProperty("nick","LanteaBot");
			nspass = prop.getProperty("nspass");
			nsaccount = prop.getProperty("nsaccount");
			channels = prop.getProperty("channels");
			commandprefix = prop.getProperty("commandprefix", "@");
			enablehttpd = prop.getProperty("enablehttpd", "true");
			httpdport = prop.getProperty("httpdport", "8081");
			proxyhost = prop.getProperty("proxyhost");
			proxyport = prop.getProperty("proxyport");
			adminlist = prop.getProperty("admins");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ArrayList<String> commands = new ArrayList();
	public static void registerCommand(String command) {
		if (!commands.contains(command)) {
			commands.add(command);
			System.out.println("Registering Command: " + command);
		}
	}

	public void unregisterCommand(String command) {
		if (commands.contains(command)) {
			commands.remove(command);
			System.out.println("Removing Command: " + command);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IRCBot() {
		setConfig();	
		
		if (!proxyhost.isEmpty()) {
			System.setProperty("socksProxyHost",proxyhost);
			System.setProperty("socksProxyPort",proxyport);
		}
		
		IRCBot.INSTANCE = this;

		config.setName(nick).setLogin("lb");
		config.setAutoNickChange(true);
		config.setCapEnabled(true);
		config.setAutoReconnect(true);
		config.setAutoNickChange(true);
		if (!nspass.isEmpty())
			config.setNickservPassword(nspass);
		
		if (!channels.isEmpty()) {
			if (channels.contains(",")) {
				String[] joinChannels = channels.split(",");
				for (String s: joinChannels)
			    {
					config.addAutoJoinChannel(s);
					System.out.println(s);
			    }
			} else {
				config.addAutoJoinChannel(channels);
			}
		}
		
		if (!adminlist.isEmpty()) {
			if (adminlist.contains(",")) {
				for (String s: adminlist.split(","))
			    {
					admins.put(s, 1);
			    }
			} else {
				admins.put(adminlist, 1);
			}
		}
		
		config.addCapHandler(new EnableCapHandler("extended-join", true));
		config.addCapHandler(new EnableCapHandler("account-notify", true));
		config.addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true));

		config.setEncoding(Charset.forName("UTF-8"));
		config.setServer(server, Integer.parseInt(serverport), serverpass);

		//Load all classes in the pcl.lc.irc.hooks package.
		Reflections plugins = new Reflections("pcl.lc.irc.hooks");
		Set<Class<? extends ListenerAdapter>> allClasses = plugins.getSubTypesOf(ListenerAdapter.class);
		for (Class<? extends Object> s : allClasses) {
			try {
				config.addListener((Listener) s.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			httpd httpServer = new httpd();
			httpServer.start();
			scheduler = new TaskScheduler();
			scheduler.start();
			bot = new PircBotX(config.buildConfiguration());
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
