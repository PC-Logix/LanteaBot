import pcl.lc.irc.IRCBot;

public class AppStub {

	public static void main(String[] args) {
		System.setProperty("logback.configurationFile", "logback.xml");
		new IRCBot();
	}

}
