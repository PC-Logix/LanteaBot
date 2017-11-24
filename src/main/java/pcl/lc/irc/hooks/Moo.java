/**
 * 
 */
package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static pcl.lc.irc.hooks.Flip.flip;

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
		IRCBot.registerCommand(local_command, "What'choo loooking at moo?");
	}

	private void initCommands() {
		local_command = new Command("moo", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = "";
				if (params.equals("")) {
					Helper.sendMessage(target, "Moo?", nick);
				} else {
					if (params.equals("^")) {
						List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
							if (entry.getValue().get(0).equals(target)) {
								if (entry.getValue().get(2).toLowerCase().contains("o")) {
									str = entry.getValue().get(2);
									break;
								}
							}
						}
					} else {
						str = params;
					}
					str = str.replaceAll("u", "o").replaceAll("U", "O");
					Helper.sendMessage(target, str.replaceAll("o", "oo").replaceAll("O", "OO"), nick);
				}
			}
		}; local_command.setHelpText("Moos the text");
		IRCBot.registerCommand(local_command);
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
