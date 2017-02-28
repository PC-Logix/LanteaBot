package pcl.lc.irc;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Command {
	String command;
	String className;
	Integer rateLimit;
	long lastExecution;
	ArrayList<String> aliases;

	public Command(String command, Integer rateLimit) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), rateLimit);
	}

	public Command(String command) {
		this(command, Thread.currentThread().getStackTrace()[2].getClassName(), 0);
	}

	public Command(String command, String className) {
		this(command, className, 0);
	}

	public Command(String command, String className, Integer rateLimit) {
		this.command = command;
		this.className = className;
		this.rateLimit = rateLimit;
		this.lastExecution = 0;
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

	public int shouldExecute(String command, String nick) {
		if (!command.equals(Config.commandprefix + this.command) && !hasAlias(command))
			return -1;
		if (nick != null && IRCBot.isIgnored(nick))
			return -1;
		if (this.rateLimit == 0)
			return 0;
		if (this.lastExecution == 0)
			return 0;
		long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
		long difference = timestamp - lastExecution;
		System.out.println(timestamp + " - " + lastExecution + " = " + difference + " > " + (this.rateLimit * 1000));
		if (difference > (this.rateLimit * 1000))
			return 0;
		return this.rateLimit - ((int) difference / 1000);
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
}
