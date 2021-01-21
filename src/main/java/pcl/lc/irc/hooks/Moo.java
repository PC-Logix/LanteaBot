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
public class Moo extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("moo", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str == null) {
					Helper.sendMessage(target, "Moo?", nick);
				} else {
					str = str.replaceAll("u", "o").replaceAll("U", "O");
					Helper.sendMessage(target, str.replaceAll("o", "oo").replaceAll("O", "OO"));
				}
			}
		};
		local_command.setHelpText("Moos the text");
	}
}
