package pcl.lc.irc.entryClasses;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@SuppressWarnings("FieldCanBeLocal")
public class Command {
	public static final long INVALID_COMMAND = -1;
	public static final long IGNORED = -2;
	public static final long NO_PERMISSION = -3;
	public static final long DISABLED = -4;

	String command;
	String className;
	CommandRateLimit rateLimit;
	public CommandArgumentParser argumentParser;
	ArrayList<String> aliases;
	ArrayList<String> aliasesFixedArguments;
	ArrayList<Command> subCommands;
	public ArrayList<Command> parentCommands;
	boolean isEnabled;
	String minRank;
	String helpText;
	SyntaxGroup syntax;
	String actualCommand;
	String commandChain;

	public String callingRelay = null;

	public Command(String command) {
		this(command, null, null, true, Permissions.EVERYONE);
	}

	public Command(String command, CommandArgumentParser argumentParser) {
		this(command, argumentParser, null, true, Permissions.EVERYONE);
	}

	public Command(String command, CommandArgumentParser argumentParser, CommandRateLimit rateLimit) {
		this(command, argumentParser, rateLimit, true, Permissions.EVERYONE);
	}

	public Command(String command, CommandArgumentParser argumentParser, CommandRateLimit rateLimit, String minRank) {
		this(command, argumentParser, rateLimit, true, minRank);
	}

	public Command(String command, CommandArgumentParser argumentParser, String minRank) {
		this(command, argumentParser, null, true, minRank);
	}

	public Command(String command, CommandRateLimit rateLimit) {
		this(command, null, rateLimit, true, Permissions.EVERYONE);
	}

	public Command(String command, CommandRateLimit rateLimit, String minRank) {
		this(command, null, rateLimit, true, minRank);
	}

	public Command(String command, CommandRateLimit rateLimit, boolean isEnabled, String minRank) {
		this(command, null, rateLimit, isEnabled, minRank);
	}

	public Command(String command, String minRank) {
		this(command, null, null, true, minRank);
	}

