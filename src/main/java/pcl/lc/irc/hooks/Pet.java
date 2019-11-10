/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
		actions.put("pets", new ActionType("Petting", "Petting", "Pet"));
		actions.put("brushes", new ActionType("Brushing", "Brushing", "Brush"));

		local_command = new Command("pet", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				try
				{
					Item item = Inventory.getRandomItem(false);
					String dust = "";
					if (item != null) {
						dust = item.decrementUses(false, true, true);
						if (!dust.equals(""))
							dust = " " + dust;
					}

					String petTarget = String.join(" ", params);

					String[] keys = actions.keySet().toArray(new String[0]);
					ActionType petAction = actions.get(keys[Helper.getRandomInt(0, keys.length - 1)]);

//					DiceRoll roll = Helper.rollDice(Math.max(1, (item != null ? item.getUsesLeft() : 1) / 2) + "d4").getFirstGroupOrNull();

					if (petTarget.equals(""))
						Helper.sendAction(target,"flails at nothingness" + (item != null ? " with " + item.getName() : ""));
					 else if (nick.equals(petTarget)) {
						 Helper.sendMessage(target,"Don't pet yourself in public.", nick);
					}
					else if (Helper.doInteractWith(petTarget)) {
						DiceRollResult heal = new DiceRollResult();
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
//						Helper.sendAction(target, actions.get(action) + " " + params + (item != null ? " with " + item.getName() + "." : "") + ((roll != null) ? " " + Item.stringifyHealingResult(heal) + "!" : "") + " " + dust);
					}
					else
						Helper.sendMessage(target,"I'm not going to pet myself in public. It'd be rude.", nick);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		local_command.registerAlias("stroke");
		IRCBot.registerCommand(local_command, "Give pets");
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
