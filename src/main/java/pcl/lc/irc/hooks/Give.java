/**
 * 
 */
package pcl.lc.irc.hooks;

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
public class Give extends ListenerAdapter {
	public Give() {
		IRCBot.registerCommand("give", Config.commandprefix + "give <nick> <item>  ex: " + Config.commandprefix + "give User cake " +IRCBot.ournick + " gives User some cake");
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
					StringBuilder strBuilder = new StringBuilder();
					for (int i = 2; i < who.length; i++) {
					   strBuilder.append(who[i] + " ");
					}
					String newString = strBuilder.toString();
					event.getChannel().send().action("gives " + who[1] + " some " + newString);
				}
			}			
		}
	}
}
