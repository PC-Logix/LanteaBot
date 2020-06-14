package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Random;

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

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
