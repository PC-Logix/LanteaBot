/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;

import java.util.HashMap;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Pet extends AbstractListener {
	private Command local_command;
	private HashMap<String, ActionType> actions;

	@Override
	protected void initHook() {
		actions = new HashMap<>();
		actions.put("pets", new ActionType("Petting", "Petting", "Pet", "Petted"));
		actions.put("brushes", new ActionType("Brushing", "Brushing", "Brush", "Brushed"));

		local_command = new Command("pet", new CommandArgumentParser(1, new CommandArgument("Nick", ArgumentTypes.STRING), new CommandArgument("Item", ArgumentTypes.STRING))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String petTarget = this.argumentParser.getArgument("Nick");
				String with = this.argumentParser.getArgument("Item");
				if (petTarget == null || petTarget.equals("")) {
					Helper.sendMessage(target, nick + " flails at nothingness...");
					return CommandChainState.FINISHED;
				} else {
					if (nick.equals(petTarget)) {
						Helper.sendMessage(target, "Don't pet yourself in public.", nick);
						return CommandChainState.FINISHED;
					}

					Item item;
					if (with != null)
						item = new Item(with, false);
					else
						item = Inventory.getRandomItem();

					String dust = "";
					if (item != null) {
						dust = item.decrementUses(false, true, true);
						if (!dust.equals(""))
							dust = " " + dust;
					}

					String[] keys = actions.keySet().toArray(new String[0]);
					ActionType petAction = actions.get(keys[Helper.getRandomInt(0, keys.length - 1)]);

//					DiceRoll roll = Helper.rollDice(Math.max(1, (item != null ? item.getUsesLeft() : 1) / 2) + "d4").getFirstGroupOrNull();

					DiceRollResult heal;
					if (item != null) {
						heal = item.getHealing();
						heal.bonus = DiceRollBonusCollection.getHealingItemBonus(item);
					} else
						heal = Item.getGenericRoll(1, 4, new DiceRollBonusCollection());
					String healString = heal.getResultString();
					if (healString == null)
						healString = "no hit points";
					else
						healString += " hit points";
					Helper.sendMessage(target, nick + " is " + petAction.actionNameIs.toLowerCase() + " " + petTarget + (item != null ? " with " + item.getName() : "") + ". " + petTarget + " regains " + healString + "!" + dust);
				}
				return CommandChainState.FINISHED;
			}
		};
		local_command.registerAlias("stroke");
		local_command.setHelpText("Give pets! Give hit points! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <item>] If [with <item>] is omitted tries to use a random item from the inventory.");
		IRCBot.registerCommand(local_command);
	}
}
