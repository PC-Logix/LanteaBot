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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class Drama extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("drama", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					URL obj = null;
					try {
						obj = new URL("http://pc-logix.com/drama.php?plain");
					} catch (MalformedURLException e) {
						e.printStackTrace();
						Helper.sendMessage(target, "Something went wrong", nick);
						return;
					}
					try {
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
						con.setRequestMethod("GET");
						con.setRequestProperty("User-Agent", IRCBot.USER_AGENT);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuilder response = new StringBuilder();
						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						in.close();
						Helper.sendMessage(target, response.toString(), nick);
					} catch (IOException ex) {
						Helper.sendMessage(target, "Server returned an error " + ex, nick);
					} catch (Exception e) {
						e.printStackTrace();
						Helper.sendMessage(target, "Something went wrong", nick);
					}
				}
			}
		};
		local_command.setHelpText("Generates random drama using Mod Developers, Projects, and other semi random data.");
		IRCBot.registerCommand(local_command);
	}

	public String chan;
	public String target = null;

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
