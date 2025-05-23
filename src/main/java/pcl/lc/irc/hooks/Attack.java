/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.*;

import java.util.ArrayList;

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

		Actions(String command, ActionType type) {
			this.command = command;
			this.type = type;
		}
	}

	private static String actionList = "";

	@Override
	protected void initHook() {

		ArrayList<String> acts = new ArrayList<>();
		for (Attack.Actions act : Attack.Actions.values())
			if (act.command != null)
				acts.add(act.command);
		actionList = String.join(", ", acts);

		local_command = new Command("attack", new CommandArgumentParser(2, new CommandArgument(ArgumentTypes.STRING, "Action", "One of " + Helper.oxfordJoin(acts, ", ", ", or ")), new CommandArgument(ArgumentTypes.STRING, "Target"), new CommandArgument(ArgumentTypes.STRING, "Item", "If item is not specified tries to use random inventory item.")), new CommandRateLimit(300, true)) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String method = this.argumentParser.getArgument("Action");
				if (!actionList.contains(method.toLowerCase())) {
					Helper.sendMessage(target, "Specify an action as the first parameter: " + actionList);
					return null;
				}
				Actions action = Actions.valueOf(method.toUpperCase());
				String attackTarget = this.argumentParser.getArgument("Target");
				String with = this.argumentParser.getArgument("Item");
				if (with != null && with.startsWith("with"))
					with = with.replaceFirst("with ?", "");
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
					Helper.sendMessage(target, nick + " flails at nothingness" + (item != null ? " with " + item.getName() : ""));
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
					String result = nick + " is " + action.type.actionNameIs.toLowerCase() + " " + attackTarget + (item != null ? " with " + item.getName() : "") + " for " + dmgString + "!" + dust;
					Defend.addEvent(nick, attackTarget, target, dmg.getTotal(), itemName, Defend.EventTypes.ATTACK, result);
					Helper.sendMessage(target, nick + " is trying to " + action.type.actionNameWill.toLowerCase() + " " + attackTarget + "! They have " + Defend.getReactionTimeString() + " if they want to attempt to " + Config.commandprefix + "defend against it!");
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, DiceRoll.rollDiceInString("uses " + (item != null ? item.getName() : Helper.parseSelfReferral("his") + " orbital death ray") + " to vaporize " + Helper.antiPing(nick) + " who takes 10d10 damage." + dust));
				}
				return new CommandChainStateObject();
			}
		};
		for (Attack.Actions action : Attack.Actions.values()) {
			if (action.command != null)
				local_command.registerAlias(action.command, action.command);
		}
		local_command.setHelpText("Attack someone and deal damage! Each action can also be used as an alias which only needs the target (and optionally an item) as arguments.");
		IRCBot.registerCommand(local_command);
	}
}
