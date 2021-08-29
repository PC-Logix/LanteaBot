package pcl.lc.irc;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.cap.SASLCapHandler;
import org.pircbotx.cap.TLSCapHandler;
import pcl.lc.utils.CommentedProperties;
import pcl.lc.utils.GoogleSearch;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Config {

	public static HashMap<String, Object> botConfig = new HashMap<String, Object>();

	public static String nick = null;
	public static String nspass = null;
	public static String nsaccount = null;
	public static String channels = null;
	public static String ignoredUsersProp = null;
	public static String commandprefix = null;
	public static String httpdport = null;
	public static String httpdEnable = "false";
	public static String enablehttpd = null;
	public static String httpdBaseDomain = null;
	public static String proxyhost = null;
	public static String proxyport = null;
	public static String enableTLS = null;
	public static String enableSSL = null;
	public static String TwitCKey = null;
	public static String TwitCSecret = null;
	public static String TwitToken = null;
	public static String TwitTSecret = null;
	public static String googleAPI = null;
	public static String AzureTextAPI = null;
	static String adminProps = null;
	public static String weatherAPI = null;
	public static String yuriWebAPI = null;
	public static List<String> parseBridgeCommandsFromUsers = null;
	public static List<String> overBridgeUsernameBrackets = null;
	public static List<String> ignoreMessagesEndingWith = null;
	public static int maxNumberOfCommandsPerMessage = 2;

	public static String mysqlDbHost = null;
	public static String mysqlDbPort = null;
	public static String mysqlDbUser = null;
	public static String mysqlDbPass = null;
	public static String mysqlDbName = null;
	
	public static String botGender = null;
	@SuppressWarnings("rawtypes")
	public static Builder config = new Configuration.Builder();
	public static CommentedProperties prop = new CommentedProperties();

	public static void saveProps() {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream("config.properties");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			prop.store(output, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setConfig() {
		InputStream input = null;

		try {

			File file = new File("config.properties");
			if (!file.exists()) {
				file.createNewFile();
			}
		    final Version VERSION = new Version(
		            "@versionMajor@",
		            "@versionMinor@",
		            "@versionRevision@",
		            "@versionBuild@"
		    );
		    if (VERSION.getMajor() == -1) {
		    	IRCBot.setDebug(true);
		    }
			Config.config.setVersion("MichiBot Build# " + VERSION);
			input = new FileInputStream(file);
			// load a properties file
			prop.load(input);
			botConfig.put("server", prop.getProperty("server", "irc.esper.net"));
			botConfig.put("serverport", prop.getProperty("serverport", "6667"));
			botConfig.put("serverpass", prop.getProperty("serverpass", ""));
			botConfig.put("WeatherAPI", prop.getProperty("WeatherAPI", ""));
			botConfig.put("GoogleAPI", prop.getProperty("GoogleAPI", ""));
			botConfig.put("AzureTextAPI", prop.getProperty("AzureTextAPI", ""));
			botConfig.put("WolframAPI", prop.getProperty("WolframAPI", ""));
			nick = prop.getProperty("nick","LanteaBot");
			nspass = prop.getProperty("nspass", "");
			nsaccount = prop.getProperty("nsaccount", "");
			ignoredUsersProp = prop.getProperty("ignoredUsers", "");
			commandprefix = prop.getProperty("commandprefix", "@");
			enablehttpd = prop.getProperty("enablehttpd", "true");
			httpdport = prop.getProperty("httpdport", "8081");
			httpdEnable = prop.getProperty("httpdEnable", "false");
			httpdBaseDomain = prop.getProperty("httpdBaseDomain", "http://localhost");
			botConfig.put("httpDocRoot", prop.getProperty("httpDocRoot", ""));
			botConfig.put("wikiWatcherURL", prop.getProperty("wikiWatcherURL", ""));
			proxyhost = prop.getProperty("proxyhost", "");
			proxyport = prop.getProperty("proxyport", "");
			adminProps = prop.getProperty("admins", "");
			enableTLS = prop.getProperty("enableTLS", "false");
			enableSSL = prop.getProperty("enableSSL", "false");
			TwitCKey = prop.getProperty("TwitCKey");
			TwitCSecret = prop.getProperty("TwitCSecret");
			TwitToken = prop.getProperty("TwitToken");
			TwitTSecret = prop.getProperty("TwitTSecret");
			googleAPI = prop.getProperty("GoogleAPI", "");
			AzureTextAPI = prop.getProperty("AzureTextAPI", "");
			weatherAPI = prop.getProperty("WeatherAPI", "");
			yuriWebAPI = prop.getProperty("yuriWebAPI", "");
			parseBridgeCommandsFromUsers = Arrays.asList(prop.getProperty("parseBridgeCommandsFromUsers", "").split(","));
			overBridgeUsernameBrackets = Arrays.asList(prop.getProperty("overBridgeUsernameBrackets", "<>,()").split(","));
			ignoreMessagesEndingWith = Arrays.asList(prop.getProperty("ignoreMessagesEndingWith", "%*%").split(","));
			try {
				maxNumberOfCommandsPerMessage = Integer.parseInt(prop.getProperty("maxNumberOfCommandsPerMessage", "2"));
			} catch (Exception ignored) {}

			mysqlDbHost = prop.getProperty("mysqlDbHost", "");
			mysqlDbPort = prop.getProperty("mysqlDbPort", "");
			mysqlDbUser = prop.getProperty("mysqlDbUser", "");
			mysqlDbPass = prop.getProperty("mysqlDbPass", "");
			mysqlDbName = prop.getProperty("mysqlDbName", "");
			
			botGender = prop.getProperty("botGender", "genderless");

			saveProps();


			if (!Config.proxyhost.isEmpty()) {
				System.setProperty("socksProxyHost",Config.proxyhost);
				System.setProperty("socksProxyPort",Config.proxyport);
			}

			Config.config.setRealName(Config.nick).setName(Config.nick).setLogin(Config.nick);
			Config.config.setAutoNickChange(true);
			Config.config.setCapEnabled(true);
			Config.config.setAutoReconnect(true);
			Config.config.setAutoNickChange(true);
			Config.config.setAutoSplitMessage(true);
			if (!Config.nspass.isEmpty()){
				Config.config.setCapEnabled(true);
				Config.config.addCapHandler(new SASLCapHandler(Config.nsaccount, Config.nspass));
				Config.config.setNickservPassword(Config.nspass);
			}

			if (!Config.googleAPI.isEmpty())
				GoogleSearch.setup(Config.googleAPI);

			Config.config.addCapHandler(new EnableCapHandler("extended-join", true));
			Config.config.addCapHandler(new EnableCapHandler("account-notify", true));
			Config.config.setEncoding(Charset.forName("UTF-8"));
			if (Config.enableTLS.equals("true")) {
				Config.config.addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true));
			} 
			Config.config.setSnapshotsEnabled(true);
			if (Config.enableSSL.equals("true")) {
				Config.config.addServer(Config.botConfig.get("server").toString(), Integer.parseInt(Config.botConfig.get("serverport").toString()))
			    .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates()).setServerPassword(Config.botConfig.get("serverpass").toString());
			} else {
				Config.config.addServer(Config.botConfig.get("server").toString(), Integer.parseInt(Config.botConfig.get("serverport").toString()))
			    .setServerPassword(Config.botConfig.get("serverpass").toString());
			}

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

	public static String getTwitCKey() {
		return TwitCKey;
	}

	public static String getTwitCSecret() {
		return TwitCSecret;
	}

	public static String getTwitToken() {
		return TwitToken;
	}

	public static String getTwitTSecret() {
		return TwitTSecret;
	}


}
