/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class IsUp extends AbstractListener {

	public static boolean ping(String url, int timeout) {

		//url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		} catch (IOException exception) {
			return false;
		}
	}

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("isup", "Checks is a website is up");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix+ "isup")) {
			if (!IRCBot.isIgnored(nick)) {
				String site = copyOfRange[0].trim();
				boolean rez = ping(site, 1000);
				if (rez) {
					event.respond(site + " Is Up.");
				} else {
					event.respond(site + " Is Down.");
				}
			}
		}
	}
}
