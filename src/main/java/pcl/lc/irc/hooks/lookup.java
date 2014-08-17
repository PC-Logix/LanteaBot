package pcl.lc.irc.hooks;

import java.net.InetAddress;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class lookup extends ListenerAdapter {
	public lookup() {
		IRCBot.registerCommand("lookup");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] message = StringUtils.split(trigger);
			String triggerWord = message[0];
			if (triggerWord.equals(prefix + "lookup")) {
				InetAddress addr = InetAddress.getByName(message[1]);
				event.respond("Hostname: " + addr.getHostName() + " IP: " + addr.getHostAddress());
			}
		}
	}
}