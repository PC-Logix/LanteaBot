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

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Pet extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("pet", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try
				{
					Item item = Inventory.getRandomItem(false);
					String dust = "";
					if (item != null) {
						dust = item.decrementUses(false, true, false);
					}

					params = params.trim();

					ArrayList<String> actions = new ArrayList<>();
					actions.add("pets");
					actions.add("brushes");

					DiceRoll roll = Helper.rollDice(Math.max(1, (item != null ? item.getUsesLeft() : 1) / 2) + "d4");

					int action = Helper.getRandomInt(0, actions.size() - 1);

					if (params == "")
						Helper.sendAction(target,"flails at nothingness" + (item != null ? " with " + item.getName() : ""));
					 else if (nick.equals(params)) {
						 Helper.sendMessage(target,"Don't pet yourself in public.", nick);
					}
					else if (Helper.doInteractWith(params)) {
						int[] heal = {0,0,0};
						if (item != null)
							heal = item.getHealing();
						Helper.sendAction(target, actions.get(action) + " " + params + (item != null ? " with " + item.getName() + "." : "") + ((roll != null) ? " " + Item.stringifyHealingResult(heal) + "!" : "") + " " + dust);
					}
					else
						Helper.sendMessage(target,"I'm not going to pet myself in public. It'd be rude.", nick);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		local_command.registerAlias("stroke");
		IRCBot.registerCommand(local_command, "Give pets");
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
