package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
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
		local_command = new Command("eightball", new CommandArgumentParser(0, new CommandArgument("Question", ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String question = this.argumentParser.getArgument("Question");
				if (question != null && ((question.length() > 6 && question.matches(".*\\?$")) || question.equals("^"))) {
					ArrayList<String> messages = new ArrayList<>();
					messages.add("Signs point to yes");
					messages.add("Without a doubt");
					messages.add("Reply hazy, try again");
					messages.add("Ask again later");
					messages.add("My reply is no");
					messages.add("Outlook not so good");
					messages.add("*The Bowling ball doesn't answer");
					String msg = messages.get(Helper.getRandomInt(0, messages.size() - 1));
					if (msg.startsWith("*"))
						Helper.sendAction(target, msg.replaceFirst("\\*", ""));
					else
						Helper.sendMessage(target, msg, nick);
					return null;
				}
				Helper.sendMessage(target, "I don't think that's a question...", nick);
				return CommandChainState.FINISHED;
			}
		}; local_command.setHelpText("Gives vague answers to all questions.");
		local_command.registerAlias("8ball");
		IRCBot.registerCommand(local_command);
	}
}
