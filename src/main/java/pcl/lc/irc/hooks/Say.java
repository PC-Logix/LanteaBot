package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
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
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("say", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Message"), new CommandArgument(ArgumentTypes.STRING, "Channel")), Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String message = this.argumentParser.getArgument("Message");
				String channel = this.argumentParser.getArgument("Channel");
				if (channel == null)
					Helper.sendMessage(target, message);
				else
					Helper.sendMessage(channel, message);
				return new CommandChainStateObject();
			}
		};
		local_command.setHelpText("Makes bot say thing");
	}
}
