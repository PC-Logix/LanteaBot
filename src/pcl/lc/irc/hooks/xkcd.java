package pcl.lc.irc.hooks;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class xkcd extends ListenerAdapter {
	public xkcd() {
		IRCBot.registerCommand("xkcd");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "xkcd")) {
				URLConnection con = new URL( "http://dynamic.xkcd.com/random/comic/" ).openConnection();
				con.connect();
				InputStream is = con.getInputStream();
				String newurl = con.getURL().toString();
				is.close();
				event.respond("Random XKCD Comic: " + newurl);
			}
		}
	}
}