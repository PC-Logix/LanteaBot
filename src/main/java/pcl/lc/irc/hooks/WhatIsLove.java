package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class WhatIsLove extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "The answer to the most question");
	}

	private void initCommands() {
		local_command = new Command("whatislove") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				int roll = Helper.getRandomInt(0,100);
				Helper.sendMessage(target, "Love is... " + Inventory.getRandomItem(true).getName() + ((roll < 25) ? ", with " + Inventory.getRandomItem(false).getName() + " on top!" : "!"), nick);
			}
		};
		local_command.registerAlias("loveis");
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