	public Command(String command, CommandArgumentParser argumentParser, CommandRateLimit rateLimit, boolean isEnabled, String minRank) {
		this.command = command;
		this.className = Thread.currentThread().getStackTrace()[2].getClassName();
		this.rateLimit = rateLimit;
		this.argumentParser = argumentParser;
		this.aliases = new ArrayList<>();
		this.aliasesFixedArguments = new ArrayList<>();
		this.subCommands = new ArrayList<>();
		this.parentCommands = new ArrayList<>();
		this.isEnabled = isEnabled;
		this.minRank = minRank;
		this.helpText = "";
		this.commandChain = "";
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public String getHelpText() {
		return this.helpText;
	}

	public void setActualCommand(String actualCommand) {
		this.actualCommand = actualCommand;
	}

	public void setPermissionLevel(String minRank) {
		this.minRank = minRank;
	}

	public String getPermissionLevel() {
		return this.minRank;
	}

	public String getActualCommand() {
		return this.actualCommand;
	}

	public void disable() {
		this.isEnabled = false;
	}

	public void enable() {
		this.isEnabled = true;
	}

	public void toggleEnabled() {
		this.isEnabled = !this.isEnabled;
	}

	public String getCommand() {
		return this.command;
	}

	public String getClassName() {
		return this.className;
	}

	public CommandRateLimit getRateLimit() {
		return this.rateLimit;
	}

	public long getLastExecution(String nick) {
		return this.rateLimit.getLastExecution(nick);
	}

	public void setLastExecution(String nick, Integer lastExecution) {
		this.rateLimit.setLastExecution(nick, lastExecution);
	}

	public void updateLastExecution(String nick) {
		if (this.rateLimit != null)
			this.rateLimit.updateLastExecution(nick);
	}

	public long shouldExecute(String command, GenericMessageEvent event) {
		return shouldExecute(command, event, null);
	}

	/**
	 * Tests if a command should be executed based on a number of factors
	 * If nick is passed also tests if user is ignored
	 * Returns 0 if the command should be executed
	 * Returns non-zero on failure
	 * If return is greater than 0 it's the number of seconds remaining until the rate limit expires.
	 * @param command String
	 * @param nick String Optional
	 * @return int
	 */
	public long shouldExecute(String command, GenericMessageEvent event, String nick) {
		String prefix = "";
		if (this.parentCommands.size() == 0)
			prefix = Config.commandprefix;
		if (!command.toLowerCase().equals(prefix + this.command.toLowerCase()) && !hasAlias(command))
			return INVALID_COMMAND;
		if (!this.isEnabled)
			return DISABLED;
		if (!Permissions.hasPermission(IRCBot.bot, event, this.minRank))
			return NO_PERMISSION;
		if (nick != null && IRCBot.isIgnored(nick))
			return IGNORED;
		if (this.rateLimit == null)
			return 0;
		return this.rateLimit.getHeatValue(nick);
	}

	public boolean shouldExecuteBool(String command, GenericMessageEvent event) {
		return shouldExecuteBool(command, event, null);
	}

	private boolean shouldExecuteBool(String command, GenericMessageEvent event, String nick) {
		return shouldExecute(command, event, nick) == 0;
	}

	/**
	 * Returns a string containing the reason the command could not be executed based on shouldExecutes return code
	 * @param shouldExecuteResult long
	 * @return String
	 */
	private String getCannotExecuteReason(long shouldExecuteResult) {
		if (shouldExecuteResult > 0)
			return this.rateLimit.getFailMessage(shouldExecuteResult);
		else if (shouldExecuteResult == INVALID_COMMAND) //This should never happen since a command that isn't registered is ignored
			return "";
		else if (shouldExecuteResult == IGNORED)
			return "I'm ignoring you. You know what you did.";
		else if (shouldExecuteResult == NO_PERMISSION)
			return "You do not have sufficient privileges to use this command.";
		else if (shouldExecuteResult == DISABLED)
			return "This command is not enabled.";
		return "";
	}

	public void registerAlias(String alias) {
		registerAlias(alias, "");
	}

	public void registerAlias(String alias, ArrayList<String> forcedArgs) {
		String args = "";
		for (String arg : forcedArgs)
			args += arg + " ";
		args = args.trim();
		registerAlias(alias, args);
	}

	public void registerAlias(String alias, String forcedArgs) {
		if (!this.aliases.contains(alias)){
			this.aliases.add(alias);
			this.aliasesFixedArguments.add(forcedArgs);
		}
	}

	public void unregisterAlias(String alias) {
		if (this.aliases.contains(alias)) {
			this.aliasesFixedArguments.remove(this.aliases.indexOf(alias));
			this.aliases.remove(alias);
		}
	}

	public boolean hasAlias(String alias) {
		return this.aliases.contains(alias.replaceFirst(Pattern.quote(Config.commandprefix), ""));
	}

	/**
	 * @param alias The alias to get the forced argument of
	 * @return Returns the forced argument as a string, or an empty string if alias has no forced argument. Returns null if alias does not exist.
	 */
	public String getAliasForcedArgument(String alias) {
		if (hasAlias(alias)) {
			return this.aliasesFixedArguments.get(this.aliases.indexOf(alias));
		}
		return null;
	}

	public ArrayList<String> getAliases() {
		return this.aliases;
	}

	public ArrayList<String> getAliasesDisplay() {
		ArrayList<String> aliases = new ArrayList<>();
		for (String alias : this.aliases) {
			String parent = "";
			if (this.parentCommands.size() > 0)
				parent = this.parentCommands.get(0).command + " ";
			aliases.add(Config.commandprefix + parent + alias);
		}
		for (Command cmd : this.parentCommands) {
			for (int i = 0; i < cmd.aliases.size(); i++) {
				if (cmd.aliasesFixedArguments.get(i).equals(this.command))
					aliases.add(Config.commandprefix + cmd.aliases.get(i));
			}
		}
		return aliases;
	}

	public void registerSubCommand(Command command) {
		if (!this.subCommands.contains(command)) {
			command.parentCommands.add(this);
			this.subCommands.add(command);
		}
	}

	public void unregisterSubCommand(Command command) {
		if (this.subCommands.contains(command)) {
			command.parentCommands.remove(this);
			this.subCommands.remove(command);
		}
	}

	public boolean hasSubCommand(String subCommand) {
		for (Command command : this.subCommands) {
			if (command.getCommand() == subCommand)
				return true;
		}
		return false;
	}

	public String getSubCommandsAsString() {
		return getSubCommandsAsString(false);
	}

	private String getSubCommandsAsString(boolean includeAliases) {
		if (this.subCommands.size() > 0) {
			String list = "";
			for (Command command : this.subCommands) {
				list += ", " + command.getCommand();
				if (includeAliases) {
					String aliases = "";
					for (String alias : command.aliases) {
						aliases += alias + ", ";
					}
					aliases = aliases.replaceAll(", $", "");
					if (aliases != "")
						list += " (" + aliases + ")";
				}
			}
			return list.replaceAll("^, ", "");
		}
		else
			return "No registered sub-commands.";
	}

	public ArrayList<Command> getSubCommands() {
		return this.subCommands;
	}

	protected String trySubCommandsMessage(ArrayList<String> params) {
		if (params.size() > 0)
			return trySubCommandsMessage(params.get(0));
		else
			return "Must specify sub-command. (Try: " + this.getSubCommandsAsString(true) + ")";
	}

	protected String trySubCommandsMessage(String param) {
		return "Unknown sub-command '" + param + "' (Try: " + this.getSubCommandsAsString(true) + ")";
	}

	public CommandChainStateObject tryExecute(String command, String nick, String target, GenericMessageEvent event, String[] params) throws Exception {
		return tryExecute(command, nick, target, event, new ArrayList<>(Arrays.asList(params)), false);
	}
	public CommandChainStateObject tryExecute(String command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
		return tryExecute(command, nick, target, event, params, false);
	}
	public CommandChainStateObject tryExecute(String command, String nick, String target, GenericMessageEvent event, String[] params, boolean ignore_sub_commands) throws Exception {
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(params));
		return tryExecute(command, nick, target, event, arguments, ignore_sub_commands);
	}

