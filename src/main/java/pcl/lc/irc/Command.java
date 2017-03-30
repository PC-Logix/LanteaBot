package pcl.lc.irc;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.utils.Helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("FieldCanBeLocal")
public class Command {
	private static long INVALID_COMMAND = -1;
	private static long IGNORED = -2;
	public static long NO_PERMISSION = -3;
	private static long DISABLED = -4;

	private String command;
	private String className;
	private Integer rateLimit;
	private long lastExecution;
	private ArrayList<String> aliases;
	private ArrayList<Command> subCommands;
	private boolean isSubCommand;
	private boolean isEnabled;
	private int minPermissionLevel;
	private String helpText;

	public Command(String command) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), 0, false, true, 0);
	}

	public Command(String command, Integer rateLimit) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, false, true, 0);
	}

	public Command(String command, Integer rateLimit, boolean isSubCommand) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, isSubCommand, true, 0);
	}

	public Command(String command, Integer rateLimit, int minPermissionLevel) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, false, true, minPermissionLevel);
	}

	public Command(String command, Integer rateLimit, boolean isSubCommand, boolean isEnabled, int minPermissionLevel) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, isSubCommand, isEnabled, minPermissionLevel);
	}

	public Command(String command, boolean isSubCommand) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), 0, isSubCommand, true, 0);
	}

	public Command(String command, String className) {
		this(command, className, 0, false, true, 0);
	}

	public Command(String command, String className, Integer rateLimit, boolean isSubCommand, boolean isEnabled, int minPermissionLevel) {
		this.command = command;
		this.className = className;
		this.rateLimit = rateLimit;
		this.lastExecution = 0;
		this.aliases = new ArrayList<>();
		this.subCommands = new ArrayList<>();
		this.isSubCommand = isSubCommand;
		this.isEnabled = isEnabled;
		this.minPermissionLevel = minPermissionLevel;
		this.helpText = "";
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public String getHelpText() {
		return this.helpText;
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

	String getClassName() {
		return this.className;
	}

	public Integer getRateLimit() {
		return this.rateLimit;
	}

	public long getLastExecution() {
		return this.lastExecution;
	}

	public void setLastExecution(Integer lastExecution) {
		this.lastExecution = lastExecution;
	}

	public void updateLastExecution() {
		this.lastExecution = new Timestamp(System.currentTimeMillis()).getTime();
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
		if (!this.isSubCommand)
			prefix = Config.commandprefix;

		if (!this.isEnabled)
			return DISABLED;
		if (!command.toLowerCase().equals(prefix + this.command.toLowerCase()) && !hasAlias(command))
			return INVALID_COMMAND;
		if (!Permissions.hasPermission(IRCBot.bot, event, this.minPermissionLevel))
			return NO_PERMISSION;
		if (nick != null && IRCBot.isIgnored(nick))
			return IGNORED;
		if (this.rateLimit == 0)
			return 0;
		if (this.lastExecution == 0)
			return 0;
		long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
		long difference = timestamp - lastExecution;
		if (difference > this.rateLimit)
			return 0;
		return this.rateLimit - difference;
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
			return "I cannot execute this command right now. Wait " + Helper.timeString(Helper.parseMilliseconds(shouldExecuteResult)) + ".";
		else if (shouldExecuteResult == -1)
			return "";
		else if (shouldExecuteResult == -2)
			return "";
		else if (shouldExecuteResult == -3)
			return "You do not have sufficient privileges to use this command.";
		else if (shouldExecuteResult == -4)
			return "This command is not enabled.";
		return "";
	}

	public void registerAlias(String alias) {
		if (!this.aliases.contains(alias)){
			this.aliases.add(alias);
		}
	}

	public void unregisterAlias(String alias) {
		if (this.aliases.contains(alias)) {
			this.aliases.remove(alias);
		}
	}

	private boolean hasAlias(String alias) {
		return this.aliases.contains(alias.replace(Config.commandprefix, ""));
	}

	public void registerSubCommand(Command command) {
		if (!this.subCommands.contains(command)) {
			command.isSubCommand = true;
			this.subCommands.add(command);
		}
	}

	public void unregisterSubCommand(Command command) {
		if (this.subCommands.contains(command)) {
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

	protected String trySubCommandsMessage(ArrayList<String> params) {
		if (params.size() > 0)
			return trySubCommandsMessage(params.get(0));
		else
			return "Must specify sub-command. (Try: " + this.getSubCommandsAsString(true) + ")";
	}

	protected String trySubCommandsMessage(String param) {
		return "Unknown sub-command '" + param + "' (Try: " + this.getSubCommandsAsString(true) + ")";
	}

	public long tryExecute(String command, String nick, String target, GenericMessageEvent event, String[] params) { return tryExecute(command, nick, target, event, params, false);}
	public long tryExecute(String command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) { return tryExecute(command, nick, target, event, params, false);}
	public long tryExecute(String command, String nick, String target, GenericMessageEvent event, String[] params, boolean ignore_sub_commands) {
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(params));
		return tryExecute(command, nick, target, event, arguments, ignore_sub_commands);
	}

	public long tryExecute(String command, String nick, String target, GenericMessageEvent event, ArrayList<String> params, boolean ignore_sub_commands)
	{
		long shouldExecute = this.shouldExecute(command, (MessageEvent) event, nick);
		if (shouldExecute == -1) //Command does not match, ignore
			return 0;
		else if (shouldExecute == 0) {
			this.updateLastExecution();
			if (!ignore_sub_commands && params.size() > 0) {
				String firstParam = params.get(0);
				int executed = 0;
				for (Command sub : this.subCommands) {
					ArrayList<String> subParams;
					if (params.size() > 1)
						subParams = new ArrayList<>(params.subList(1, params.size()));
					else
						subParams = params;
					executed += sub.tryExecute(firstParam, nick, target, event, subParams, false);
				}
				if (executed > 0)
					return 1;
			}
			this.onExecuteSuccess(this, nick, target, event, params);
			String message = "";
			for (String aCopyOfRange : params)
			{
				message = message + " " + aCopyOfRange;
			}
			message = message.trim();
			this.onExecuteSuccess(this, nick, target, event, message);
			return 1;
		}
		else
			this.onExecuteFail(this, nick, target, shouldExecute);
		return 0;
	}

	public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {}
	public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {}
	public void onExecuteFail(Command command, String nick, String target, long timeout) {
		Helper.sendMessage(target, getCannotExecuteReason(timeout), nick);
	}
}
