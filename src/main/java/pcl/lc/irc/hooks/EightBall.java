package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings({ "rawtypes" })
public class EightBall extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("eightball") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if ((params.length() > 6 && params.matches(".*\\?$")) || params.equals("^")) {
					ArrayList<String> messages = new ArrayList<>();
					messages.add("Signs point to yes");
					messages.add("Without a doubt");
					messages.add("Reply hazy, try again");
					messages.add("Ask again later");
					messages.add("My reply is no");
					messages.add("Outlook not so good");
					messages.add("[ The Bowling ball doesn't answer ]");
					Helper.sendMessage(target, messages.get(Helper.getRandomInt(0, messages.size() - 1)), nick);
					return;
				}
				Helper.sendMessage(target, "I don't think that's a question...", nick);
			}
		}; local_command.setHelpText("Gives vague answers to all questions.");
		local_command.registerAlias("8ball");
		IRCBot.registerCommand(local_command);
	}
}
