/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Juggle extends AbstractListener {
	private Command local_command;
	private double base_drop_chance = .20; // 0.10 is added for every item included

	@Override
	protected void initHook() {
		local_command = new Command("juggle", 0) {
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
						if (drop_roll < (100 * (base_drop_chance + (0.10 * item_amount)))) {
							dropped++;
							int damage = Helper.getRandomInt(1, 3);
							String dust = item.damage(damage);
							Helper.sendAction(target, "drops " + item.getName() + " which takes " + damage + " damage" + dust);
						}
					}
				}
				if (dropped > 0)
					Helper.sendMessage(target, Helper.get_fail_response());
				else {
					Helper.sendAction(target, "doesn't drop anything");
					Helper.sendMessage(target, Helper.get_success_response());
				}
			}
		};
		IRCBot.registerCommand(local_command, "Juggle with items");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}
}
