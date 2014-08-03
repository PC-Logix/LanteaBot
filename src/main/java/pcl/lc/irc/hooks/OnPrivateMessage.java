/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings({ "rawtypes" })
public class OnPrivateMessage extends ListenerAdapter {
	public OnPrivateMessage() {
		IRCBot.registerCommand("xkcd");
	}

	@Override
	public void onPrivateMessage(final PrivateMessageEvent event) throws Exception {
		if (event.getUser().equals("NickServ")) {
			if (!IRCBot.nspass.isEmpty())
				event.respond("ns identify " + IRCBot.nsaccount + " " + IRCBot.nspass);
		}
	}
}
