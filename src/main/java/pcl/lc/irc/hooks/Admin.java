package pcl.lc.irc.hooks;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;
import pcl.lc.utils.FormatUtils;

import org.pircbotx.hooks.events.*;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Caitlyn
 * This module implements Admin commands
 *
 */

@SuppressWarnings("rawtypes")
public class Admin extends ListenerAdapter {

	public Admin() {
		IRCBot.registerCommand("prefix", "Changes the prefix that the bot responds to, requires Bot Admin");
		IRCBot.registerCommand("join", "Joins the channel supplied in the first arg, requires Bot Admin");
		IRCBot.registerCommand("part", "Parts the channel supplied in the first arg, requires Bot Admin, or Channel Op");
		IRCBot.registerCommand("shutdown", "Stops the bot, requires Bot Admin");
		IRCBot.registerCommand("cycle", "Quickly parts and rejoins the current channel, requires Bot Admin");
		IRCBot.registerCommand("raw", "Sends RAW IRC commands to the server, this can break stuff, requires Bot Admin");
		IRCBot.registerCommand("nick", "Changes the bots nick to the supplied nick, requires Bot Admin");
		IRCBot.registerCommand("hashcount", "Gets the current size of the hash table for various things, Requires Bot Admin");
		IRCBot.registerCommand("flushhash", "Flushes the hash table used for various things, requires Bot Admin");
		IRCBot.registerCommand("ignore", "Makes the bot ignore a user, requires Bot Admin, or Channel Op *THIS IS A GLOBAL IGNORE!*");
		IRCBot.registerCommand("unignore", "Unignores a user, requires Bot Admin, or Channel Op");
		IRCBot.registerCommand("ignorelist", "Prints the current ignore list, requires Bot Admin, or Channel Op");
	}

	@SuppressWarnings("unused")
	private void sendKnock(String channel, MessageEvent event) {
		event.getBot().sendRaw().rawLineNow("KNOCK " + channel + " :Asked to join by " + event.getUser().getNick());
		IRCBot.invites.put(channel, channel);
	}

	public static void sendKnock(String channel, ServerResponseEvent event) {
		event.getBot().sendRaw().rawLineNow("KNOCK " + channel + " : Lemme in!");
		IRCBot.invites.put(channel, channel);
	}

	private void joinChannel(String channel, MessageEvent event) {
		event.getBot().sendIRC().joinChannel(channel);
	}

	private void partChannel(String channel, MessageEvent event) {
		event.getBot().sendRaw().rawLine("PART " + channel + " :Commanded to part by " + event.getUser().getNick());
	}

	@Override
	public void onServerResponse(final ServerResponseEvent event) throws Exception {
		if (event.getCode() == 473) {
			ImmutableList channel = event.getParsedResponse();
			Admin.sendKnock(channel.toArray()[1].toString(), event);
		}
	}

