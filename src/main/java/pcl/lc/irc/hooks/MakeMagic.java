package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class MakeMagic extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("makemagic", new CommandArgumentParser(1, new CommandArgument("Item", ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String item = this.argumentParser.getArgument("Item");
				String[] prefixes = Helper.solvePrefixes(item);
				if (prefixes != null)
					Helper.sendMessage(target, String.join(" magic ", prefixes));
				else
					Helper.sendMessage(target, "Seems I'm out of mana...");
			}
		};
		local_command.setHelpText("Magic?");
	}
}
