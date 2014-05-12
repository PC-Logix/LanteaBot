package pcl.lc.irc;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;

public class IRCBot {

	private PircBotX bot;
	private Configuration config;

	public IRCBot() {
		// Setup this bot
		config = new Configuration.Builder()
				.setName("lanteabot")
				.setLogin("lb")
				.setAutoNickChange(true)
				.setCapEnabled(true)
				.addCapHandler(
						new TLSCapHandler(new UtilSSLSocketFactory()
								.trustAllCertificates(), true))
				.addListener(new IRCEventListener())
				.setServerHostname("irc.esper.net")
				.addAutoJoinChannel("#lanteacraft").buildConfiguration();
		try {
			bot = new PircBotX(config);
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
