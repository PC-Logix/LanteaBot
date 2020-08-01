package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.CommandRateLimit;
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
		local_command = new Command("jiggle", new CommandRateLimit(120, true)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				int number = 0;
				if (params.length() > 0) {
					try { number = Integer.parseInt(params); } catch (NumberFormatException ignored) {}
					if (number == 0)
						Helper.sendAction(target, "jiggles " + params);
					else
						Helper.sendAction(target, "jiggles " + number + " times");
				}
				else
					Helper.sendAction(target, "jiggles");
			}
		};
		local_command.setHelpText("Jiggle");
	}
}
