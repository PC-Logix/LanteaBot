/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Hiss extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("hiss", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str == null || str.equals("")) {
					Helper.sendMessage(target, "Snek?", nick);
				} else {
					Helper.sendMessage(target, str.replaceAll("s", "ss").replaceAll("S", "SS"));
				}
				return CommandChainState.FINISHED;
			}
		};
		local_command.registerAlias("snake");
		local_command.registerAlias("snek");
		local_command.setHelpText("Sneks the text");
	}
}
