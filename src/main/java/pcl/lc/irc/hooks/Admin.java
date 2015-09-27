package pcl.lc.irc.hooks;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;

import pcl.lc.httpd.httpd;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.job.WikiChangeWatcher;
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
		IRCBot.registerCommand("prefix");
		IRCBot.registerCommand("join");
		IRCBot.registerCommand("part");
		IRCBot.registerCommand("shutdown");
		IRCBot.registerCommand("cycle");
		IRCBot.registerCommand("raw");
		IRCBot.registerCommand("nick");
		IRCBot.registerCommand("lctodo");
		IRCBot.registerCommand("hashcount");
		IRCBot.registerCommand("flushhash");
		IRCBot.registerCommand("ignore");
		IRCBot.registerCommand("unignore");
		IRCBot.registerCommand("ignorelist");
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
			System.out.println();
			Admin.sendKnock(channel.toArray()[1].toString(), event);
		}
	}

	public void restart() throws URISyntaxException, IOException, Exception {

		if(!Config.httpdport.isEmpty() && !Config.botConfig.get("httpDocRoot").equals("")) {
			IRCBot.httpServer.stop();
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
		boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			String lowerNick = IRCBot.ournick.toLowerCase();
			String account = Account.getAccount(event.getUser(), event);
			
			if (splitMessage[0].equals(Config.commandprefix + "addadmin")) {
				if (IRCBot.admins.containsKey(account) || isOp) {
					try {
						String newOpNick = splitMessage[1];
						User newOp = bot.getUserChannelDao().getUser(newOpNick);
						if (!newOp.isVerified()) {
							bot.sendIRC().notice(sender, "User " + newOpNick + " is not a registered user.");
							return;
						}
						String nsRegistration;
						bot.sendRaw().rawLine("WHOIS " + newOpNick + " " + newOpNick);
						WaitForQueue waitForQueue = new WaitForQueue(bot);
						WhoisEvent whoisEvent = waitForQueue.waitFor(WhoisEvent.class);
						waitForQueue.close();
						nsRegistration = whoisEvent.getRegisteredAs();
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

			if(triggerWord.equals(Config.commandprefix + "lctodo"))
				event.respond("https://docs.google.com/spreadsheets/d/1Agsv7aHJ9JGDMnVYOtaoeDmoyKwoVCOppmwQm34fg1c");

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
				if (IRCBot.admins.containsKey(account)) {
					event.respond("DEPERECATED! Current admins: " + IRCBot.admins.toString());
				}
			}
			
			if (splitMessage[0].equals("listadmins")) {
				bot.sendIRC().notice(sender,"Admin list");
				for (String entry : IRCBot.getInstance().getOps()) {
					bot.sendIRC().notice(sender, entry);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "prefix")) {
				if (IRCBot.admins.containsKey(account)) {
					String newPrefix = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					Config.prop.setProperty("commandprefix", newPrefix);
					Config.commandprefix = newPrefix;
					Config.saveProps();
					event.respond("Prefix changed to " + newPrefix);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "join")) {
				System.out.print(account);
				if (IRCBot.admins.containsKey(account)) {
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
				if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
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
				if (IRCBot.admins.containsKey(account)) {
					if(!Config.httpdport.isEmpty()) {
						IRCBot.httpServer.stop();
					}
					//WikiChangeWatcher.stop();
					event.respond("Exiting");
					System.exit(1);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "test")) {
				if (IRCBot.admins.containsKey(account)) {
					event.respond("Success");
				}
			}

			if (triggerWord.equals(Config.commandprefix + "restart")) {
				if (IRCBot.admins.containsKey(account)) {
					restart();
				}
			}

			if (triggerWord.equals(Config.commandprefix + "flushauth")) {
				if (IRCBot.admins.containsKey(account)) {
					//IRCBot.authed.clear();
					for(Channel chan : event.getBot().getUserBot().getChannels()) {
						IRCBot.bot.sendRaw().rawLineNow("who " + chan.getName() + " %an");
					}
					event.respond("Authed hashmap size: " + IRCBot.authed.size());
				}
			}

			if (triggerWord.equals(Config.commandprefix + "ram")) {
				if (IRCBot.admins.containsKey(account)) {
					Runtime rt = Runtime.getRuntime();
					long m0 = rt.totalMemory() - rt.freeMemory();
					event.respond("Used RAM: " + FormatUtils.convertToStringRepresentation(m0));
				}
			}

			if (triggerWord.equals(Config.commandprefix + "flushhash")) {
				if (IRCBot.admins.containsKey(account)) {
					IRCBot.messages.clear();
					event.respond("Hashmap size: " + IRCBot.messages.size());
				}
			}

			if (triggerWord.equals(Config.commandprefix + "charset")) {
				if (IRCBot.admins.containsKey(account)) {
					event.respond("Default Charset=" + Charset.defaultCharset());
				}
			}

			if (triggerWord.equals(Config.commandprefix + "cycle")) {
				if (IRCBot.admins.containsKey(account)) {
					String channel = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					if (channel.isEmpty()) {
						channel = event.getChannel().getName();
					}
					partChannel(channel, event);
					joinChannel(channel, event);
				}
			}
			if (triggerWord.equals(Config.commandprefix + "raw")) {
				if (IRCBot.admins.containsKey(account)) {
					String string = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length());
					event.getBot().sendRaw().rawLine(string);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "chgnick")) {
				if (IRCBot.admins.containsKey(account)) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					event.getBot().sendRaw().rawLineNow("NICK " + nick);
				}
			}

			if (triggerWord.equals(Config.commandprefix + "load")) {

				if (IRCBot.admins.containsKey(account)) {
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
				if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					IRCBot.ignoredUsers.add(nick);
					Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));		
					Config.saveProps();
				}
			}

			if(triggerWord.equals(Config.commandprefix + "unignore")) {
				if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					IRCBot.ignoredUsers.remove(nick);
					Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));
					Config.saveProps();
				}
			}

			if(triggerWord.equals(Config.commandprefix + "ignorelist")) {
				if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
					event.respond("Ignored Users: " + IRCBot.ignoredUsers.toString());			
				}
			}

			if (triggerWord.equals(Config.commandprefix + "commands")) {
				String listString = "";

				for (String s : IRCBot.commands)
				{
					listString += s + ", ";
				} 
				event.getUser().send().notice("Current commands: " + listString.replaceAll(", $", ""));
			}
		}
	}
}
