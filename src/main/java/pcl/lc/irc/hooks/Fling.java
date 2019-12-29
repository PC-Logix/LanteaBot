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

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Fling extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Fling");
	}

	private void initCommands() {
		local_command = new Command("fling", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String[] split = params.split(" at ", 2);
				String flingTarget = null;
				String with = null;
				if (split.length == 1) {
					flingTarget = split[0].trim();
				} else {
					with = split[0].trim();
					flingTarget = split[1].trim();
				}

				Item item = null;
				try {
					if (with == null)
						Inventory.getRandomItem(false);
					else
						item = new Item(with, false);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (item != null) {
					String user = flingTarget;
					if (user.equals(""))
						user = Helper.getRandomUser(event);
					if (!Helper.doInteractWith(user))
						user = nick;
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
						Helper.sendMessage(target, nick + " flings " + item.getName() + " in a random direction. It hits " + user + " " + Helper.get_hit_place() + ". They take " + dmgString + "!");
						itemDamage = 1;
					}
					else {
						Helper.sendAction(target, "flings " + item.getName() + " in a random direction. It hits the ground near " + user);
						itemDamage = 2;
					}
					String dust = item.damage(itemDamage, false, true, true);
					if (!dust.equals(""))
						Helper.sendAction(target, dust);
				} else {
					Helper.sendAction(target, "makes a flinging motion but realizes there was nothing there...");
				}
			}
		};
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
