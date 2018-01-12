/**
 * 
 */
package pcl.lc.irc.hooks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Helper;
import pcl.lc.utils.PasteUtils;

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
		String prefix = Config.commandprefix;
		target = Helper.getTarget(event);
		if (args.length > 0) {
			String messageEvent = String.join(" ", args).replace(prefix, "");
			if (messageEvent.matches("s/(.+)/(.+)")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					if (Helper.isEnabledHere(event.getChannel().getName(), "SED")) {
						String message = messageEvent;
						String pattern = "";
						String replacement = "";
						boolean isGlobal=false;
						boolean isCaseInsensitive = false;

						message = message.substring(message.indexOf("/")+1);

						pattern = message.substring(0,message.indexOf("/"));
						message = message.substring(message.indexOf("/")+1);

						if (message.indexOf('/')>=0) {
							replacement = message.substring(0,message.indexOf("/"));
							message = message.substring(message.indexOf("/")+1);

							if (!message.isEmpty()) {
								isGlobal = message.indexOf('g')>=0;
								isCaseInsensitive = message.indexOf('i')>=0;
							}
						} else { replacement = message; }

						Pattern regex;
						try {
							if(isCaseInsensitive) {
								regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE);                                 
							} else {
								regex = Pattern.compile(pattern);
							}
						} catch (PatternSyntaxException err) {
							event.respond("Invalid regex " + err.getPattern());
							return;
						}

						List<Entry<UUID, List<String>>> messageList = new ArrayList<>(IRCBot.messages.entrySet());
						for(Entry<UUID, List<String>> entry : Lists.reverse(messageList)){	
							if (entry.getValue().get(0).equals(event.getChannel().getName().toString())) {
								try {
									AntiPings = Helper.getNamesFromTarget(target);
									String reply;
									Matcher matcher;
									String newMessage;
									matcher = regex.matcher(entry.getValue().get(2));

									if(isGlobal) {
										reply = matcher.replaceAll(replacement);
									} else {
										reply = matcher.replaceFirst(replacement);
									}

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
									if (newMessage.length() > 380) {
										String pasteURL = PasteUtils.paste(newMessage, PasteUtils.Formats.NONE);
										newMessage = "Message too long to send to channel " + pasteURL;
									}

									event.getChannel().send().message(newMessage);
									List<String> list = new ArrayList<String>();
									list.add(event.getChannel().getName().toString());
									list.add(entry.getValue().get(1));
									list.add(reply);
									IRCBot.messages.put(UUID.randomUUID(), list);
									IRCBot.log.info("--> " + event.getChannel().getName().toString() + " " + newMessage);
									return;
								} catch(Exception e) {
									event.respond("Invalid regex " + e.getMessage());
									e.printStackTrace();
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
