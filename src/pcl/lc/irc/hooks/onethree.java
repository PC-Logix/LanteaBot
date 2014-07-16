/**
 * 
 */
package pcl.lc.irc.hooks;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class onethree extends ListenerAdapter {
	public onethree() {
		IRCBot.registerCommand("onethree");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(IRCBot.commandprefix + "onethree")) {
				if(event.getChannel().getName().equals("#oc"))
					event.respond("https://github.com/MightyPirates/OpenComputers/wiki/OneThree");
			}
		}
	}
}