	public CommandChainStateObject tryExecute(String command, String nick, String target, GenericMessageEvent event, ArrayList<String> params, boolean ignore_sub_commands) throws Exception {
		if (this.argumentParser != null)
			this.argumentParser.target = target;
//		System.out.println("tryExecute: " + command);
		long shouldExecute = this.shouldExecute(command, event, nick);
		if (shouldExecute == INVALID_COMMAND) { //Command does not match, ignore
//			System.out.println("Error when attempting to execute '" + this.command + "'. Doesn't match '" + command + "'");
			return new CommandChainStateObject(CommandChainState.CONTINUE);
		} else if (shouldExecute == 0 || (this.rateLimit != null && !this.rateLimit.getIgnorePermissions() && Permissions.hasPermission(IRCBot.bot, event, Permissions.ADMIN))) {
			this.actualCommand = command.replace(Config.commandprefix, "");
			int aliasIndex = aliases.indexOf(command.replaceFirst(Pattern.quote(Config.commandprefix), ""));
			if (aliasIndex != -1) {
				ArrayList<String> forcedParams = new ArrayList<>();
				if (!this.aliasesFixedArguments.get(aliasIndex).isEmpty())
					forcedParams.addAll(Arrays.asList(this.aliasesFixedArguments.get(aliasIndex).split(" ")));
				forcedParams.addAll(params);
				params = forcedParams;
			}
			this.updateLastExecution(nick);
			if (!ignore_sub_commands && params.size() > 0) {
				String firstParam = params.get(0);
				for (Command sub : this.subCommands) {
					ArrayList<String> subParams;
					if (params.size() > 1)
						subParams = new ArrayList<>(params.subList(1, params.size()));
					else
						subParams = new ArrayList<>();
					sub.commandChain = this.commandChain + this.command + " ";
					CommandChainStateObject state = sub.tryExecute(firstParam, nick, target, event, subParams, false);
					if (state.state != CommandChainState.CONTINUE)
						return state;
				}
			}
			String paramError = this.onInvalidArguments(params);
			if (paramError != null) {
				Helper.sendMessage(target, paramError, nick);
				return new CommandChainStateObject(CommandChainState.ERROR, paramError);
			}
			CommandChainStateObject state;
			state = this.onExecuteSuccess(this, nick, target, event, params.toArray(new String[]{}));
			if (state.state != CommandChainState.CONTINUE)
				return state;
			state = this.onExecuteSuccess(this, nick, target, event, params);
			if (state.state != CommandChainState.CONTINUE)
				return state;
			String message = String.join(" ", params);
			message = message.replaceAll("^\\s+", "");
			state = this.onExecuteSuccess(this, nick, target, event, message);
			return state;
		} else {
			return this.onExecuteFail(this, nick, target, shouldExecute);
		}
	}

