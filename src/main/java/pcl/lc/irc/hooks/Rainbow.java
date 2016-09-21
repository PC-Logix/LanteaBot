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

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Rainbow extends AbstractListener {
	public String makeRainbow(String message) {

		Integer rainbow = 0;
		String messageout = "";
		for (int i = 0; i < message.length(); i++){
			char c = message.charAt(i);
			if (rainbow == 0) {
				messageout = messageout + Colors.RED + c;
			} else if (rainbow == 1) {
				messageout = messageout + Colors.OLIVE + c;
			} else if (rainbow == 2) {
				messageout = messageout + Colors.YELLOW + c;
			} else if (rainbow == 3) {
				messageout = messageout + Colors.GREEN + c;
			} else if (rainbow == 4) {
				messageout = messageout + Colors.BLUE + c;
			} else if (rainbow == 5) {
				messageout = messageout + Colors.DARK_BLUE + c;
			} else if (rainbow == 6) {
				messageout = messageout + Colors.MAGENTA + c;
			}
			rainbow++;
			if (rainbow >= 6) {
				rainbow = 0;
			}
		}
		return messageout;
	}

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("rainbow", "Replies with a rainbow version of the supplied text");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "rainbow")) {
			if (!IRCBot.isIgnored(nick)) {
				String s = event.getMessage().substring(event.getMessage().indexOf("rainbow") + 7).trim();
				event.respond(makeRainbow(s));
			}
		}
	}
}
