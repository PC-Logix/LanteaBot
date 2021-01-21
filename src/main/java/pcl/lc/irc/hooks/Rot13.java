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
import pcl.lc.irc.entryClasses.ArgumentTypes;
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
public class Rot13 extends AbstractListener {
	private Command rot;

	@Override
	protected void initHook() {
		rot = new Command("rot13", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				Helper.sendMessage(target, rot13(Colors.removeFormattingAndColors(str)), nick);
			}
		};
		rot.setHelpText("Applies the ROT13 cipher to the supplied text");
		IRCBot.registerCommand(rot);
	}

	private static String rot13(String input) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if       (c >= 'a' && c <= 'm') c += 13;
			else if  (c >= 'A' && c <= 'M') c += 13;
			else if  (c >= 'n' && c <= 'z') c -= 13;
			else if  (c >= 'N' && c <= 'Z') c -= 13;
			sb.append(c);
		}
		return sb.toString();
	}
}
