/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
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

	private enum Actions {
		STAB("stab", new ActionType("Stabbing", "Stabbed", "Stab", "Stabbed")),
		HIT("hit", new ActionType("Hitting", "Hit", "Hit", "Hit")),
		SHIV("shiv", new ActionType("Shivving", "Shivved", "Shiv", "Shivved")),
		STRIKE("strike", new ActionType("Striking", "Struck", "Strike", "Struck")),
		SLAP("slap", new ActionType("Slapping", "Slapped", "Slap", "Slapped")),
		POKE("poke", new ActionType("Poking", "Poked", "Poke", "Poked")),
		PROD("prod", new ActionType("Prodding", "Prodded", "Prod", "Prodded")),
		SMACK("smack", new ActionType("Smacking", "Smacked", "Smack", "Smacked")),
		CONK("conk", new ActionType("Conking", "Conked", "Conk", "Conked")),

		BITE("bite", new ActionType("Biting", "Bit", "Bite", "Bitten")),
		CLAW("claw", new ActionType("Clawing", "Clawed", "Claw", "Clawed")),
		PUNCH("punch", new ActionType("Punching", "Punched", "Punch", "Punched"));

		private final String command;
		private ActionType type;

		Actions(String command, ActionType type) { this.command = command; this.type = type; }
	}

	private static String actionList = "";

	@Override
	protected void initHook() {

		ArrayList<String> acts = new ArrayList<>();
		for (Attack.Actions act : Attack.Actions.values())
			if (act.command != null)
				acts.add(act.command);
		actionList = String.join(", ", acts);

		local_command = new Command("attack", new CommandRateLimit(60)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() == 0) {
					Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
					return;
				}
				try
				{
					String method = params.remove(0);
					if (!actionList.contains(method.toLowerCase())) {
						Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
						return;
					}
					Actions action = Actions.valueOf(method.toUpperCase());

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

					ArrayList<Actions> nonItemActions = new ArrayList<>();
					nonItemActions.add(Actions.BITE);
					nonItemActions.add(Actions.CLAW);
					nonItemActions.add(Actions.PUNCH);

					if (!nonItemActions.contains(action)) {
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
						} else if (action == Actions.BITE) {
							dmg = Item.getGenericRoll(1, 6);
						} else if (action == Actions.CLAW) {
							dmg = Item.getGenericRoll(1, 6);
						} else if (action == Actions.PUNCH) {
							dmg = Item.getGenericRoll(1, 4);
						} else {
							dmg = Item.getGenericRoll(1, 4);
						}
						String dmgString = dmg.getResultString();
						if (dmgString == null)
							dmgString = "no damage";
						else
							dmgString += " damage";
						String itemName = item != null ? item.getName() : "";
						Defend.addEvent(nick, attackTarget, dmg.getTotal(), itemName, Defend.EventTypes.ATTACK);
						Helper.sendMessage(target, nick + " is " + action.type.actionNameIs.toLowerCase() + " " + attackTarget + (item != null ? " with " + item.getName() : "") + " for " + dmgString + "!" + dust);
					} else {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendAction(target,DiceRoll.rollDiceInString("uses " + (item != null ? item.getName() : Helper.parseSelfReferral("his") + " orbital death ray") + " to vaporize " + Helper.antiPing(nick) + " who takes 10d10 damage." + dust));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		IRCBot.registerCommand(local_command, "Attack someone and deal damage! Syntax: " + Config.commandprefix + local_command.getCommand() + " <attack_type> <target> [with <item>] Valid attack types: " + actionList + " or random if invalid. If [with <item>] is omitted tries to use a random item from the inventory. Note that 'bite' always ignores any item. Each attack type can also be used as an individual command.");
		for (Attack.Actions action : Attack.Actions.values()) {
			if (action.command != null)
				local_command.registerAlias(action.command, action.command);
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
