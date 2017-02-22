/**
 * 
 */
package pcl.lc.irc.hooks;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.unix4j.Unix4j;

import com.google.common.collect.Lists;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Account;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class SED extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();
	public SED() {
        try {
            PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
            checkHook.setString(1, "SED");
            ResultSet results = checkHook.executeQuery();
            while (results.next()) {
            	enabledChannels.add(results.getString("channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		
		if (!event.getChannel().getName().isEmpty()) {
			String prefix = Config.commandprefix;
			String ourinput = event.getMessage().toLowerCase().replaceFirst(Pattern.quote(prefix), "");
			String trigger = ourinput.replaceAll("[^a-zA-Z0-9 ]", "").trim();
			String trigger2 = event.getMessage().toLowerCase().trim();
			if (trigger.length() > 1) {
				String messageEvent = event.getMessage();
				String reply = null;
				if (event.getMessage().matches("s/(.+)/(.+)")) {
					if (!IRCBot.isIgnored(event.getUser().getNick())) {					
						if (enabledChannels.contains(event.getChannel().getName())) {

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
							List<Entry<UUID, List<String>>> messageList = new ArrayList<>(IRCBot.messages.entrySet());
							for(Entry<UUID, List<String>> entry : Lists.reverse(messageList)){	
								if (entry.getValue().get(0).equals(event.getChannel().getName().toString())) {
									if (entry.getValue().get(2).indexOf(StringUtils.substringBetween(message, "/", "/"))>= 0 ) {
										try {
											reply = Unix4j.fromString(entry.getValue().get(2)).sed(message).toStringResult();
											if (reply.length() >= 380) {
												reply = reply.substring(0, 380);
											}
											event.getChannel().send().message("<" + entry.getValue().get(1) + "> " + reply);
											List<String> list = new ArrayList<String>();
											list.add(event.getChannel().getName().toString());
											list.add(entry.getValue().get(1));
											list.add(reply);
											IRCBot.messages.put(UUID.randomUUID(), list);
											return;
										} catch(IllegalArgumentException e) {
											event.respond("Invalid regex " + e.getMessage());
											return;
										}
									}
								}
							}
							return;
						}
					} 
				}else {
					String[] firstWord = StringUtils.split(trigger2);
					String triggerWord2 = firstWord[0];
					if (triggerWord2.equals(prefix + "sed")) {
						boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
						if (isOp || Helper.isChannelOp(event)) {
							String command = event.getMessage().substring(event.getMessage().indexOf("sed") + 3).trim();
							if (command.equals("disable")) {
								if (enabledChannels.contains(event.getChannel().getName())) {
									try {
										enabledChannels.remove(event.getChannel().getName());
										PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
										disableHook.setString(1, "SED");
										disableHook.setString(2, event.getChannel().getName());
										disableHook.executeUpdate();
									} catch (Exception e) {
										e.printStackTrace();
									}
									event.respond("Disabled SED for this channel");
									return;
								}
							} else if (command.equals("enable")) {
								if (!enabledChannels.contains(event.getChannel().getName())) {
									try {
										enabledChannels.add(event.getChannel().getName());
										PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
										enableHook.setString(1, "SED");
										enableHook.setString(2, event.getChannel().getName());
										enableHook.executeUpdate();
									} catch (Exception e) {
										e.printStackTrace();
									}
									event.respond("Enabled SED for this channel");
									return;
								}
							} else if (command.equals("list")) {
								event.respond("Enabled SED channels: " + enabledChannels);
								return;
							}
						}
					}
				}
			}			
		}
	}
}
