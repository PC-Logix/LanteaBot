/**
 *
 */
package pcl.lc.irc.hooks;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.time.Duration;
import java.time.Instant;

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
	private Command command_syntax;
	private Command command_authed;
	private Command command_addadmin;
	private Command command_time_test;
	private Command command_whatami;
	private Command command_ami;
	private Command command_debug;

	private Instant start_time = null;

	@Override
	protected void initHook() {
		start_time = Instant.now();
		Database.addStatement("CREATE TABLE IF NOT EXISTS Ignore(username PRIMARY KEY, time INTEGER)");
		Database.addPreparedStatement("addIgnore", "INSERT INTO Ignore (username, time) VALUES (?, ?)");
		Database.addPreparedStatement("removeIgnore", "DELETE FROM Ignore WHERE username = ?");
		Database.addPreparedStatement("getIgnores", "SELECT username, time FROM Ignore;");
		PreparedStatement statement;
		try {
			statement = Database.getPreparedStatement("getIgnores");
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				IRCBot.ignoredUsers.add(resultSet.getString(1));
				//items += resultSet.getString(2) + ((resultSet.getInt(3) == -1) ? " (*)" : "") + "\n";
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		command_prefix = new Command("prefix", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Prefix")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String prefix = this.argumentParser.getArgument("Prefix");
				Config.prop.setProperty("commandprefix", prefix);
				Config.commandprefix = prefix;
				Config.saveProps();
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Prefix changed to " + prefix);
				return new CommandChainStateObject();
			}
		};
		command_prefix.setHelpText("Changes the prefix that the bot responds to, requires Bot Admin");
		command_join = new Command("join", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Channel")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String channel = this.argumentParser.getArgument("Channel");
				PreparedStatement addChannel = Database.getPreparedStatement("addChannel");
				addChannel.setString(1, channel);
				addChannel.executeUpdate();
				event.getBot().sendIRC().joinChannel(channel);
				Helper.AntiPings = Helper.getNamesFromTarget(channel);
				Helper.sendMessage(target, "Joined channel " + channel);
				return new CommandChainStateObject();
			}
		};
		command_join.setHelpText("Joins the channel supplied in the first arg, requires Bot Admin");
		command_part = new Command("part", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Channel")), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String channel = this.argumentParser.getArgument("Channel");
				PreparedStatement removeChannel = Database.getPreparedStatement("removeChannel");
				removeChannel.setString(1, channel);
				removeChannel.executeUpdate();
				event.getBot().getUserChannelDao().getChannel(channel).send().part();
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Left channel " + channel);
				return new CommandChainStateObject();
			}
		};
		command_part.setHelpText("Parts the channel supplied in the first arg, requires Bot Admin, or Channel Op");
		command_shutdown = new Command("shutdown", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (!Config.httpdport.isEmpty()) {
					//TODO: Fix httpd stop
					//IRCBot.httpServer.stop();
				}
				//WikiChangeWatcher.stop();
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Exiting");
				System.exit(1);
				return new CommandChainStateObject();
			}
		};
		command_shutdown.setHelpText("Stops the bot, requires Bot Admin");
		command_cycle = new Command("cycle", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING, "Channel")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String channel = this.argumentParser.getArgument("Channel");
				if (channel == null || params.isEmpty()) {
					channel = ((MessageEvent) event).getChannel().getName();
				}
				partChannel(channel, event);
				joinChannel(channel, event);
				return new CommandChainStateObject();
			}
		};
		command_cycle.setHelpText("Quickly parts and rejoins the current or specified channel.");
		command_raw = new Command("raw", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Message")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				event.getBot().sendRaw().rawLine(this.argumentParser.getArgument("Message"));
				return new CommandChainStateObject();
			}
		};
		command_raw.setHelpText("Sends RAW IRC commands to the server, this can break stuff, requires Bot Admin");
		command_chnick = new Command("chnick", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Nick")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				event.getBot().sendRaw().rawLineNow("NICK " + this.argumentParser.getArgument("Nick"));
				return new CommandChainStateObject();
			}
		};
		command_chnick.setHelpText("Changes the bots nick to the supplied nick.");
		command_hashcount = new Command("hashcount", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Current hashmap size is: " + IRCBot.messages.size(), nick);
				return new CommandChainStateObject();
			}
		};
		command_hashcount.setHelpText("Gets the current size of the hash table for various things.");
		command_usercount = new Command("usercount") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Current usercount is: " + IRCBot.users.size(), nick);
				return new CommandChainStateObject();
			}
		};
		command_usercount.setHelpText("Count users");
		command_authcount = new Command("authcount") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Current auth count is: " + IRCBot.authed.size(), nick);
				return new CommandChainStateObject();
			}
		};
		command_authcount.setHelpText("Count authed users");
		command_flushhash = new Command("flushhash", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				IRCBot.messages.clear();
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Hashmap size: " + IRCBot.messages.size(), nick);
				return new CommandChainStateObject();
			}
		};
		command_flushhash.setHelpText("Flushes the hash table used for various things.");
		command_flushauth = new Command("flushauth", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				IRCBot.authed.clear();
				Account.userCache.clear();
				/*for(Channel chan : event.getBot().getUserBot().getChannels()) {
						IRCBot.bot.sendRaw().rawLineNow("who " + chan.getName() + " %an");
					}*/
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Authed hashmap size: " + IRCBot.authed.size(), nick);
				return new CommandChainStateObject();
			}
		};
		command_flushauth.setHelpText("Prints the current authed user list.");
		command_ignore = new Command("ignore", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Nick")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String name = this.argumentParser.getArgument("Nick");
				String user = null;
				if (name.contains("@")) {
					String s = IRCBot.getDiscordID(name.replace("\"", "").replaceAll("\\p{C}", ""));
					user = s;
					IRCBot.ignoredUsers.add(s);
				} else {
					user = name;
					IRCBot.ignoredUsers.add(name);
				}
				PreparedStatement add = Database.getPreparedStatement("addIgnore");
				add.setString(1, user);
				add.setInt(2, 0);
				if (add.executeUpdate() > 0) {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "User added to ignore list");
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "ERROR!");
				}
				//Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));
				//Config.saveProps();
				return new CommandChainStateObject();
			}
		};
		command_ignore.setHelpText("Makes the bot ignore a user. *THIS IS A GLOBAL IGNORE!*");
		command_unignore = new Command("unignore", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Nick")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String name = this.argumentParser.getArgument("Nick");
				String user = null;
				if (name.contains("@")) {
					String s = IRCBot.getDiscordID(name.replace("\"", "").replaceAll("\\p{C}", ""));
					user = s;
					IRCBot.ignoredUsers.remove(s);
				} else {
					user = name;
					IRCBot.ignoredUsers.remove(name);
				}

				PreparedStatement rem = Database.getPreparedStatement("removeIgnore");
				rem.setString(1, user);
				if (rem.executeUpdate() > 0) {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "User removed from ignore list");
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "ERROR!");
				}

				//Config.prop.setProperty("ignoredUsers", Joiner.on(",").join(IRCBot.ignoredUsers));
				//Config.saveProps();
				return new CommandChainStateObject();
			}
		};
		command_unignore.setHelpText("Unignores a user.");
		command_ignorelist = new Command("ignorelist", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Ignored Users: " + IRCBot.ignoredUsers.toString(), nick);
				return new CommandChainStateObject();
			}
		};
		command_ignorelist.setHelpText("Prints the list of ignored users.");
		command_load = new Command("load", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Module")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws IllegalAccessException, InstantiationException {
				String module = this.argumentParser.getArgument("Module");
				try {
					Config.config.addListener((Listener) Class.forName("pcl.lc.irc.hooks." + module).newInstance());
					event.respond("Module " + module + " Loaded");
				} catch (ClassNotFoundException e) {
					event.respond("Module " + module + " not loaded " + e.fillInStackTrace());
				}
				return new CommandChainStateObject();
			}
		};
		command_load.setHelpText("Load module.");
		command_commands = new Command("commands") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String listString = "";
				Iterator it = IRCBot.commands.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					listString += pair.getKey() + ", ";
				}
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Current commands: " + listString.replaceAll(", $", ""));
				return new CommandChainStateObject();
			}
		};
		command_commands.setHelpText("List commands.");
		command_charset = new Command("charset", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Default Charset=" + Charset.defaultCharset(), nick);
				return new CommandChainStateObject();
			}
		};
		command_charset.setHelpText("Returns current default charset.");
		command_ram = new Command("ram", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Runtime rt = Runtime.getRuntime();
				long m0 = rt.totalMemory() - rt.freeMemory();
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Used RAM: " + FormatUtils.convertToStringRepresentation(m0));
				return new CommandChainStateObject();
			}
		};
		command_ram.setHelpText("Returns current used ram.");
		command_restart = new Command("restart", Permissions.EVERYONE) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Duration timeElapsed = Duration.between(start_time, Instant.now());
				boolean permissionOverride = Permissions.hasPermission(IRCBot.bot, (MessageEvent) event, Permissions.ADMIN);
				if (permissionOverride || (timeElapsed.toMillis() < (1000 * 60 * 60 * 24))) {
					try {
						restart(target);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Helper.sendMessage(target, "Sorry, only an admin can restart the bot within 24 hours of startup.", nick);
				}
				return new CommandChainStateObject();
			}
		};
		command_restart.setHelpText("Restart the bot.");
		command_test = new Command("test", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, "Success", nick);
				return new CommandChainStateObject();
			}

			@Override
			public CommandChainStateObject onExecuteFail(Command command, String nick, String target, long timeout) {
				if (timeout == Command.NO_PERMISSION) {
					Helper.sendMessage(target, "No.", nick);
					return new CommandChainStateObject(CommandChainState.ERROR, "No permission");
				} else {
					return super.onExecuteFail(command, nick, target, timeout);
				}
			}
		};
		command_test.setHelpText("Test.");
		command_listadmins = new Command("listadmins") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, IRCBot.getInstance().getOps().toString(), nick);
				return new CommandChainStateObject();
			}
		};
		command_listadmins.setHelpText("List current admins.");
		command_help = new Command("help", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Command"))) {
			@Override
			public String onInvalidArguments(ArrayList<String> params) {
				if (params.size() == 0)
					return "Command list: " + httpd.getBaseDomain() + "/help";
				return super.onInvalidArguments(params);
			}

			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String cmd = this.argumentParser.getArgument("Command");
				System.out.println("Find '" + cmd + "'");
				if (params.size() == 0) {
					String listString = "";
					for (Object o : IRCBot.commands.entrySet()) {
						Map.Entry pair = (Map.Entry) o;
						listString += pair.getKey() + ", ";
					}
					event.getUser().send().notice("Current commands: " + listString.replaceAll(", $", ""));
				} else {
					String nickClean = nick.replaceAll("\\p{C}", "");

					Command com = Command.findCommand(cmd);
					if (com == null) {
						Helper.sendNotice(nick, "Unable to find the command '" + cmd + "'", this.callingRelay);
					} else {
						ArrayList<String> aliases = com.getAliases();
						String helpText = com.getHelpText();
						Helper.sendNotice(nick, "help for command '" + cmd + "': " + helpText, this.callingRelay);
						if (com.getPermissionLevel() != null)
							Helper.sendNotice(nick, "Required permission level: " + com.getPermissionLevel(), this.callingRelay);
						else
							Helper.sendNotice(nick, "This is a dynamic command", this.callingRelay);
						if (aliases.size() > 0)
							Helper.sendNotice(nick, "Aliases:  " + String.join(", ", aliases), this.callingRelay);
						if (com.argumentParser != null)
							Helper.sendNotice(nick, "Syntax: " + Config.commandprefix + com.getCommand() + " " + com.argumentParser.getArgumentSyntax());
					}
				}
				return new CommandChainStateObject();
			}
		};
		command_help.setHelpText("If you can read this you don't need help with help.");
		command_syntax = new Command("syntax", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Command")), Permissions.EVERYONE) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String com = this.argumentParser.getArgument("Command");
				Command cmd = Command.findCommand(com);
				if (cmd == null) {
					Helper.sendMessage(target, "Unable to find command '" + com + "'", nick);
				} else {
					if (cmd.argumentParser != null)
						Helper.sendMessage(target, Config.commandprefix + cmd.getCommand() + " " + cmd.argumentParser.getArgumentSyntax(), nick);
					else
						Helper.sendMessage(target, "This command has no argument syntax defined.", nick);
				}
				return new CommandChainStateObject();
			}
		};
		command_syntax.setHelpText("This probably does what you'd imagine it does.");
		command_authed = new Command("authed", Permissions.EVERYONE) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (IRCBot.authed.containsKey(event.getUser().getNick())) {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "Authenticated to Nickserv account " + IRCBot.authed.get(event.getUser().getNick()), nick);
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "Nope.", nick);
				}
				return new CommandChainStateObject();
			}
		};
		command_authed.setHelpText("Check if executing user is authed.");
		command_addadmin = new Command("addadmin", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Nick")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String newOpNick = this.argumentParser.getArgument("Nick");
				User newOp = event.getBot().getUserChannelDao().getUser(newOpNick);
				if (!newOp.isVerified()) {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendMessage(target, "User " + newOpNick + " is not a registered user.", nick);
					return null;
				}
				String nsRegistration = Account.getAccount(newOpNick, event);
				IRCBot.getInstance().getOps().add(nsRegistration);
				PreparedStatement addOp = Database.getPreparedStatement("addOp");
				addOp.setString(1, nsRegistration);
				addOp.executeUpdate();
				Helper.sendMessage(target, "User " + newOpNick + " (" + nsRegistration + ") added to list.", nick);
				return new CommandChainStateObject();
			}
		};
		command_addadmin.setHelpText("Add a new admin.");
		command_time_test = new Command("timetest", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.INTEGER, "Amount"), new CommandArgument(ArgumentTypes.STRING, "Unit"))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String amount = this.argumentParser.getArgument("Amount");
				String unit = this.argumentParser.getArgument("Unit");
				if (unit.equals("ms"))
					Helper.sendMessage(target, Helper.timeString(Helper.parseMilliseconds(Long.parseLong(amount))));
				else
					Helper.sendMessage(target, Helper.timeString(Helper.parseSeconds(Long.parseLong(amount))));
				return new CommandChainStateObject();
			}
		};
		command_time_test.setHelpText("This just takes a number of seconds or milliseconds (pass `ms` as the second argument) and outputs a formatted time string such as `1 hour, 5 minutes and 4 seconds.");
		command_whatami = new Command("whatami") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String rank = Permissions.getRank(event);
				System.out.println("Rank: " + rank);
				if (rank != "")
					Helper.sendMessage(target, "You are '" + rank + "'");
				else
					Helper.sendMessage(target, "You are nothing! NOTHING!");
				return new CommandChainStateObject();
			}
		};
		command_whatami.setHelpText("Returns the rank of the executing users rank if any.");
		command_ami = new Command("ami", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Something"))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (nick.equals(this.argumentParser.getArgument("Something")))
					Helper.sendMessage(target, "Yes you are.");
				else
					Helper.sendMessage(target, "No, you are not '" + params + "', you are '" + nick + "'");
				return new CommandChainStateObject();
			}
		};
		command_debug = new Command("debug") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				if (IRCBot.isDebug)
					Helper.sendMessage(target, "Debug mode is ENABLED");
				else
					Helper.sendMessage(target, "Debug mode is DISABLED");
				return new CommandChainStateObject(CommandChainState.FINISHED);
			}
		};
		IRCBot.registerCommand(command_prefix);
		IRCBot.registerCommand(command_join);
		IRCBot.registerCommand(command_part);
		IRCBot.registerCommand(command_shutdown);
		IRCBot.registerCommand(command_cycle);
		IRCBot.registerCommand(command_raw);
		IRCBot.registerCommand(command_chnick);
		IRCBot.registerCommand(command_hashcount);
		IRCBot.registerCommand(command_usercount);
		IRCBot.registerCommand(command_authcount);
		IRCBot.registerCommand(command_flushhash);
		IRCBot.registerCommand(command_flushauth);
		IRCBot.registerCommand(command_ignore);
		IRCBot.registerCommand(command_unignore);
		IRCBot.registerCommand(command_ignorelist);
		IRCBot.registerCommand(command_load);
		IRCBot.registerCommand(command_commands);
		IRCBot.registerCommand(command_charset);
		IRCBot.registerCommand(command_ram);
		IRCBot.registerCommand(command_restart);
		IRCBot.registerCommand(command_test);
		IRCBot.registerCommand(command_listadmins);
		IRCBot.registerCommand(command_help);
		IRCBot.registerCommand(command_syntax);
		IRCBot.registerCommand(command_authed);
		IRCBot.registerCommand(command_addadmin);
		IRCBot.registerCommand(command_time_test);
		IRCBot.registerCommand(command_whatami);
		IRCBot.registerCommand(command_ami);
		IRCBot.registerCommand(command_debug);
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String[] copyOfRange) {
		ArrayList<String> terms = new ArrayList<>();
		terms.add(": prefix");
		terms.add(", prefix");
		terms.add(" prefix");
		terms.add(": commandchar");
		terms.add(", commandchar");
		terms.add(" commandchar");
		terms.add(": commandprefix");
		terms.add(", commandprefix");
		terms.add(" commandprefix");
		String botNick = IRCBot.ournick.toLowerCase();
		String message = event.getMessage().toLowerCase();
		for (String term : terms) {
			if (message.startsWith(botNick + term)) {
				event.respond(Config.commandprefix);
			}
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

	public void restart(String target) throws URISyntaxException, IOException, Exception {
		relaunch(target);
	}

	private static void relaunch(String target) throws InterruptedException, UnsupportedEncodingException {

		String command = "/" + FilenameUtils.getPath(IRCBot.getThisJarFile().getAbsolutePath()) + "restart.sh";
		Process p;
		try {
			Database.storeJsonData("restartFlag", target);
			Helper.sendMessage(target,"Restart initiated!");
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private final Set<String> channelsToJoin = new HashSet<>(Config.config.getAutoJoinChannels().keySet());


	@Override
	public void onJoin(JoinEvent event) {
		// Check if the bot is the one joining
		if (event.getUser().getNick().equals(event.getBot().getNick())) {
			String channel = event.getChannel().getName();
			channelsToJoin.remove(channel);
			System.out.println("Joined channel: " + channel);
			//event.getBot().getLogger().info("Joined channel: " + channel);

			// Check if all channels are joined
			if (channelsToJoin.isEmpty()) {
				//event.getBot().getLogger().info("All channels joined!");
				System.out.println("All channels joined!");
				// Add your logic here when all channels are joined
				String restartFlag = "false";
				try {
					IRCBot.log.info("Checking restartFlag");
					restartFlag = Database.getJsonData("restartFlag");
				} catch (Exception e) {
					IRCBot.log.info("Error fetching restartFlag: " + e.getMessage(), e);
				}
				if (!restartFlag.equals("false")) { // Use .equals() for string comparison
					try {
						Database.storeJsonData("restartFlag", "false");
						IRCBot.log.info("Trying to set restartFlag = false");
					} catch (Exception e) {
						IRCBot.log.info("Error setting restartFlag: " + e.getMessage(), e);
					}
					Helper.sendMessage(restartFlag, "Restart complete!");
					IRCBot.log.info("Restart complete");
				}
			}
		}
	}
}
