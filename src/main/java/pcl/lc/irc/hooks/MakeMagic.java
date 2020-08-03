package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
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
		local_command = new Command("makemagic") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String[] prefixes = Helper.solvePrefixes(params);
				if (prefixes != null)
					Helper.sendMessage(target, String.join(" magic ", prefixes ));
				else
					Helper.sendMessage(target, "Seems I'm out of mana...");
			}
		};
		local_command.setHelpText("Magic?");
	}
}
