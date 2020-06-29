/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.CommandRateLimit;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;
import pcl.lc.utils.ItemCollection;

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
		local_command = new Command("juggle", new CommandRateLimit(60)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				local_command.updateLastExecution();
				int item_amount = 3;
				try {
					item_amount = Integer.parseInt(params.get(0));
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("No valid amount specified. Defaulting to " + item_amount);
				}
				item_amount = Math.min(6, item_amount);
				item_amount = Math.max(1, item_amount);

				ItemCollection items = new ItemCollection();
				if (!items.fillWithUniqueItems(item_amount)) {
					Helper.sendMessage(target, "I can't find any items to juggle with.", nick);
					return;
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
					Helper.sendMessage(target, Helper.getFailResponse());
				else {
					Helper.sendAction(target, "doesn't drop anything");
					Helper.sendMessage(target, Helper.getSuccessResponse());
				}
			}
		};
		IRCBot.registerCommand(local_command, "Juggle with items");
	}
}
