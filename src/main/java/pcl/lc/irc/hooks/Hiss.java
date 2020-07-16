/**
 * 
 */
package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
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
		IRCBot.registerCommand(local_command, "Don't step on snek!");
	}

	private void initCommands() {
		local_command = new Command("hiss") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = "";
				if (params.equals("")) {
					Helper.sendMessage(target, "Snek?", nick);
				} else {
					if (params.equals("^")) {
						List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
							if (entry.getValue().get(0).equals(target)) {
								if (entry.getValue().get(2).toLowerCase().contains("s")) {
									str = entry.getValue().get(2);
									break;
								}
							}
						}
					} else {
						str = params;
					}
					Helper.sendMessage(target, str.replaceAll("s", "ss").replaceAll("S", "SS"));
				}
			}
		}; local_command.setHelpText("Sneks the text");
		local_command.registerAlias("snake");
		local_command.registerAlias("snek");
	}
}