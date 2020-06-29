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

import java.util.Arrays;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Fling extends AbstractListener {
	private Command local_command;
	private static String ALIAS_SLING = "sling";
	private static String ALIAS_SHOOT = "shoot";

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Fling");
	}

	private void initCommands() {
		local_command = new Command("fling") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					String[] split = params.split("^? ?at ", 2);
					String flingTarget = "";
					String with;
					if (split.length == 1) {
						with = split[0].trim();
					} else {
						with = split[0].trim();
						flingTarget = split[1].trim();
					}

					Item item = null;
					try {
						if (with.equals(""))
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
							Defend.addEvent(nick, flingTarget, dmg.getTotal(), item.getName(), Defend.EventTypes.FLING);
							Helper.sendMessage(target, nick + action + item.getName() + " in a random direction. It hits " + flingTarget + " " + Helper.getHitPlace() + ". They take " + dmgString + "!");
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

						Helper.sendMessage(target, nick + " makes a " + action + " motion but realizes there was nothing there...");
					}
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "Something wrong happened.");
				}
			}
		};
		local_command.registerAlias(ALIAS_SLING);
		local_command.registerAlias(ALIAS_SHOOT);
		local_command.setHelpText("Fling something at someone! Syntax: " + Config.commandprefix + local_command.getCommand() + " [<item> [at <target>]] If [at <target>] is omitted picks a random target from IRC user list. If <item> is omitted tries to use a random item from the inventory.");
	}
}
