package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Bap extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("bap", new CommandArgumentParser(0, new CommandArgument("Target", ArgumentTypes.STRING), new CommandArgument("Item", ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String bapTarget = this.argumentParser.getArgument("Target");
				String with = this.argumentParser.getArgument("Item");
				if (bapTarget == null)
					Helper.sendWorldAction(target, nick + " flails at the darkness");
				else {
					if (Helper.doInteractWith(params)) {
						Item item = null;
						if (with == null)
							item = Inventory.getRandomItem(false);
						else {
							try {
								item = new Item(with, false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (item != null)
							if (nick.equals(bapTarget))
								Helper.sendMessage(target, nick + " baps themselves with " + item.getName(true) + "!");
							else
								Helper.sendMessage(target, nick + " baps " + bapTarget + " with " + item.getName(true) + "!");
						else {
							if (nick.equals(bapTarget))
								Helper.sendMessage(target, nick + " baps themselves!");
							else
								Helper.sendMessage(target, nick + " baps " + bapTarget + "!");
						}
					} else {
						Helper.sendAction(target, "smacks " + nick + "!");
					}
				}
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("Baps someone harmlessly without doing damage! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <item>] If [with <item>] is omitted tries to use a random item from the inventory.");
	}
}
