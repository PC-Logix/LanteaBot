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
	List<String> enabledChannels;

	private Command local_command;

	@Override
	protected void initHook() {
		enabledChannels = new ArrayList<String>();
		try {
			PreparedStatement checkHook = Database.getPreparedStatement("checkHook");
			checkHook.setString(1, "SED");
			ResultSet results = checkHook.executeQuery();
			while (results.next()) {
				String channel = results.getString("channel");
				System.out.println(channel);
				enabledChannels.add(channel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		local_command = new Command("sed", 10, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("disable")) {
					if (enabledChannels.contains(target)) {
						try {
							enabledChannels.remove(target);
							PreparedStatement disableHook = Database.getPreparedStatement("disableHook");
							disableHook.setString(1, "SED");
							disableHook.setString(2, target);
							disableHook.executeUpdate();
						} catch (Exception e) {
							e.printStackTrace();
						}
						event.respond("Disabled SED for this channel");
						return;
					}
				} else if (params.equals("enable")) {
					if (!enabledChannels.contains(target)) {
						try {
							enabledChannels.add(target);
							PreparedStatement enableHook = Database.getPreparedStatement("enableHook");
							enableHook.setString(1, "SED");
							enableHook.setString(2, target);
							enableHook.executeUpdate();
						} catch (Exception e) {
							e.printStackTrace();
						}
						event.respond("Enabled SED for this channel");
						return;
					}
				}
				String isEnabled = enabledChannels.contains(target) ? "enabled" : "disabled";
				Helper.sendMessage(target, "SED is " + isEnabled + " in this channel", nick);
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
					if (enabledChannels.contains(event.getChannel().getName())) {
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
