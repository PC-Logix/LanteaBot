/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TablesOfRandomThings;

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Juggle extends AbstractListener {
	private Command local_command;
	private double base_drop_chance = .20; // 0.08 is added for every item included

	@Override
	protected void initHook() {
		local_command = new Command("juggle", new CommandArgumentParser(0, new CommandArgument("Number", ArgumentTypes.INTEGER)), new CommandRateLimit(60)) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				int item_amount = this.argumentParser.getInt("Number");
				if (item_amount == Integer.MIN_VALUE)
					item_amount = 3;
				item_amount = Math.min(6, item_amount);
				item_amount = Math.max(1, item_amount);

				ItemCollection items = new ItemCollection();
				if (!items.fillWithUniqueItems(item_amount)) {
					Helper.sendMessage(target, "I can't find any items to juggle with.", nick);
					return CommandChainState.FINISHED;
				}

				Helper.sendAction(target, "juggles with " + items.getItemNames());
				int dropped = 0;
				for (Item item : items.getItems()) {
					if (item != null) {
						int drop_roll = Helper.getRandomInt(0, 100);
						if (drop_roll < (100 * (base_drop_chance + (0.08 * item_amount)))) {
							dropped++;
							int damage = Helper.getRandomInt(1, 5);
							String dust = item.damage(damage, true, false, true);
							Helper.sendAction(target, "drops " + item.getName() + Colors.NORMAL + " which takes " + damage + " damage" + dust);
						}
					}
				}
				if (dropped > 0)
					Helper.sendMessage(target, TablesOfRandomThings.getFailResponse());
				else {
					Helper.sendAction(target, "doesn't drop anything");
					Helper.sendMessage(target, TablesOfRandomThings.getSuccessResponse());
				}
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("Juggle with items");
		IRCBot.registerCommand(local_command);
	}
}
