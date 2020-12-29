package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.irc.entryClasses.CommandRateLimit;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Jiggle extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("jiggle", new CommandArgumentParser(0, new CommandArgument("Thing", "String"), new CommandArgument("Times", "Integer")), new CommandRateLimit(120, true, true)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String thing = this.argumentParser.getArgument("Thing");
				int number = this.argumentParser.getInt("Times");
				if (thing != null) {
					if (number == Integer.MIN_VALUE)
						Helper.sendAction(target, "jiggles " + thing);
					else
						Helper.sendAction(target, "jiggles " + thing + " " + number + " times");
				}
				else
					Helper.sendAction(target, "jiggles");
			}
		};
		local_command.setHelpText("Jiggle");
	}
}
