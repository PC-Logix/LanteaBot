/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Drama extends AbstractListener {
	@Override
	protected void initCommands() {
		IRCBot.registerCommand("drama", "Generates random drama using Mod Developers, Projects, and other semi random data.");
	}


	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			if (command.equals(prefix + "drama")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					URL obj = null;
					try {
						obj = new URL("http://pc-logix.com/drama.php?plain");
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
						con.setRequestMethod("GET");
						con.setRequestProperty("User-Agent", IRCBot.USER_AGENT);
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer response = new StringBuffer();
						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						in.close();
						event.getBot().sendIRC().message(event.getChannel().getName(), Helper.antiPing(sender) + ": " + response.toString());
					} catch (IOException ex) {
						event.respond("Server returned an error " + ex);
					}
				}
			}			
		}
	}


	@Override
	public void handleCommand(String nick, GenericMessageEvent event,
			String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
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
