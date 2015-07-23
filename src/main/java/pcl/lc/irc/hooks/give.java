/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class give extends ListenerAdapter {
	public give() {
		IRCBot.registerCommand("give");
		IRCBot.registerCommand("cookies");
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
			if (triggerWord.equals(prefix + "give")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					String[] who = event.getMessage().split(" ");
					event.getChannel().send().action("gives " + who[1] + " some " + who[2]);
				}
			}			
		}
	}
}
