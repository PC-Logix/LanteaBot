package pcl.lc.irc;

import pcl.lc.utils.Helper;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Command {
	String command;
	String className;
	Integer rateLimit;
	long lastExecution;
	ArrayList<String> aliases;
	ArrayList<Command> subCommands;
	boolean isSubCommand;

	public Command(String command, Integer rateLimit) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, false);
	}

	public Command(String command, Integer rateLimit, boolean isSubCommand) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit, isSubCommand);
	}

	public Command(String command) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), 0, false);
	}

	public Command(String command, boolean isSubCommand) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), 0, isSubCommand);
	}

	public Command(String command, String className) {
		this(command, className, 0, false);
	}

	public Command(String command, String className, Integer rateLimit, boolean isSubCommand) {
		this.command = command;
		this.className = className;
		this.rateLimit = rateLimit;
		this.lastExecution = 0;
		this.aliases = new ArrayList<>();
		this.subCommands = new ArrayList<>();
		this.isSubCommand = isSubCommand;
	}

	public String getCommand() {
		return this.command;
	}

	public String getClassName() {
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

	public int shouldExecute(String command) {
		return shouldExecute(command, null);
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
	public int shouldExecute(String command, String nick) {
		String prefix = "";
		if (!this.isSubCommand)
			prefix = Config.commandprefix;

		if (!command.equals(prefix + this.command) && !hasAlias(command))
			return -1;
		if (nick != null && IRCBot.isIgnored(nick))
			return -2;
		if (this.rateLimit == 0)
			return 0;
		if (this.lastExecution == 0)
			return 0;
		long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
		long difference = timestamp - lastExecution;
		if (difference > (this.rateLimit * 1000))
			return 0;
		return this.rateLimit - ((int) difference / 1000);
	}

	public boolean shouldExecuteBool(String command) {
		return shouldExecuteBool(command, null);
	}

	public boolean shouldExecuteBool(String command, String nick) {
		return shouldExecute(command, nick) == 0;
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

	public boolean hasAlias(String alias) {
		return this.aliases.contains(alias.replace(Config.commandprefix, ""));
	}

	public void registerSubCommand(Command command) {
		if (!this.subCommands.contains(command)) {
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

	public String getSubCommandsAsString(boolean includeAliases) {
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

	public String getCannotExecuteReason(long shouldExecuteResult) {
			if (shouldExecuteResult > 0)
				return "I cannot execute this command right now. Wait " + Helper.timeString(Helper.parse_seconds((int) shouldExecuteResult)) + ".";
			else if (shouldExecuteResult == -1)
				return "";
			else if (shouldExecuteResult == -2)
				return "";
			return null;
		}
	}
}
