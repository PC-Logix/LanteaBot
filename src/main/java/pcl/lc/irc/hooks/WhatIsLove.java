package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
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
		IRCBot.registerCommand(local_command);
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
		local_command.setHelpText("The answer to the most question");
	}
}
