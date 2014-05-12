package pcl.lc.irc;

import java.util.logging.Logger;

import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;

import pcl.lc.irc.hooks.IRCEventListener;
import pcl.lc.irc.job.TaskScheduler;

public class IRCBot {

	private static IRCBot INSTANCE;

	public static Logger getLog() {
		return IRCBot.INSTANCE.log;
	}

	private Logger log = Logger.getLogger("lanteabot");
	private PircBotX bot;
	private TaskScheduler scheduler;

	public IRCBot() {
		IRCBot.INSTANCE = this;
		Builder config = new Configuration.Builder();
		config.setName("lanteabot").setLogin("lb");
		config.setAutoNickChange(true);
		config.setCapEnabled(true);
		config.addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory()
				.trustAllCertificates(), true));
		config.addListener(new IRCEventListener());
		config.setServerHostname("irc.esper.net");
		config.addAutoJoinChannel("#lanteacraft");

		try {
			scheduler = new TaskScheduler();
			scheduler.start();
			bot = new PircBotX(config.buildConfiguration());
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
