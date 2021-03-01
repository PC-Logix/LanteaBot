/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class reverse extends AbstractListener {
	private Command reverse;

	@Override
	protected void initHook() {
		reverse = new Command("reverse", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				Helper.sendMessage(target, new StringBuffer(Colors.removeFormattingAndColors(str)).reverse().toString(), nick);
				return new CommandChainStateObject();
			}
		};
		reverse.setHelpText("Reverses the supplied text");
		IRCBot.registerCommand(reverse);
	}
}