	public void forceExecute(String nick, String target, GenericMessageEvent event, String[] params) throws Exception { forceExecute(nick, target, event, params, false); }
	public void forceExecute(String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception { forceExecute(nick, target, event, params, false);}
	public void forceExecute(String nick, String target, GenericMessageEvent event, String[] params, boolean ignore_sub_commands) throws Exception {
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(params));
		forceExecute(nick, target, event, arguments, ignore_sub_commands);
	}
	public void forceExecute(String nick, String target, GenericMessageEvent event, ArrayList<String> params, boolean ignore_sub_commands) throws Exception {
		this.onExecuteSuccess(this, nick, target, event, params.toArray(new String[] {}));
		this.onExecuteSuccess(this, nick, target, event, params);
		String message = "";
		for (String aCopyOfRange : params)
		{
			message = message + " " + aCopyOfRange;
		}
		//message = message.trim();
		message = message.replaceAll("^\\s+", "");
		this.onExecuteSuccess(this, nick, target, event, message);
	}

	public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String[] params) {
		System.out.println("Called default onExecuteSuccess (String[])");
		return new CommandChainStateObject(CommandChainState.CONTINUE);
	}
	public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
		System.out.println("Called default onExecuteSuccess (String)");
		return new CommandChainStateObject(CommandChainState.CONTINUE);
	}
	public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
		System.out.println("Called default onExecuteSuccess (ArrayList<String>)");
		return new CommandChainStateObject(CommandChainState.CONTINUE);
	}
	public CommandChainStateObject onExecuteFail(Command command, String nick, String target, long timeout) {
		Helper.sendNotice(nick, getCannotExecuteReason(timeout), this.callingRelay);
		return new CommandChainStateObject(CommandChainState.ERROR, getCannotExecuteReason(timeout));
	}

	@Override
	public String toString() {
		return "'" + command + "' aliases: " + getAliases().toString();
	}

	/**
	 * Search for a command by name or alias
	 * @param command Command or alias
	 * @return Returns the command if found or null otherwise
	 */
	public static Command findCommand(String command) {
		AtomicReference<Command> ret = new AtomicReference<>();
		IRCBot.commands.forEach((k,v) -> {
			if (k.equals(command) || v.hasAlias(command))
				ret.set(v);
		});
		return ret.get();
	}

	/**
	 * Override to provide a custom message when invalid or no parameters are provided (call parent at the end to avoid having to re-implement the parameter checking)
	 * @param params An ArrayList of the parameters provided to the command
	 * @return An error message that is printed to the channel of origin, prefixed with the executing user's name and interrupts the command, or null on no error, continuing the command.
	 */
	public String onInvalidArguments(ArrayList<String> params) {
		if (this.argumentParser != null) {
			if (params.size() > 0 && params.get(0).equals("syntax"))
				return Config.commandprefix + this.commandChain + this.command + " " + this.argumentParser.getArgumentSyntax();
			int arguments = this.argumentParser.parseArguments(params);
			if (!this.argumentParser.validateArguments(arguments)) {
				return "Invalid arguments. " + Config.commandprefix + this.commandChain + this.command + " " + this.argumentParser.getArgumentSyntax();
			}
		} else if (params.size() > 0 && params.get(0).equals("syntax")) {
			return "This command has no argument syntax defined.";
		}
		return null;
	}
}
