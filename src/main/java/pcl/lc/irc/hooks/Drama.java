/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Drama extends ListenerAdapter {
	public Drama() {
		IRCBot.registerCommand("drama", "Generates random drama using Mod Developers, Projects, and other semi random data.");
	}


	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "drama")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					URL obj = new URL("http://pc-logix.com/drama.php?plain");
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
						event.respond(response.toString());
					} catch (IOException ex) {
						event.respond("Server returned an error " + ex);
					}
				}
			}			
		}
	}
}
