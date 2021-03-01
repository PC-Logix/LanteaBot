/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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

	@Override
	protected void initHook() {
		local_command = new Command("isup", new CommandArgumentParser(1, new CommandArgument("URL", ArgumentTypes.STRING))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String site = this.argumentParser.getArgument("URL");
				if (!site.startsWith("http://") && !site.startsWith("https://")) {
					Helper.sendMessage(target, "https is " + ((ping("https://" + site, 1000)) ? "UP" : "DOWN (Might be using untrusted certificates)"), nick);
					Helper.sendMessage(target, "http  is " + ((ping("http://" + site, 1000)) ? "UP" : "DOWN"), nick);
				} else {
					Helper.sendMessage(target, site + " is " + ((ping(site, 1000)) ? "UP" : "DOWN"));
				}
				return new CommandChainStateObject();
			}
		}; local_command.setHelpText("Checks if a website is up or down.");
		IRCBot.registerCommand(local_command);
	}
}
