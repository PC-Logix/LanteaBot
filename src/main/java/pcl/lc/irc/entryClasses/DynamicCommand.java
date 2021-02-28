package pcl.lc.irc.entryClasses;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.hooks.DynamicCommands;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;

public class DynamicCommand extends Command {
	public String content;

	public DynamicCommand(String command, String content) {
		super(command);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandArgumentParser argumentParser) {
		super(command, argumentParser);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandArgumentParser argumentParser, CommandRateLimit rateLimit) {
		super(command, argumentParser, rateLimit);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandArgumentParser argumentParser, CommandRateLimit rateLimit, String minRank) {
		super(command, argumentParser, rateLimit, minRank);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandArgumentParser argumentParser, String minRank) {
		super(command, argumentParser, minRank);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandRateLimit rateLimit) {
		super(command, rateLimit);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandRateLimit rateLimit, String minRank) {
		super(command, rateLimit, minRank);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandRateLimit rateLimit, boolean isEnabled, String minRank) {
		super(command, rateLimit, isEnabled, minRank);
		this.content = content;
	}

	public DynamicCommand(String command, String content, String minRank) {
		super(command, minRank);
		this.content = content;
	}

	public DynamicCommand(String command, String content, CommandArgumentParser argumentParser, CommandRateLimit rateLimit, boolean isEnabled, String minRank) {
		super(command, argumentParser, rateLimit, isEnabled, minRank);
		this.content = content;
	}

	@Override
	public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String[] params) {
		try {
			System.out.println("Executing dyn command (String)");
			DynamicCommands.parseDynCommand(this.actualCommand, nick, target, params);
		} catch (Exception e) {
			e.printStackTrace();
			Helper.sendMessage(target, "Something went wrong.", nick);
		}
		return CommandChainState.FINISHED;
	}
}
