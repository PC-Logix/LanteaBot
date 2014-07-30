/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.unix4j.Unix4j;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class SED extends ListenerAdapter {
	List<String> disabledChannels;
	public SED() {
		disabledChannels = new ArrayList<String>(Arrays.asList(IRCBot.prop.getProperty("seddisabled-channels", "").split(",")));
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		if (!event.getChannel().getName().isEmpty()) {
			String prefix = IRCBot.commandprefix;
			String ourinput = event.getMessage().toLowerCase().replaceFirst(Pattern.quote(prefix), "");
			String trigger = ourinput.replaceAll("[^a-zA-Z0-9 ]", "").trim();
			if (trigger.length() > 1) {
				String messageEvent = event.getUser().getNick().toString() + "|" + event.getChannel().getName().toString();
				String reply = null;

				if (event.getMessage().matches("s/(.+)/(.+)")) {
					if (!disabledChannels.contains(event.getChannel().getName().toString())) {
						String message = event.getMessage();
						if (!message.substring(message.length() - 2).equals("/g")) {
							if(!message.substring(message.length() - 2).equals("/i")) {
								if (!message.substring(message.length() - 1).equals("/")) {
									message = message + "/";
								}
							}
						}
						if (IRCBot.messages.containsKey(messageEvent)) {
							try {
								reply = Unix4j.fromString(IRCBot.messages.get(messageEvent).toString()).sed(message).toStringResult();
							} catch(IllegalArgumentException e) {
								event.respond("Invalid regex");
								return;
							}
							event.respond(reply);
						}
						return;
					}
				} else {
					IRCBot.messages.put(messageEvent, event.getMessage());
				}
			}
		}
	}
}
