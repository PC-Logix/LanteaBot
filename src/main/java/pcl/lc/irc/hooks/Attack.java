/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;

import javax.swing.Action;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Attack extends AbstractListener {
	private Command local_command;
	private HashMap<String, ActionType> actions;
	private static final String STAB = "stab";
	private static final String HIT = "hit";
	private static final String SHIV = "shiv";
	private static final String STRIKE = "strike";
	private static final String SLAP = "slap";
	private static final String POKE = "poke";
	private static final String PROD = "prod";
	private static final String SMACK = "smack";
	private static final String CONK = "conk";

	private static final String BITE = "bite";
	private static final String CLAW = "claw";

	private static String actionList = "";

	@Override
	protected void initHook() {
		actions = new HashMap<>();
		actions.put(STAB, new ActionType("Stabbing", "Stabbed", "Stab", "Stabbed"));
		actions.put(HIT, new ActionType("Hitting", "Hit", "Hit", "Hit"));
		actions.put(SHIV, new ActionType("Shivving", "Shivved", "Shiv", "Shivved"));
		actions.put(STRIKE, new ActionType("Striking", "Struck", "Strike", "Struck"));
		actions.put(SLAP, new ActionType("Slapping", "Slapped", "Slap", "Slapped"));
		actions.put(POKE, new ActionType("Poking", "Poked", "Poke", "Poked"));
		actions.put(PROD, new ActionType("Prodding", "Prodded", "Prod", "Prodded"));
		actions.put(SMACK, new ActionType("Smacking", "Smacked", "Smack", "Smacked"));
		actions.put(CONK, new ActionType("Conking", "Conked", "Conk", "Conked"));

		actions.put(BITE, new ActionType("Biting", "Bit", "Bite", "Bitten"));
		actions.put(CLAW, new ActionType("Clawing", "Clawed", "Claw", "Clawed"));

		ArrayList<String> acts = new ArrayList<>();
		for (Map.Entry<String, ActionType> act : actions.entrySet())
			acts.add(act.getValue().actionNameWill);
		actionList = String.join(", ", acts);

		local_command = new Command("attack", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 0) {
					Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
					return;
				}
				try
				{
					String method = params.remove(0);
					if (!actions.containsKey(method.toLowerCase())) {
						Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
						return;
					}

					String message = "";
					for (String aParam : params)
					{
						message += " " + aParam;
					}

					String[] split = message.trim().split(" with ", 2);
					String attackTarget = split[0].trim();
					String with = null;
					if (split.length > 1)
						with = split[1].trim();

					Item item = null;
					if (!actions.get(method.toLowerCase()).equals(actions.get(BITE)) && !actions.get(method.toLowerCase()).equals(actions.get(CLAW))) { //Don't get item on bite attack or claw attack
						if (with == null)
							item = Inventory.getRandomItem(false);
						else
							item = new Item(with, false);
					}

					String dust = "";
					if (item != null) {
						dust = item.decrementUses(false, true, true);
						if (!dust.equals(""))
							dust = " " + dust;
					}

					//action = Helper.getRandomInt(0, actions.size() - 1);

					if (attackTarget.equals(""))
						Helper.sendMessage(target,nick + " flails at nothingness" + (item != null ? " with " + item.getName() : ""));
					else if (Helper.doInteractWith(attackTarget)) {
						Helper.AntiPings = Helper.getNamesFromTarget(target);

						DiceRollResult dmg;
						if (item != null) {
							dmg = item.getDamage();
							dmg.bonus = DiceRollBonusCollection.getOffensiveItemBonus(item);
						} else if (actions.get(method.toLowerCase()).equals(actions.get(BITE))) {
							dmg = Item.getGenericRoll(1, 6, new DiceRollBonusCollection());
						} else if (actions.get(method.toLowerCase()).equals(actions.get(CLAW))) {
							dmg = Item.getGenericRoll(1, 6, new DiceRollBonusCollection());
						} else {
							dmg = Item.getGenericRoll(1, 4, new DiceRollBonusCollection());
						}
						String dmgString = dmg.getResultString();
						if (dmgString == null)
							dmgString = "no damage";
						else
							dmgString += " damage";
						String itemName = item != null ? item.getName() : "";
						Helper.addAttack(nick, attackTarget, dmg.getTotal(), itemName);
						Helper.sendMessage(target, nick + " is " + actions.get(method.toLowerCase()).actionNameIs.toLowerCase() + " " + attackTarget + (item != null ? " with " + item.getName() : "") + " for " + dmgString + "!" + dust);
					} else {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendAction(target,DiceRoll.rollDiceInString("uses " + (item != null ? item.getName() : Helper.parseSelfReferral("his") + " orbital death ray") + " to vaporize " + Helper.antiPing(nick) + " who takes 10d10 damage." + dust));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		IRCBot.registerCommand(local_command, "Attack someone and deal damage! Syntax: " + Config.commandprefix + local_command.getCommand() + " <attack_type> <target> [with <item>] Valid attack types: " + actions.toString().replace("[","").replace("]","") + " or random if invalid. If [with <item>] is omitted tries to use a random item from the inventory. Note that 'bite' always ignores any item. Each attack type can also be used as an individual command.");
		for (String action : actions.keySet()) {
			local_command.registerAlias(action, action);
		}
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
