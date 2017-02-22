import java.util.Arrays;

import pcl.lc.irc.IRCBot;

public class AppStub {

	public static void main(String[] args) {
		if (!Arrays.asList(args).contains("debug")) {
			System.setProperty("logback.configurationFile", "logback.xml");
		}
		new IRCBot();
		if (!Arrays.asList(args).contains("debug")) {
			IRCBot.setDebug(true);
		}
	}

}
