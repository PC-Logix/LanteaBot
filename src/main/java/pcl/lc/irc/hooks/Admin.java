/**
 * 
 */
package pcl.lc.irc.hooks;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.Account;
import pcl.lc.utils.FormatUtils;
import pcl.lc.utils.Helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Admin extends AbstractListener {
	private Command command_prefix;
	private Command command_join;
	private Command command_part;
	private Command command_shutdown;
	private Command command_cycle;
	private Command command_raw;
	private Command command_chnick;
	private Command command_hashcount;
	private Command command_flushhash;
	private Command command_ignore;
	private Command command_unignore;
	private Command command_ignorelist;
	private Command command_usercount;
	private Command command_authcount;
	private Command command_load;
	private Command command_commands;
	private Command command_charset;
	private Command command_ram;
	private Command command_flushauth;
	private Command command_restart;
	private Command command_test;
	private Command command_listadmins;
	private Command command_help;
	private Command command_authed;
	private Command command_addadmin;

	@Override
	protected void initHook() {
		command_prefix = new Command("prefix", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Config.prop.setProperty("commandprefix", params);
				Config.commandprefix = params;
				Config.saveProps();
				event.respond("Prefix changed to " + params);
			}
		}; command_prefix.setHelpText("Changes the prefix that the bot responds to, requires Bot Admin");
		command_join = new Command("join", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement addChannel = Database.getPreparedStatement("addChannel");
					addChannel.setString(1, params);
					addChannel.executeUpdate();
					event.getBot().sendIRC().joinChannel(params);
					event.getBot().sendIRC().notice(target, "Joined channel " + params);
				} catch (Exception e) {
					e.printStackTrace();
					event.getBot().sendIRC().notice(target, "Something went wrong!");
				}
			}
		}; command_join.setHelpText("Joins the channel supplied in the first arg, requires Bot Admin");
		command_part = new Command("part", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement removeChannel = Database.getPreparedStatement("removeChannel");
					removeChannel.setString(1, params);
					removeChannel.executeUpdate();
					event.getBot().getUserChannelDao().getChannel(params).send().part();
					event.getBot().sendIRC().notice(target, "Left channel " + params);
				} catch (Exception e) {
					e.printStackTrace();
					event.getBot().sendIRC().notice(target, "Something went wrong!");
				}
			}
		}; command_part.setHelpText("Parts the channel supplied in the first arg, requires Bot Admin, or Channel Op");
		command_shutdown = new Command("shutdown", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (!Config.httpdport.isEmpty()) {
					//TODO: Fix httpd stop
					//IRCBot.httpServer.stop();
				}
				//WikiChangeWatcher.stop();
				event.respond("Exiting");
				System.exit(1);
			}
		}; command_shutdown.setHelpText("Stops the bot, requires Bot Admin");
		command_cycle = new Command("cycle", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String channel = params;
				if (params.isEmpty()) {
					channel = ((MessageEvent) event).getChannel().getName();
				}
				partChannel(channel, event);
				joinChannel(channel, event);
			}
		}; command_cycle.setHelpText("Quickly parts and rejoins the current channel, requires Bot Admin");
		command_raw = new Command("raw", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				event.getBot().sendRaw().rawLine(params);
			}
		}; command_raw.setHelpText("Sends RAW IRC commands to the server, this can break stuff, requires Bot Admin");
		command_chnick = new Command("chnick", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				event.getBot().sendRaw().rawLineNow("NICK " + params);
			}
		}; command_chnick.setHelpText("Changes the bots nick to the supplied nick, requires Bot Admin");
		command_hashcount = new Command("hashcount", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Current hashmap size is: " + IRCBot.messages.size(), nick);
			}
		}; command_hashcount.setHelpText("Gets the current size of the hash table for various things, Requires Bot Admin");
		command_usercount = new Command("usercount", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Current hashmap size is: " + IRCBot.users.size(), nick);
			}
		}; command_usercount.setHelpText("Flushes the hash table used for various things, requires Bot Admin");
		command_authcount = new Command("authcount", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Current hashmap size is: " + IRCBot.authed.size(), nick);
			}
		}; command_authcount.setHelpText("Makes the bot ignore a user, requires Bot Admin, or Channel Op *THIS IS A GLOBAL IGNORE!*");
		command_flushhash = new Command("flushhash", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				IRCBot.messages.clear();
				Helper.sendMessage(target, "Hashmap size: " + IRCBot.messages.size(), nick);
			}
		}; command_flushhash.setHelpText("Unignores a user, requires Bot Admin, or Channel Op");
		command_flushauth = new Command("flushauth", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				IRCBot.authed.clear();
				Account.userCache.clear();
					/*for(Channel chan : event.getBot().getUserBot().getChannels()) {
						IRCBot.bot.sendRaw().rawLineNow("who " + chan.getName() + " %an");
					}*/
				Helper.sendMessage(target, "Authed hashmap size: " + IRCBot.authed.size(), nick);
			}
		}; command_flushauth.setHelpText("Prints the current ignore list, requires Bot Admin, or Channel Op");
		command_ignore = new Command("ignore", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				IRCBot.ignoredUsers.add(params);
				Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));
				Config.saveProps();
			}
		}; command_ignore.setHelpText("Count users");
		command_unignore = new Command("unignore", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				IRCBot.ignoredUsers.remove(params);
				Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));
				Config.saveProps();
			}
		}; command_unignore.setHelpText("Count authed users");
		command_ignorelist = new Command("ignorelist", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Ignored Users: " + IRCBot.ignoredUsers.toString(), nick);
			}
		}; command_ignorelist.setHelpText("Prints the list of ignored users");
		command_load = new Command("load", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					Config.config.addListener((Listener) Class.forName( "pcl.lc.irc.hooks." + params ).newInstance());
					event.respond("Module " + params + " Loaded");
				} catch( ClassNotFoundException e ) {
					event.respond("Module " + params + " not loaded " + e.fillInStackTrace());
				} catch (IllegalAccessException | InstantiationException e) {
					e.printStackTrace();
				}
			}
		}; command_load.setHelpText("Load module");
		command_commands = new Command("commands", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String listString = "";
				Iterator it = IRCBot.commands.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry)it.next();
					listString += pair.getKey() + ", ";
				}
				event.getUser().send().notice("Current commands: " + listString.replaceAll(", $", ""));
			}
		}; command_commands.setHelpText("List commands");
		command_charset = new Command("charset", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Default Charset=" + Charset.defaultCharset(), nick);
			}
		}; command_charset.setHelpText("Current default charset");
		command_ram = new Command("ram", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Runtime rt = Runtime.getRuntime();
				long m0 = rt.totalMemory() - rt.freeMemory();
				event.respond("Used RAM: " + FormatUtils.convertToStringRepresentation(m0));
			}
		}; command_ram.setHelpText("Current ram");
		command_restart = new Command("restart", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					restart();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}; command_restart.setHelpText("Restart");
		command_test = new Command("test", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "Success", nick);
			}

			@Override
			public void onExecuteFail(Command command, String nick, String target, long timeout) {
				if (timeout == Command.NO_PERMISSION)
					Helper.sendMessage(target, "No.", nick);
				else
					super.onExecuteFail(command, nick, target, timeout);
			}
		}; command_test.setHelpText("Test");
		command_listadmins = new Command("listadmins", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, IRCBot.getInstance().getOps().toString(), nick);
			}
		}; command_listadmins.setHelpText("List current admins");
		command_help = new Command("help", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 1) {
					String listString = "";
					for (Object o : IRCBot.commands.entrySet()) {
						Map.Entry pair = (Map.Entry) o;
						listString += pair.getKey() + ", ";
					}
					event.getUser().send().notice("Current commands: " + listString.replaceAll(", $", ""));
				} else {
					try {
						String l_command = params.get(1);
						event.getBot().sendIRC().notice(target, "help for " + l_command);
						event.getBot().sendIRC().notice(target, IRCBot.helpList.get(l_command));
					} catch (Exception e) {
						e.printStackTrace();
						event.getBot().sendIRC().notice(target, "Something went wrong!");
					}
				}
			}
		}; command_help.setHelpText("If you can read this you don't need help with help.");
		command_authed = new Command("authed", 0, Permissions.USER) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if(IRCBot.authed.containsKey(event.getUser().getNick())) {
					Helper.sendMessage(target, "Authenticated to Nickserv account " + IRCBot.authed.get(event.getUser().getNick()), nick);
				} else {
					Helper.sendMessage(target, "Nope.", nick);
				}
			}
		}; command_authed.setHelpText("Check if executer is authed");
		command_addadmin = new Command("addadmin", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				try {
					String newOpNick = params.get(0);
					User newOp = event.getBot().getUserChannelDao().getUser(newOpNick);
					if (!newOp.isVerified()) {
						Helper.sendMessage(target, "User " + newOpNick + " is not a registered user.", nick);
						return;
					}
					String nsRegistration = Account.getAccount(newOpNick, event);
					IRCBot.getInstance().getOps().add(nsRegistration);
					PreparedStatement addOp = Database.getPreparedStatement("addOp");
					addOp.setString(1, nsRegistration);
					addOp.executeUpdate();
					Helper.sendMessage(target, "User " + newOpNick + " (" + nsRegistration + ") added to list.", nick);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}; command_addadmin.setHelpText("Add a new admin");
		IRCBot.registerCommand(command_prefix, command_prefix.getHelpText());
		IRCBot.registerCommand(command_join, command_join.getHelpText());
		IRCBot.registerCommand(command_part, command_part.getHelpText());
		IRCBot.registerCommand(command_shutdown, command_shutdown.getHelpText());
		IRCBot.registerCommand(command_cycle, command_cycle.getHelpText());
		IRCBot.registerCommand(command_raw, command_raw.getHelpText());
		IRCBot.registerCommand(command_chnick, command_chnick.getHelpText());
		IRCBot.registerCommand(command_hashcount, command_hashcount.getHelpText());
		IRCBot.registerCommand(command_usercount, command_usercount.getHelpText());
		IRCBot.registerCommand(command_authcount, command_authcount.getHelpText());
		IRCBot.registerCommand(command_flushhash, command_flushhash.getHelpText());
		IRCBot.registerCommand(command_flushauth, command_flushauth.getHelpText());
		IRCBot.registerCommand(command_ignore, command_ignore.getHelpText());
		IRCBot.registerCommand(command_unignore, command_unignore.getHelpText());
		IRCBot.registerCommand(command_ignorelist, command_ignorelist.getHelpText());
		IRCBot.registerCommand(command_load, command_load.getHelpText());
		IRCBot.registerCommand(command_commands, command_commands.getHelpText());
		IRCBot.registerCommand(command_charset, command_charset.getHelpText());
		IRCBot.registerCommand(command_ram, command_ram.getHelpText());
		IRCBot.registerCommand(command_restart, command_restart.getHelpText());
		IRCBot.registerCommand(command_test, command_test.getHelpText());
		IRCBot.registerCommand(command_listadmins, command_listadmins.getHelpText());
		IRCBot.registerCommand(command_help, command_help.getHelpText());
		IRCBot.registerCommand(command_authed, command_authed.getHelpText());
		IRCBot.registerCommand(command_addadmin, command_addadmin.getHelpText());
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		command_prefix.tryExecute(command, nick, target, event, copyOfRange);
		command_join.tryExecute(command, nick, target, event, copyOfRange);
		command_part.tryExecute(command, nick, target, event, copyOfRange);
		command_shutdown.tryExecute(command, nick, target, event, copyOfRange);
		command_cycle.tryExecute(command, nick, target, event, copyOfRange);
		command_raw.tryExecute(command, nick, target, event, copyOfRange);
		command_chnick.tryExecute(command, nick, target, event, copyOfRange);
		command_hashcount.tryExecute(command, nick, target, event, copyOfRange);
		command_flushhash.tryExecute(command, nick, target, event, copyOfRange);
		command_ignore.tryExecute(command, nick, target, event, copyOfRange);
		command_unignore.tryExecute(command, nick, target, event, copyOfRange);
		command_ignorelist.tryExecute(command, nick, target, event, copyOfRange);
		command_usercount.tryExecute(command, nick, target, event, copyOfRange);
		command_authcount.tryExecute(command, nick, target, event, copyOfRange);
		command_load.tryExecute(command, nick, target, event, copyOfRange);
		command_commands.tryExecute(command, nick, target, event, copyOfRange);
		command_charset.tryExecute(command, nick, target, event, copyOfRange);
		command_ram.tryExecute(command, nick, target, event, copyOfRange);
		command_restart.tryExecute(command, nick, target, event, copyOfRange);
		command_flushauth.tryExecute(command, nick, target, event, copyOfRange);
		command_test.tryExecute(command, nick, target, event, copyOfRange);
		command_listadmins.tryExecute(command, nick, target, event, copyOfRange);
		command_help.tryExecute(command, nick, target, event, copyOfRange);
		command_authed.tryExecute(command, nick, target, event, copyOfRange);
		command_addadmin.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		String botNick = IRCBot.ournick.toLowerCase();
		String message = event.getMessage().toLowerCase();
		if (message.startsWith(botNick + ": prefix") || message.startsWith(botNick + ", prefix") || message.startsWith(botNick + " prefix")) {
			event.respond(Config.commandprefix);
		}
	}

	public static void sendKnock(String channel, ServerResponseEvent event) {
		event.getBot().sendRaw().rawLineNow("KNOCK " + channel + " : Lemme in!");
		IRCBot.invites.put(channel, channel);
	}

	private void joinChannel(String channel, GenericMessageEvent event) {
		event.getBot().sendIRC().joinChannel(channel);
	}

	private void partChannel(String channel, GenericMessageEvent event) {
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
		relaunch();
	}

	private static void relaunch() throws InterruptedException, UnsupportedEncodingException {
		String[] command = new String[]{"java", "-Dfile.encoding=UTF-8", "-jar", IRCBot.getThisJarFile().getAbsolutePath()};

		//Relaunches the bot using UTF-8 mode.
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.inheritIO(); //Tells the new process to use the same command line as this one.
		try {
			Process process = processBuilder.start();
			process.waitFor();  //We wait here until the actual bot stops. We do this so that we can keep using the same command line.
			System.exit(process.exitValue());
		} catch (IOException e) {
			if (e.getMessage().contains("\"java\"")) {
				System.out.println("BotLauncher: There was an error relaunching the bot. We couldn't find Java to launch with.");
				System.out.println("BotLauncher: Attempted to relaunch using the command:\n   " + StringUtils.join(command, " ", 0, command.length));
				System.out.println("BotLauncher: Make sure that you have Java properly set in your Operating System's PATH variable.");
				System.out.println("BotLauncher: Stopping here.");
			} else {
				e.printStackTrace();
			}
		}
	}
}
