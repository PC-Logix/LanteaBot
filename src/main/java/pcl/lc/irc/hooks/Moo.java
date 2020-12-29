/**
 * 
 */
package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
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
		local_command = new Command("moo", new CommandArgumentParser(0, new CommandArgument("String"))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str == null) {
					Helper.sendMessage(target, "Moo?", nick);
				} else {
					if (str.equals("^")) {
						List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
							if (entry.getValue().get(0).equals(target)) {
								if (entry.getValue().get(2).toLowerCase().contains("o")) {
									str = entry.getValue().get(2);
									break;
								}
							}
						}
					}
					str = str.replaceAll("u", "o").replaceAll("U", "O");
					Helper.sendMessage(target, str.replaceAll("o", "oo").replaceAll("O", "OO"));
				}
			}
		};
		local_command.setHelpText("Moos the text");
	}
}
