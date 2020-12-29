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

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Fling extends AbstractListener {
	private Command local_command;
	private static String ALIAS_SLING = "sling";
	private static String ALIAS_SHOOT = "shoot";
	private static String ALIAS_LAUNCH = "launch";

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("fling", new CommandArgumentParser(1, new CommandArgument("Target", "String"), new CommandArgument("Item", "String"))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String flingTarget = this.argumentParser.getArgument("Target");
				String with = this.argumentParser.getArgument("Item");

				Item item = null;
				try {
					if (with == null || with.equals(""))
						item = Inventory.getRandomItem(false);
					else
						item = new Item(with, false);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (item != null) {
					String action = " flings ";
					if (this.getActualCommand().equals(ALIAS_SLING))
						action = " slings ";
					else if (this.getActualCommand().equals(ALIAS_SHOOT))
						action = " shoots ";
					else if (this.getActualCommand().equals(ALIAS_LAUNCH))
						action = " launches ";

					if (flingTarget.equals(""))
						flingTarget = Helper.getRandomUser(event);
					if (!Helper.doInteractWith(flingTarget))
						flingTarget = nick;
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					DiceRoll hit = Helper.rollDice("1d100").getFirstGroupOrNull();
					int itemDamage = 0;
					if (hit != null && hit.getSum() > 20) {
						DiceRollResult dmg = item.getDamage();
						dmg.bonus = DiceRollBonusCollection.getOffensiveItemBonus(item);

						String dmgString = dmg.getResultString();
						if (dmgString == null)
							dmgString = "no damage";
						else
							dmgString += " damage";
						String result = nick + action + item.getName() + " in a random direction. It hits " + flingTarget + " " + Helper.getHitPlace() + ". They take " + dmgString + "!";
						Defend.addEvent(nick, flingTarget, target, dmg.getTotal(), item.getName(), Defend.EventTypes.FLING, result);
						Helper.sendMessage(target, nick + " is flinging something at " + flingTarget + "! They have " + Defend.getReactionTimeString() + " if they want to attempt to " + Config.commandprefix + "defend against it!");
						itemDamage = 1;
					} else {
						Helper.sendMessage(target, nick + action + item.getName() + " in a random direction. It hits the ground near " + flingTarget);
						itemDamage = 2;
					}
					String dust = item.damage(itemDamage, false, true, true);
					if (!dust.equals(""))
						Helper.sendAction(target, dust);
				} else {
					String action = "flinging";
					if (this.getActualCommand().equals(ALIAS_SLING))
						action = "slinging";
					else if (this.getActualCommand().equals(ALIAS_SHOOT))
						action = "shooting";
					else if (this.getActualCommand().equals(ALIAS_LAUNCH))
						action = "launching";

					Helper.sendMessage(target, nick + " makes a " + action + " motion but realizes there was nothing there...");
				}
			}
		};
		local_command.registerAlias(ALIAS_SLING);
		local_command.registerAlias(ALIAS_SHOOT);
		local_command.registerAlias(ALIAS_LAUNCH);
		local_command.setHelpText("Fling something at someone! Syntax: " + Config.commandprefix + local_command.getCommand() + " [<item> [at <target>]] If [at <target>] is omitted picks a random target from IRC user list. If <item> is omitted tries to use a random item from the inventory.");
	}
}
