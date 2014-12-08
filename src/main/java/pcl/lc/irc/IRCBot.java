package pcl.lc.irc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.job.TaskScheduler;
import pcl.lc.irc.job.WikiChangeWatcher;
import pcl.lc.utils.CommentedProperties;
import pcl.lc.utils.TimedHashMap;

public class IRCBot {


	public static Logger getLog() {
		return IRCBot.log;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	//public static TimedHashMap messages = new TimedHashMap(600000, null );
	public static TreeMap<UUID, List<String>> messages = new TreeMap<UUID, List<String>>(Collections.reverseOrder());;
	public static HashMap<String, String> invites = new HashMap<String, String>();
	public static HashMap<String, String> users = new HashMap<String, String>();
	public static HashMap<String, String> authed = new HashMap<String,String>();
	public static HashMap<String, Integer> admins = new HashMap<String,Integer>();
	public static HashMap<String, Object> botConfig = new HashMap<String, Object>();
	public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
	public static String ournick = null;

	public static Logger log = Logger.getLogger("lanteabot");
	public static PircBotX bot;
	private TaskScheduler scheduler;

	public static String nick = null;
	public static String nspass = null;
	public static String nsaccount = null;
	public static String channels = null;
	public static String commandprefix = null;
	public static String httpdport = null;
	public static String enablehttpd = null;
	public static String proxyhost = null;
	public static String proxyport = null;
	public static String enableTLS = null;
	static String adminProps = null;
	@SuppressWarnings("rawtypes")
	public static Builder config = new Configuration.Builder();
	public static CommentedProperties prop = new CommentedProperties();

	public static httpd httpServer = new httpd();
	
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
			
	        File file = new File("config.properties");
	        if (!file.exists()) {
	            System.out.println("Config file missing, edit config.default, and rename to config.properties");
	            file.createNewFile();
	        }
			
			input = new FileInputStream(file);
			// load a properties file
			prop.load(input);
			botConfig.put("server", prop.getProperty("server", "irc.esper.net"));
			botConfig.put("serverport", prop.getProperty("serverport", "6667"));
			botConfig.put("serverpass", prop.getProperty("serverpass", ""));
			botConfig.put("WUndergroundAPI", prop.getProperty("WUndergroundAPI", ""));
			nick = prop.getProperty("nick","LanteaBot");
			nspass = prop.getProperty("nspass", "");
			nsaccount = prop.getProperty("nsaccount", "");
			channels = prop.getProperty("channels", "");
			commandprefix = prop.getProperty("commandprefix", "@");
			enablehttpd = prop.getProperty("enablehttpd", "true");
			httpdport = prop.getProperty("httpdport", "8081");
			botConfig.put("httpDocRoot", prop.getProperty("httpDocRoot", ""));
			botConfig.put("wikiWatcherURL", prop.getProperty("wikiWatcherURL", ""));
			proxyhost = prop.getProperty("proxyhost", "");
			proxyport = prop.getProperty("proxyport", "");
			adminProps = prop.getProperty("admins", "");
			enableTLS = prop.getProperty("enableTLS", "");
			saveProps();
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
		setConfig();	
		
		if (!proxyhost.isEmpty()) {
			System.setProperty("socksProxyHost",proxyhost);
			System.setProperty("socksProxyPort",proxyport);
		}
		
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
					log.fine(s);
					System.out.println(s);
			    }
			} else {
				config.addAutoJoinChannel(channels);
			}
		}
		
		if (!adminProps.isEmpty()) {
			String[] pairs = adminProps.split(",");
			for (int i=0;i<pairs.length;i++) {
			    String pair = pairs[i];
			    String[] keyValue = pair.split(":");
			    admins.put(keyValue[0], Integer.valueOf(keyValue[1]));
			}
		}
		
		config.addCapHandler(new EnableCapHandler("extended-join", true));
		config.addCapHandler(new EnableCapHandler("account-notify", true));
		if (enableTLS.equals("true"))
			config.addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true));

		config.setEncoding(Charset.forName("UTF-8"));
		config.setServer(botConfig.get("server").toString(), Integer.parseInt(botConfig.get("serverport").toString()), botConfig.get("serverpass").toString());

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
			if(!IRCBot.httpdport.isEmpty() && !botConfig.get("httpDocRoot").equals("")) {
				httpServer.start();
			}

			if(!botConfig.get("wikiWatcherURL").equals("")) {
				WikiChangeWatcher WikiChange = new WikiChangeWatcher();
				WikiChange.start();
			}

			scheduler = new TaskScheduler();
			scheduler.start();
			bot = new PircBotX(config.buildConfiguration());
			Thread.sleep(2000);
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
