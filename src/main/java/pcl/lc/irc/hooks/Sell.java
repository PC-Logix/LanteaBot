package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Sell extends AbstractListener {
	private Command local_command;
	private ArrayList<String> strings;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);

		strings = new ArrayList<>();
		strings.add("New %s! Buy now! Only 99.99! ");
		strings.add("Buy the new %s now to enhance your life!");
		strings.add("%s is now in stock! Get it before it's gone! 88.99 plus tax!");
		strings.add("Tired of being tired? Buy %s now and then go to bed!");
		strings.add("Get the fantastic %s now while it's available! Only 99.50!");
		strings.add("Happy to be alive? %s will make you 300% happier! (Side effects include not getting happier)");
	}

	private void initCommands() {
		local_command = new Command("sell", new CommandArgumentParser(1, new CommandArgument("Item", "String"))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String string = strings.get(Helper.getRandomInt(0, strings.size() - 1));
				string = String.format(string, this.argumentParser.getArgument("Item"));
				Helper.sendMessage(target, string, nick);
			}
		};
		local_command.setHelpText("Returns a sales pitch for the given argument.");
	}
}
