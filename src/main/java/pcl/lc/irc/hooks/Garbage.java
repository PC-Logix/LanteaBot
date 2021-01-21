package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TablesOfRandomThings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Garbage extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("garbage", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String item = this.argumentParser.getArgument(0);
				if (item == null || item.equals("")) {
					Helper.sendAction(target, "kicks a can " + TablesOfRandomThings.getGarbageDisposal());
				} else {
					Helper.sendAction(target, "throws '" + item + "' " + TablesOfRandomThings.getGarbageDisposal() + ", it was never seen again.");
				}
			}
		};
		local_command.registerAlias("gb");
		local_command.setHelpText("It goes in the garbage");
	}
}
