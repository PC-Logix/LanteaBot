package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Map;

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
		local_command = new Command("aliases", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				boolean didTheThing = false;
				for (String param : params) {
					for (Map.Entry<String, Command> cmd : IRCBot.commands.entrySet()) {
						if (cmd.getKey().equals(param) || cmd.getValue().hasAlias(param)) {
							Helper.sendMessage(target, cmd.getValue().toString(), nick);
							didTheThing = true;
						}
					}
				}
				if (!didTheThing)
					Helper.sendMessage(target, "No commands found matching, or with alias matching argument" + (params.size() == 1 ? "" : "s") + ".");
			}
		};
		local_command.setHelpText("Get aliases for a command, or find the root command for an alias");
		local_command.registerAlias("alias");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
