/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.unix4j.Unix4j;

import com.google.common.base.Joiner;

import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;

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
			String trigger2 = event.getMessage().toLowerCase().trim();
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

						for (Entry<UUID, List<String>> entry : IRCBot.messages.entrySet()) {
							if (entry.getValue().get(0).equals(event.getChannel().getName().toString())) {
								System.out.println(entry.getValue().get(2));
								if (entry.getValue().get(2).indexOf(StringUtils.substringBetween(message, "/", "/"))>= 0 ) {
									try {
										reply = Unix4j.fromString(entry.getValue().get(2)).sed(message).toStringResult();
										event.getChannel().send().message("<" + entry.getValue().get(1) + "> " + reply);
										return;
									} catch(IllegalArgumentException e) {
										event.respond("Invalid regex");
										return;
									}
								}
							}
						}
						return;
					}
				} else {
					String[] firstWord = StringUtils.split(trigger2);
					String triggerWord2 = firstWord[0];
					if (triggerWord2.equals(prefix + "sed")) {
						String account = Account.getAccount(event.getUser(), event);
						if (IRCBot.admins.containsKey(account)) {
							String command = event.getMessage().substring(event.getMessage().indexOf("sed") + 3).trim();
							System.out.println(command);
							if (command.equals("disable")) {
								disabledChannels.add(event.getChannel().getName().toString());
								IRCBot.prop.setProperty("seddisabled-channels", Joiner.on(",").join(disabledChannels));
								IRCBot.saveProps();
								return;
							} else if (command.equals("enable")) {
								disabledChannels.remove(event.getChannel().getName().toString());
								IRCBot.prop.setProperty("seddisabled-channels", Joiner.on(",").join(disabledChannels));
								IRCBot.saveProps();
								return;
							} else if (command.equals("list")) {
								event.respond("Disabled SED channels: " + disabledChannels);
								return;
							}
						}
					}
				}
			}
		}
	}
}