/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringEscapeUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

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

				if (!target_argument.equals(IRCBot.getOurNick())) {
					Item item;
					if (item_name.equals("random")) {
						System.out.println("Get random item");
						item = Inventory.getRandomItem(false);
					} else
						try {
							item = new Item(StringEscapeUtils.escapeHtml4(item_name));
						} catch (Exception e) {
							item = null;
						}

					if (item == null) {
						Helper.sendAction(target, "searches through " + Helper.parseSelfReferral("his") + " inventory for a bit. \"I couldn't find anything...\"");
						return;
					}

					int removeResult = Inventory.removeItem(item);

					if (removeResult == 0 || removeResult == Inventory.ERROR_ITEM_IS_PRESERVED)
						Helper.sendAction(target, "gives " + target_argument + " " + StringEscapeUtils.unescapeHtml4(item.getName()) + " from " + Helper.parseSelfReferral("his") + " inventory");
					else if (removeResult == Inventory.ERROR_ITEM_IS_FAVOURITE)
						Helper.sendMessage(target, "No! This is my favourite thing! I wont give it away!", nick);
					else if (removeResult == Inventory.ERROR_NO_ROWS_RETURNED)
						Helper.sendMessage(target, "No item found to give away.", nick);
					else
						Helper.sendMessage(target, "Something went wrong (" + removeResult + ")", nick);
				}
				else if (!item_name.equals(IRCBot.getOurNick())) {
					String add_attempt = Inventory.addItem(item_name, nick, false, false);
					if (add_attempt.equals("already has a few of those."))
						Helper.sendAction(target, "politely declines, as " + Helper.parseSelfReferral("he") + " already has a few of those");
					else
						Helper.sendAction(target, "accepts " + Inventory.fixItemName(item_name, true) + " and adds it to " + Helper.parseSelfReferral("his") + " inventory");
				}
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
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
