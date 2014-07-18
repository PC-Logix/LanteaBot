package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;

import org.pircbotx.hooks.events.*;

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
		IRCBot.registerCommand("addadmin");
		IRCBot.registerCommand("listadmins");
		IRCBot.registerCommand("removeadmin");
	}

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

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);

		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			String lowerNick = IRCBot.ournick.toLowerCase();

			if (event.getMessage().toLowerCase().startsWith(lowerNick + " prefix")) {
				event.respond(prefix);
			} 

			if(triggerWord.equals(IRCBot.commandprefix + "lctodo"))
				event.respond("https://docs.google.com/spreadsheets/d/1Agsv7aHJ9JGDMnVYOtaoeDmoyKwoVCOppmwQm34fg1c");

			if (triggerWord.equals(IRCBot.commandprefix + "hashcount"))
				event.respond("Current hashmap size is: " + IRCBot.messages.size());

			if (triggerWord.equals(IRCBot.commandprefix + "usercount"))
				event.respond("Current hashmap size is: " + IRCBot.users.size());

			if (triggerWord.equals(IRCBot.commandprefix + "authcount"))
				event.respond("Current hashmap size is: " + IRCBot.authed.size());

			if (triggerWord.equals(IRCBot.commandprefix + "authed")) {
				if(IRCBot.authed.containsKey(event.getUser().getNick())) {
					event.respond("Authenticated to Nickserv account " + IRCBot.authed.get(event.getUser().getNick()));
				} else {
					event.respond("Nope");
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "prefix")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String newPrefix = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					IRCBot.prop.setProperty("commandprefix", newPrefix);
					IRCBot.commandprefix = newPrefix;
					IRCBot.saveProps();
					event.respond("Prefix changed to " + newPrefix);
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "join")) {
				String account = Account.getAccount(event.getUser(), event);
				System.out.print(account);
				if (IRCBot.admins.contains(account)) {
					String channel = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					if (event.getBot().getUserChannelDao().getChannel(channel).isInviteOnly()) {
						sendKnock(channel, event);
					} else {
						joinChannel(channel, event);
					}
					String channels = IRCBot.channels.concat("," + channel);
					IRCBot.prop.setProperty("channels", channels);
					IRCBot.saveProps();
				}
			}
			
			if (triggerWord.equals(IRCBot.commandprefix + "addadmin")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String admin = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					String admins = IRCBot.admins.concat("," + admin);
					IRCBot.prop.setProperty("admins", admins);
					IRCBot.saveProps();
					event.respond("New admin added");
				}
			}
			
			if (triggerWord.equals(IRCBot.commandprefix + "removeadmin")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String admin = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					String admins = IRCBot.admins.replace(admin, "");
					IRCBot.prop.setProperty("admins", admins);
					IRCBot.saveProps();
					event.respond("Admin removed");
				}
			}
			
			if (triggerWord.equals(IRCBot.commandprefix + "listadmins")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String admin = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					event.respond("Current Admins: " + IRCBot.admins);
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "part")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String channel = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					if (channel.isEmpty()) {
						channel = event.getChannel().getName();
					}
					partChannel(channel, event);
					String channels = IRCBot.channels.replace("," + channel, "");
					IRCBot.prop.setProperty("channels", channels);
					IRCBot.saveProps();
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "shutdown")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					event.respond("Exiting");
					System.exit(1);
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "flushauth")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					//IRCBot.authed.clear();
					for(Channel chan : event.getBot().getUserBot().getChannels()) {
						IRCBot.bot.sendRaw().rawLineNow("who " + chan.getName() + " %an");
					}

					event.respond("Authed hashmap size: " + IRCBot.authed.size());
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "cycle")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String channel = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					if (channel.isEmpty()) {
						channel = event.getChannel().getName();
					}
					partChannel(channel, event);
					joinChannel(channel, event);
				}
			}
			if (triggerWord.equals(IRCBot.commandprefix + "raw")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String string = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					event.getBot().sendRaw().rawLine(string);
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "chgnick")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String nick = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					event.getBot().sendRaw().rawLineNow("NICK " + nick);
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "load")) {
				String account = Account.getAccount(event.getUser(), event);
				if (IRCBot.admins.contains(account)) {
					String module = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();
					try {
						IRCBot.config.addListener((Listener) Class.forName( "pcl.lc.irc.hooks." + module ).newInstance());
						event.respond("Module " + module + " Loaded");
					} catch( ClassNotFoundException e ) {
						event.respond("Module " + module + " not loaded");
					}
				}
			}

			if (triggerWord.equals(IRCBot.commandprefix + "commands")) {
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
