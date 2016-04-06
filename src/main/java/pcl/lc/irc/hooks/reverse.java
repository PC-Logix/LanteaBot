/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class reverse extends ListenerAdapter {
	public reverse() {
		IRCBot.registerCommand("reverse", "Reverses the supplied text");
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
			if (triggerWord.equals(prefix + "reverse")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					String s = event.getMessage().substring(event.getMessage().indexOf("reverse") + 7).trim();
					event.respond(new StringBuffer(Colors.removeFormattingAndColors(s)).reverse().toString());
				}
			}			
		}
	}
}