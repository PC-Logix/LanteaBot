package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.DiceRoll;
import pcl.lc.utils.Helper;
import pcl.lc.irc.entryClasses.Item;

/**
 * @author Forecaster
 *
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
		local_command = new Command("zap") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.length() == 0)
					Helper.sendAction(target, nick + " makes some sparks");
				else {
					String[] split = params.split(" with ", 2);
					String zapTarget = split[0].trim();
					String with = null;
					if (split.length > 1)
						with = split[1].trim();

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
			}
		};
		local_command.setHelpText("Shocking! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <item>] If [with <item>] is omitted tries to use a random item from the inventory.");
	}
}
