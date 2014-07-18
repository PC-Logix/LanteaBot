/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
public class Rainbow extends ListenerAdapter {
	public Rainbow() {
		IRCBot.registerCommand("rainbow");
	}

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];

			if (triggerWord.equals(IRCBot.commandprefix + "rainbow")) {
				String s = event.getMessage().substring(event.getMessage().indexOf("rainbow") + 7).trim();
				event.respond(makeRainbow(s));
			}
		}
	}
}
