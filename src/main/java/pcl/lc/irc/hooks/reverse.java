/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.collect.Lists;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
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
		reverse = new Command("reverse", new CommandArgumentParser(1, new CommandArgument("String"))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str.equals("^")) {
					List<Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
					for (Entry<UUID, List<String>> entry : Lists.reverse(list)) {
						if (entry.getValue().get(0).equals(target)) {
							Helper.sendMessage(target, new StringBuffer(Colors.removeFormattingAndColors(entry.getValue().get(2))).reverse().toString(), nick);
							return;
						}
					}
				} else {
					Helper.sendMessage(target, new StringBuffer(Colors.removeFormattingAndColors(str)).reverse().toString(), nick);
				}
			}
		};
		reverse.setHelpText("Reverses the supplied text");
		IRCBot.registerCommand(reverse);
	}
}