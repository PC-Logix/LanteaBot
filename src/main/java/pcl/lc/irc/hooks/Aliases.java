package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandRateLimit;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Aliases extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("aliases", new CommandRateLimit(60)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Command cmd = Command.findCommand(params);
				if (cmd != null)
					Helper.sendMessage(target, cmd.toString(), nick);
				else
					Helper.sendMessage(target, "No command or alias found matching '" + params + "'", nick);
			}
		};
		local_command.setHelpText("Get aliases for a command, or find the root command for an alias. Syntax: " + Config.commandprefix + local_command.getCommand() + " <command or alias>");
		local_command.registerAlias("alias");
	}
}
