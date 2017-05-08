package pcl.lc.irc.hooks;

import java.util.Random;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

public class RandomChoice extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("choose", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String[] parts = params.split(" or ");
				int rnd = new Random().nextInt(parts.length);
				Helper.sendMessage(target, parts[rnd].trim(), nick);
			}
		}; local_command.setHelpText("Randomly picks a choice for you.");
		local_command.registerAlias("choice");
		local_command.registerAlias("pick");
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
