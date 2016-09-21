/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
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
public class reverse extends AbstractListener {

	private String chan;

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("reverse", "Reverses the supplied text");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "reverse")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "reverse")) {
			String target;
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
			String s = event.getMessage().substring(event.getMessage().indexOf("reverse") + 7).trim();
			IRCBot.getInstance().sendMessage(target,  Helper.antiPing(nick) + ": " + new StringBuffer(Colors.removeFormattingAndColors(s)).reverse().toString());
		}
	}
}