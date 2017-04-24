/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class IsUp extends AbstractListener {
	private Command local_command;

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

	private String chan;

	@Override
	protected void initHook() {
		local_command = new Command("isup", 0);
		IRCBot.registerCommand(local_command, "Checks is a website is up");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (local_command.shouldExecuteBool(command, event)) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (local_command.shouldExecuteBool(command, event)) {
			String target;
			String dest = null;
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
			String site = copyOfRange[0].trim();
			boolean rez = ping(site, 1000);
			if (rez) {
				Helper.sendMessage(target, site + " Is Up.", nick);
			} else {
				Helper.sendMessage(target, site + " Is Down.", nick);
			}
		}
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}
