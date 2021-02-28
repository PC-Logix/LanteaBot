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
		for (int i : message.codePoints().toArray()) {
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
		local_command = new Command("rainbow", new CommandArgumentParser(0, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str == null || str.equals(""))
					str = "Rainbows!";
				System.out.println("Rainbow: '" + str + "'");
				Helper.sendMessage(target, makeRainbow(str), nick, true);
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("Replies with a rainbow version of the supplied text");
		IRCBot.registerCommand(local_command);
	}
}
