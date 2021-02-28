package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;

import java.util.*;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Jumble extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("jumble", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				ArrayList<String> words = new ArrayList<>(Arrays.asList(str.split(" ")));
				Collections.shuffle(words);

				str = "";
				for (String word : words) {
					str += word + " ";
				}
				str = str.trim();
				Helper.sendMessage(target, str);
				return CommandChainState.FINISHED;
			}
		};
		local_command.registerAlias("yoda");
		local_command.setHelpText("It jumbles things. Try it. You'll be hooked.");
	}
}
