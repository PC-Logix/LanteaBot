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
	int hitChance = 40; //percentage (out of 100)

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
				DiceRoll roll = Helper.rollDice("1d100");

				Item item = Inventory.getRandomItem(false);
				if (item != null) {
					String user = params;
					String userSecondary = Helper.getRandomUser(event);
					String userTertiary = Helper.getRandomUser(event);
					if (user == "")
						user = Helper.getRandomUser(event);
					else if (!Helper.doInteractWith(user))
					{
						Helper.sendAction(target, "kicks " + nick + " into space.");
						return;
					}
					int itemDamage = 0;
					String dust = "";
					String strike = "Seems it was a dud...";
					try {
						if (roll != null && roll.getSum() < hitChance) {
							int[] dmg1 = item.getDamage(6, 4);
							int[] dmg2 = item.getDamage(4, 2);
							int[] dmg3 = item.getDamage(4, 2);
							strike = "It strikes " + user + ". They take " + Item.stringifyDamageResult(dmg1) + ". " + userSecondary + " and " + userTertiary + " stood too close and take " + Item.getResult(dmg2) + Item.getParenthesis(dmg2) + " and " + Item.getResult(dmg3) + Item.getParenthesis(dmg3) + " damage respectively.";
							itemDamage = 1;
						} else {
							int[] dmg1 = item.getDamage(4, 2);
							int[] dmg2 = item.getDamage(4, 2);
							int[] dmg3 = item.getDamage(4, 2);
							strike = "It strikes the ground near " + user + ", " + userSecondary + " and " + userTertiary + ". They each take " + Item.getResult(dmg1) + Item.getParenthesis(dmg1) + ", " + Item.getResult(dmg2) + Item.getParenthesis(dmg2) + " and " + Item.getResult(dmg3) + Item.getParenthesis(dmg3) + " splash damage respectively.";
							itemDamage = 2;
						}
					} catch (NullPointerException ignored) {}
					Helper.AntiPings = Helper.getNamesFromTarget(target);
					Helper.sendAction(target, "loads " + item.getName(false) + " into a shell and fires it. " + strike);
					dust = item.damage(itemDamage,false, true, true);
					if (dust != "") {
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
