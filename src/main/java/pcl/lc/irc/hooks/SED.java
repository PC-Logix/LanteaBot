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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.unix4j.Unix4j;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class SED extends AbstractListener {
	public static ImmutableSortedSet<String> AntiPings;
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("sed", 10, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("disable") || params.equals("enable")) {
					Helper.toggleCommand("SED", target, params);
				} else {
					String isEnabled = Helper.isEnabledHere(target, "SED") ? "enabled" : "disabled";
					Helper.sendMessage(target, "SED is " + isEnabled + " in this channel", nick);
				}
			}
		}; local_command.setHelpText("SED Operations");
		IRCBot.registerCommand(local_command);
	}
	
	@Override
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		//if (!event.getChannel().getName().isEmpty()) {
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase().replaceFirst(Pattern.quote(prefix), "");
		String trigger = ourinput./*replaceAll("[^a-zA-Z0-9 ]", "").*/trim();
		if (trigger.length() > 1) {
			String messageEvent;
			if (String.join(" ", args).startsWith(prefix)) {
				messageEvent = String.join(" ", args).replace(prefix, "");
			} else {
				messageEvent = String.join(" ", args);
			}
			String reply = null;
			if (messageEvent.matches("s/(.+)/(.+)")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					if (Helper.isEnabledHere(event.getChannel().getName(), "SED")) {
						String s = messageEvent.substring(messageEvent.indexOf("/") + 1);
						s = s.substring(0, s.indexOf("/"));

						String message;
						if (event.getMessage().startsWith(prefix)) {
							message = messageEvent.replace(prefix, "");
						} else {
							message = messageEvent;
						}
						if (!message.substring(message.length() - 2).equals("/g")) {
							if(!message.substring(message.length() - 2).equalsIgnoreCase("/i")) {
								if (!message.substring(message.length() - 1).equals("/")) {
									message = message + "/";
								}
							}
						}
						if(message.substring(message.length() - 2).equals("/i")) {
							int length = message.length();
							String capitalizedLetter = message.substring(length - 1, length).toUpperCase();
							message = message.substring(0, length - 1) + capitalizedLetter;
						}
						List<Entry<UUID, List<String>>> messageList = new ArrayList<>(IRCBot.messages.entrySet());
						for(Entry<UUID, List<String>> entry : Lists.reverse(messageList)){	
							if (entry.getValue().get(0).equals(event.getChannel().getName().toString())) {
								AntiPings = Helper.getNamesFromTarget(target);
								//if (entry.getValue().get(2).indexOf(StringUtils.substringBetween(message, "/", "/"))>= 0 ) {
								try {
									reply = Unix4j.fromString(entry.getValue().get(2)).sed(message).toStringResult();
									if (reply.length() >= 380) {
										reply = reply.substring(0, 380);
									}
									//Helper.sendMessage(event.getChannel().getName().toString(), reply, "<" + entry.getValue().get(1) + ">".replace(": ", ""));
									String newMessage;
									if (reply.length() > 1 && reply.charAt(0) == 1 && reply.charAt(reply.length() - 1) == 1)
										newMessage = "* " + entry.getValue().get(1) + " " + reply.substring(1, reply.length() - 1);
									else
										newMessage = "<" + entry.getValue().get(1) + "> " + reply;
									if (reply.equals(entry.getValue().get(2))) {
										continue;
									}
									
									if (AntiPings != null && !AntiPings.isEmpty()) {
										String findMatch = Helper.stringContainsItemFromList(newMessage, AntiPings);
										if (!findMatch.equals("false")) {
											String[] parts = findMatch.split(" ");
											for (String part : parts) {
												newMessage = newMessage.replace(part, Helper.antiPing(part));
											}
										}
									}
									AntiPings = null;
									event.getChannel().send().message(newMessage);
									List<String> list = new ArrayList<String>();
									list.add(event.getChannel().getName().toString());
									list.add(entry.getValue().get(1));
									list.add(reply);
									IRCBot.messages.put(UUID.randomUUID(), list);
									IRCBot.log.info("--> " + event.getChannel().getName().toString() + " " + newMessage);
									return;
								} catch(IllegalArgumentException e) {
									event.respond("Invalid regex " + e.getMessage());
									return;
								}
							}
						}
					}
				} 
			}
		}
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