	public void restart() throws URISyntaxException, IOException, Exception {

		if(!Config.httpdport.isEmpty() && !Config.botConfig.get("httpDocRoot").equals("")) {
			//TODO: Fix httpd stop
			//IRCBot.httpServer.stop();
		}

		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(IRCBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if(!currentJar.getName().endsWith(".jar"))
			return;
		try {
			Runtime.getRuntime().exec(javaBin + " -jar " + currentJar.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished");

		System.exit(0);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String[] splitMessage = event.getMessage().split(" ");
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		String sender = event.getUser().getNick();
		PircBotX bot = event.getBot();
		
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			String lowerNick = IRCBot.ournick.toLowerCase();
			
			if (splitMessage[0].equals(Config.commandprefix + "addadmin")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					try {
						String newOpNick = splitMessage[1];
						User newOp = bot.getUserChannelDao().getUser(newOpNick);
						if (!newOp.isVerified()) {
							bot.sendIRC().notice(sender, "User " + newOpNick + " is not a registered user.");
							return;
						}
						String nsRegistration = Account.getAccount(newOpNick, event);
						IRCBot.getInstance().getOps().add(nsRegistration);
						PreparedStatement addOp = IRCBot.getInstance().getPreparedStatement("addOp");
						addOp.setString(1, nsRegistration);
						addOp.executeUpdate();
						bot.sendIRC().notice(sender, "User " + newOpNick + " (" + nsRegistration + ") added to list.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			if (event.getMessage().toLowerCase().startsWith(lowerNick + " prefix")) {
				event.respond(prefix);
			} 

			if (triggerWord.equals(Config.commandprefix + "hashcount"))
				event.respond("Current hashmap size is: " + IRCBot.messages.size());

			if (triggerWord.equals(Config.commandprefix + "usercount"))
				event.respond("Current hashmap size is: " + IRCBot.users.size());

			if (triggerWord.equals(Config.commandprefix + "authcount"))
				event.respond("Current hashmap size is: " + IRCBot.authed.size());

			if (triggerWord.equals(Config.commandprefix + "authed")) {
				if(IRCBot.authed.containsKey(event.getUser().getNick())) {
					event.respond("Authenticated to Nickserv account " + IRCBot.authed.get(event.getUser().getNick()));
				} else {
					event.respond("Nope");
				}
			}

			if (triggerWord.equals(Config.commandprefix + "listadmins")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					event.respond("DEPERECATED! Current admins: " + IRCBot.admins.toString());
				}
			}
			
			if (triggerWord.equals(Config.commandprefix + "help")) {
				
				if (splitMessage.length == 1) {
					String listString = "";
				    Iterator it = IRCBot.commands.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        listString += pair.getKey() + ", ";
				    }
					event.getUser().send().notice("Current commands: " + listString.replaceAll(", $", ""));
				} else {
					try {
						String command = splitMessage[1];
						bot.sendIRC().notice(sender, "help for " + command);
						bot.sendIRC().notice(sender, IRCBot.helpList.get(command).toString());
					} catch (Exception e) {
						e.printStackTrace();
						bot.sendIRC().notice(sender, "Something went wrong!");
					}					
				}
			}
			
			if (splitMessage[0].equals(Config.commandprefix + "listadmins")) {
				bot.sendIRC().notice(sender,"Admin list");
				for (String entry : IRCBot.getInstance().getOps()) {
					bot.sendIRC().notice(sender, entry);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "prefix")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					String newPrefix = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					Config.prop.setProperty("commandprefix", newPrefix);
					Config.commandprefix = newPrefix;
					Config.saveProps();
					event.respond("Prefix changed to " + newPrefix);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "join")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					try {
						String newChannel = splitMessage[1];
						PreparedStatement addChannel = IRCBot.getInstance().getPreparedStatement("addChannel");
						addChannel.setString(1, newChannel);
						addChannel.executeUpdate();
						bot.sendIRC().joinChannel(newChannel);
						bot.sendIRC().notice(sender, "Joined channel " + newChannel);
					} catch (Exception e) {
						e.printStackTrace();
						bot.sendIRC().notice(sender, "Something went wrong!");
					}
				} else {
					bot.sendIRC().notice(sender, "You cannot do that.");
				}
			}

			if (triggerWord.equals(Config.commandprefix + "part")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp || event.getChannel().isOp(event.getUser())) {
					try {
						String oldChannel = splitMessage[1];
						PreparedStatement removeChannel = IRCBot.getInstance().getPreparedStatement("removeChannel");
						removeChannel.setString(1, oldChannel);
						removeChannel.executeUpdate();
						bot.getUserChannelDao().getChannel(oldChannel).send().part();
						bot.sendIRC().notice(sender, "Left channel " + oldChannel);
					} catch (Exception e) {
						e.printStackTrace();
						bot.sendIRC().notice(sender, "Something went wrong!");
					}
				} else {
					bot.sendIRC().notice(sender, "You cannot do that.");
				}
			}

			if (triggerWord.equals(Config.commandprefix + "shutdown")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					if(!Config.httpdport.isEmpty()) {
						//TODO: Fix httpd stop
						//IRCBot.httpServer.stop();
					}
					//WikiChangeWatcher.stop();
					event.respond("Exiting");
					System.exit(1);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "test")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					event.respond("Success");
				}
			}

			if (triggerWord.equals(Config.commandprefix + "restart")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					restart();
				}
			}

			if (triggerWord.equals(Config.commandprefix + "flushauth")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					IRCBot.authed.clear();
					Account.userCache.clear();
					/*for(Channel chan : event.getBot().getUserBot().getChannels()) {
						IRCBot.bot.sendRaw().rawLineNow("who " + chan.getName() + " %an");
					}*/
					event.respond("Authed hashmap size: " + IRCBot.authed.size());
				}
			}

			if (triggerWord.equals(Config.commandprefix + "ram")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					Runtime rt = Runtime.getRuntime();
					long m0 = rt.totalMemory() - rt.freeMemory();
					event.respond("Used RAM: " + FormatUtils.convertToStringRepresentation(m0));
				}
			}

			if (triggerWord.equals(Config.commandprefix + "flushhash")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					IRCBot.messages.clear();
					event.respond("Hashmap size: " + IRCBot.messages.size());
				}
			}

			if (triggerWord.equals(Config.commandprefix + "charset")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					event.respond("Default Charset=" + Charset.defaultCharset());
				}
			}

			if (triggerWord.equals(Config.commandprefix + "cycle")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					String channel = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					if (channel.isEmpty()) {
						channel = event.getChannel().getName();
					}
					partChannel(channel, event);
					joinChannel(channel, event);
				}
			}
			if (triggerWord.equals(Config.commandprefix + "raw")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					String string = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length());
					event.getBot().sendRaw().rawLine(string);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "chgnick")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					event.getBot().sendRaw().rawLineNow("NICK " + nick);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "load")) {

				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp) {
					String module = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					try {
						Config.config.addListener((Listener) Class.forName( "pcl.lc.irc.hooks." + module ).newInstance());
						event.respond("Module " + module + " Loaded");
					} catch( ClassNotFoundException e ) {
						event.respond("Module " + module + " not loaded " + e.fillInStackTrace());
					}
				}
			}

			if(triggerWord.equals(Config.commandprefix + "ignore")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp || event.getChannel().isOp(event.getUser())) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					IRCBot.ignoredUsers.add(nick);
					Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));		
					Config.saveProps();
				}
			}

			if(triggerWord.equals(Config.commandprefix + "unignore")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp || event.getChannel().isOp(event.getUser())) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					IRCBot.ignoredUsers.remove(nick);
					Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));
					Config.saveProps();
				}
			}

			if(triggerWord.equals(Config.commandprefix + "ignorelist")) {
				boolean isOp = Account.isOp(event.getBot(), event.getUser());
				if (isOp || event.getChannel().isOp(event.getUser())) {
					event.respond("Ignored Users: " + IRCBot.ignoredUsers.toString());			
				}
			}

			if (triggerWord.equals(Config.commandprefix + "commands")) {
				String listString = "";
			    Iterator it = IRCBot.commands.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        listString += pair.getKey() + ", ";
			    }
				event.getUser().send().notice("Current commands: " + listString.replaceAll(", $", ""));
			}
		}
	}
}
