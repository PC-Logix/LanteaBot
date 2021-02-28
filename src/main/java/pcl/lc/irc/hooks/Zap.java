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
 */
@SuppressWarnings("rawtypes")
public class Zap extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("zap", new CommandArgumentParser(0, new CommandArgument("Nick", ArgumentTypes.STRING), new CommandArgument("Item", ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String zapTarget = this.argumentParser.getArgument("Nick");
				if (zapTarget == null)
					Helper.sendAction(target, nick + " makes some sparks");
				else {
					String with = this.argumentParser.getArgument("Item");

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
						String dmgString;
						if (with != null && with.equals("nothing"))
							item = null;

						if (item == null)
							dmgString = "1d6 damage";
						else
							dmgString = "1d" + (item.getDiceSizeFromItemName() + 2) + " damage";
						dmgString = DiceRoll.rollDiceInString(dmgString, true);

						if (item != null)
							if (nick.equals(zapTarget))
								Helper.sendMessage(target, nick + " zaps themselves using " + item.getName(true) + " as a conductor for " + dmgString + "!");
							else
								Helper.sendMessage(target, nick + " zaps " + zapTarget + " using " + item.getName(true) + " as a conductor for " + dmgString + "!");
						else {
							if (nick.equals(zapTarget))
								Helper.sendMessage(target, nick + " zaps themselves for " + dmgString + "!");
							else
								Helper.sendMessage(target, nick + " zaps " + zapTarget + " for " + dmgString + "!");
						}
					} else {
						Helper.sendAction(target, "zaps " + nick + "!");
					}
				}
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("Shocking! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <item>] If [with <item>] is omitted tries to use a random item from the inventory.");
	}
}
