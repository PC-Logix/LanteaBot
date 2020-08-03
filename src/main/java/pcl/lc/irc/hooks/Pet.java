/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.ActionType;
import pcl.lc.irc.entryClasses.DiceRollBonusCollection;
import pcl.lc.irc.entryClasses.DiceRollResult;
import pcl.lc.irc.entryClasses.Item;
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

		local_command = new Command("pet") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.length() == 0) {
					Helper.sendMessage(target, nick + " flails at nothingness...");
					return;
				} else {
					String[] split = params.split(" with ", 2);
					String petTarget = split[0].trim();
					String with = null;
					if (split.length > 1)
						with = split[1].trim();

					if (nick.equals(petTarget)) {
						Helper.sendMessage(target,"Don't pet yourself in public.", nick);
						return;
					}

					Item item = null;
					try {
						if (with != null)
							item = new Item(with, false);
						else
							item = Inventory.getRandomItem();
					} catch (Exception e) {
						Helper.sendMessage(target, "I think something went wrong...");
						e.printStackTrace();
					}

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
			}
		};
		local_command.registerAlias("stroke");
		local_command.setHelpText("Give pets! Give hit points! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <item>] If [with <item>] is omitted tries to use a random item from the inventory.");
		IRCBot.registerCommand(local_command);
	}
}
