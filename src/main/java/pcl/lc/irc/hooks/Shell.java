/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.DiceRoll;
import pcl.lc.utils.DiceRollResult;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Shell extends AbstractListener {
	private List<Command> commands;
	private Command shell;
	private int hitChance = 40; //percentage (out of 100)

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(shell, "Shell a target or random user. " + hitChance + "% hit chance.");
	}

	private void initCommands() {
		commands = new ArrayList<>();
		shell = new Command("shell", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				DiceRoll roll = Helper.rollDice("1d100").getFirstGroupOrNull();

				String[] split = params.split(" with ", 2);
				String shellTarget = split[0].trim();
				String with = null;
				if (split.length > 1)
					with = split[1].trim();

				Item item = null;
				try {
					if (with == null)
						item = Inventory.getRandomItem(false);
					else
						item = new Item(with, false);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (item != null) {
					String user = shellTarget;
					String userSecondary = Helper.getRandomUser(event);
					String userTertiary = Helper.getRandomUser(event);
					if (user.equals(""))
						user = Helper.getRandomUser(event);
					else if (!Helper.doInteractWith(user))
					{
						Helper.sendAction(target, "kicks " + nick + " into space.");
						return;
					}
					int itemDamage = 0;
					String dust;
					String strike = "Seems it was a dud...";
					try {
						if (roll != null && roll.getSum() < hitChance) {
							DiceRollResult dmg1 = item.getDamage(1, 6, 4);
							DiceRollResult dmg2 = item.getDamage(1, 4, 2);
							DiceRollResult dmg3 = item.getDamage(1, 4, 2);
							String auxiliary_damage = (dmg2.getTotal() == dmg3.getTotal() ? dmg2.getTotal() + " damage each" : dmg2.getTotal() + Item.getParenthesis(dmg2) + ", and " + dmg3.getTotal() + Item.getParenthesis(dmg3) + " damage respectively");
							strike = "It strikes " + user + ". They take " + Item.stringifyDamageResult(dmg1) + ". " + userSecondary + ", and " + userTertiary + " stood too close and take " + auxiliary_damage + ".";
							itemDamage = 1;
						} else {
							DiceRollResult dmg1 = item.getDamage(1, 4, 2);
							DiceRollResult dmg2 = item.getDamage(1, 4, 2);
							DiceRollResult dmg3 = item.getDamage(1, 4, 2);
							String damage = (dmg1.getTotal() == dmg2.getTotal() && dmg1.getTotal() == dmg3.getTotal() ? dmg1.getTotal() + " damage" : dmg1.getTotal() + Item.getParenthesis(dmg1) + ", " + dmg2.getTotal() + Item.getParenthesis(dmg2) + ", and " + dmg3.getTotal() + Item.getParenthesis(dmg3) + " splash damage respectively");
							strike = "It strikes the ground near " + user + ", " + userSecondary + ", and " + userTertiary + ". They each take " + damage + ".";
							itemDamage = 2;
						}
					} catch (NullPointerException ignored) {}
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, "loads " + item.getName(false) + " into a shell and fires it. " + strike);
					dust = item.damage(itemDamage,false, true, true);
					if (!dust.equals("")) {
						Helper.AntiPings = Helper.getNamesFromTarget(target);
						Helper.sendAction(target, dust);
					}
				} else {
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, "found nothing to load into the shell...");
				}
			}
		};
		commands.add(shell);
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
		for (Command com : commands)
			com.tryExecute(command, nick, target, event, copyOfRange);
	}
}
