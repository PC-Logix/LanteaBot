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

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Attack extends AbstractListener {
	private Command local_command;
	private HashMap<String, ActionType> actions;

	@Override
	protected void initHook() {
		actions = new HashMap<>();
		actions.put("stab", new ActionType("Stabbing", "Stabbed", "Stab"));
		actions.put("hit", new ActionType("Hitting", "Hit", "Hit"));
		actions.put("shiv", new ActionType("Shivving", "Shivved", "Shiv"));
		actions.put("strike", new ActionType("Striking", "Struck", "Strike"));
		actions.put("slap", new ActionType("Slapping", "Slapped", "Slap"));
		actions.put("poke", new ActionType("Poking", "Poked", "Poke"));
		actions.put("prod", new ActionType("Prodding", "Prodded", "Prod"));

		local_command = new Command("attack", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 0) {
					Helper.sendMessage(target, "Valid \"attacks\": " + actions.toString().replace("[", "").replace("]", ""));
					return;
				}
				try
				{
					String method = params.remove(0);
					String message = "";
					for (String aParam : params)
					{
						message += " " + aParam;
					}

					Item item = Inventory.getRandomItem(false);
					String dust = "";
					if (item != null) {
						dust = item.decrementUses();
					}

					if (!actions.containsKey(method.toLowerCase())) {
						Helper.sendMessage(target, "Valid \"attacks\": " + actions.toString().replace("[", "").replace("]", ""));
						return;
					}
					//action = Helper.getRandomInt(0, actions.size() - 1);

					String attackTarget = message.trim();

					if (attackTarget.equals(""))
						Helper.sendAction(target,"flails at nothingness" + (item != null ? " with " + item.getName() : ""));
					else if (Helper.doInteractWith(attackTarget)) {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						DiceRollResult dmg = new DiceRollResult();
						if (item != null) {
							dmg = item.getDamage();
							dmg.bonus = DiceRollBonusCollection.getOffensiveItemBonus(item);
						}
						else
							dmg = Item.getGenericRoll(1, 4, new DiceRollBonusCollection());
						String dmgString = dmg.getResultString();
						if (dmgString == null)
							dmgString = "no damage";
						else
							dmgString += " damage";
						Helper.sendMessage(target, nick + " is " + actions.get(method.toLowerCase()).actionNameIs.toLowerCase() + " " + attackTarget + (item != null ? " with " + item.getName() : "") + " for " + dmgString + dust);
//						Helper.sendAction(target,Helper.antiPing(actions.get(action)) + "s " + attackTarget + (item != null ? " with " + item.getName() : "") + " doing " + Item.stringifyDamageResult(dmg) + dust);
					} else {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendAction(target,"uses " + (item != null ? item.getName() : Helper.parseSelfReferral("his") + " orbital death ray") + " to vaporize " + Helper.antiPing(nick) + dust);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		IRCBot.registerCommand(local_command, "Attack things with things. First argument is attack type: " + actions.toString().replace("[","").replace("]","") + " or random if invalid.");
		for (String action : actions.keySet()) {
			local_command.registerAlias(action, action);
		}
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
