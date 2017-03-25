/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Item;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Give extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("give") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String target_argument = params.get(0);
				String item_name = "";
				for (int i = 1; i < params.size(); i++)
				{
					item_name += params.get(i) + " ";
				}
				item_name = item_name.trim();

				Item item;
				if (item_name.equals("random")) {
					System.out.println("Get random item");
					item = Inventory.getRandomItem(false);
				}
				else
					try {
						item = new Item(item_name);
					} catch (Exception e) {
						item = null;
					}

				if (item == null) {
					Helper.sendMessage(target, "I couldn't find an item.", nick);
					return;
				}

				int removeResult = Inventory.removeItem(item);

				if (removeResult == 0 || removeResult == Inventory.ERROR_ITEM_IS_PRESERVED)
					Helper.sendAction(target ,  "gives " + target_argument + " " + item.getName() + " from her inventory");
				else if (removeResult == Inventory.ERROR_ITEM_IS_FAVOURITE)
					Helper.sendMessage(target ,  "No! This is my favourite thing! I wont give it away!", nick);
				else if (removeResult == Inventory.ERROR_NO_ROWS_RETURNED)
					Helper.sendMessage(target ,  "No item found to give away.", nick);
				else
					Helper.sendMessage(target ,  "Something went wrong (" + removeResult + ")", nick);
			}
		}; local_command.setHelpText("/give <target> <item>|random - Give <target> <item> if found or random");
		IRCBot.registerCommand(local_command);
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
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}
