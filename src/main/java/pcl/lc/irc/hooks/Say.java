package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Say extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Makes bot say thing");
	}

	private void initCommands() {
		local_command = new Command("say", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, params);
			}
		};
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
