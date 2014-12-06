/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	public List<String> disabledChannels;
	public List<String> idfk = new ArrayList<String>();
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
				String messageEvent = event.getMessage();
				String reply = null;

				if (event.getMessage().matches("s/(.+)/(.+)")) {
					if (!disabledChannels.contains(event.getChannel().getName().toString())) {
						
						String s = messageEvent.substring(messageEvent.indexOf("/") + 1);
						s = s.substring(0, s.indexOf("/"));
						
						String message = event.getMessage();
						if (!message.substring(message.length() - 2).equals("/g")) {
							if(!message.substring(message.length() - 2).equals("/i")) {
								if (!message.substring(message.length() - 1).equals("/")) {
									message = message + "/";
								}
							}
						}
						Iterator it = IRCBot.messages.entries().iterator();
					    while (it.hasNext()) {
					        Map.Entry pairs = (Map.Entry)it.next();
							if (pairs.getValue().toString().matches(".*\\b*" + Pattern.quote(s) + "*\\b.*") && pairs.getKey().equals(event.getChannel().getName().toString())) {
								try {
									reply = Unix4j.fromString(pairs.getValue().toString()).sed(message).toStringResult();
									it.remove();
									event.respond(reply);
									return;
								} catch(IllegalArgumentException e) {
									event.respond("Invalid regex");
									return;
								}
							}
					    }
						return;
					}
				} else {
					if (!IRCBot.messages.containsValue("<" + event.getUser().getNick().toString() + "> " + event.getMessage())) {
						IRCBot.messages.put(event.getChannel().getName().toString(), "<" + event.getUser().getNick().toString() + "> " + event.getMessage());
						System.out.println(IRCBot.messages.toString());						
					}
					System.out.println(IRCBot.messages.toString());
				}
			}
		}
	}
}
