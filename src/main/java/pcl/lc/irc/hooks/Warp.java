package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TablesOfRandomThings;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Warp extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("warp") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, TablesOfRandomThings.getRandomWarpLocation());
				return new CommandChainStateObject();
			}
		};
		local_command.setHelpText("Go on a vacation! Or end up is a world made out of spaghetti...");
	}
}
