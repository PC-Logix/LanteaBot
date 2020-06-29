/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.collect.Lists;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Rainbow extends AbstractListener {
	private Command local_command;

	public String makeRainbow(String message) {
		Integer rainbow = 0;
		String messageOut = "";
		for(int i : message.codePoints().toArray()){
			char[] ch = Character.toChars(i);
			String c = new String(ch);
			if (rainbow == 0) {
				messageOut = messageOut + Colors.RED + c;
			} else if (rainbow == 1) {
				messageOut = messageOut + Colors.OLIVE + c;
			} else if (rainbow == 2) {
				messageOut = messageOut + Colors.YELLOW + c;
			} else if (rainbow == 3) {
				messageOut = messageOut + Colors.GREEN + c;
			} else if (rainbow == 4) {
				messageOut = messageOut + Colors.BLUE + c;
			} else if (rainbow == 5) {
				messageOut = messageOut + Colors.DARK_BLUE + c;
			} else if (rainbow == 6) {
				messageOut = messageOut + Colors.MAGENTA + c;
			}
			rainbow++;
			if (rainbow >= 6) {
				rainbow = 0;
			}
		}
		return messageOut;
	}

	@Override
	protected void initHook() {
		local_command = new Command("rainbow") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.equals("^")) {
		            List<Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
		            for (Entry<UUID, List<String>> entry : Lists.reverse(list)) {
		              if (entry.getValue().get(0).equals(target)) {
		                Helper.sendMessage(target, makeRainbow(entry.getValue().get(2)), nick);
		                return;
		              }
		            }
				} else {
					Helper.sendMessage(target, makeRainbow(params), nick);
				}
				//Helper.sendMessage(target, makeRainbow(params), nick);
			}
		}; local_command.setHelpText("Replies with a rainbow version of the supplied text");
		IRCBot.registerCommand(local_command);
	}
}
