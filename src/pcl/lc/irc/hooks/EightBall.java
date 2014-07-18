/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class EightBall extends ListenerAdapter {
	public EightBall() {
		IRCBot.registerCommand("8ball");
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
			if (triggerWord.equals(prefix + "8ball")) {
				Random generator = new Random();
				String[] ballmessages = new String[] {"Signs point to yes", "Without a doubt", "Reply hazy, try again", "Ask again later", "My reply is no", "Outlook not so good"};
				int randommessage = generator.nextInt( 4 );
				event.respond(ballmessages[randommessage]);
			}
		}
	}
}
