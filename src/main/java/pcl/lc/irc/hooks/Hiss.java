/**
 * 
 */
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str == null || str.equals("")) {
					Helper.sendMessage(target, "Snek?", nick);
				} else {
					Helper.sendMessage(target, str.replaceAll("s", "ss").replaceAll("S", "SS"));
				}
			}
		};
		local_command.registerAlias("snake");
		local_command.registerAlias("snek");
		local_command.setHelpText("Sneks the text");
	}
}
