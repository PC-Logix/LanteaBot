/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringEscapeUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.Helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
						dust = item.decrementUses();
					}

					params = params.trim();

					ArrayList<String> actions = new ArrayList<>();
					actions.add("pets");
					actions.add("brushes");

					DiceRoll roll = Helper.rollDice(Math.max(1, (item != null ? item.getUsesLeft() : 1) / 2) + "d4");

					int action = Helper.getRandomInt(0, actions.size() - 1);
					System.out.println("Action: " + action);

					if (params == "")
						Helper.sendAction(target,"flails at nothingness" + (item != null ? " with " + item.getName() : ""));
					else if (!params.equals(IRCBot.ournick))
						Helper.sendAction(target,actions.get(action) + " " + params + (item != null ? " with " + item.getName() + "." : "") + ((roll != null) ? " " + params + " recovers " + roll.getSum() + " health!" : "") + dust);
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
		if (local_command.shouldExecute(command, event) >= 0) {
			chan = event.getChannel().getName();
		}
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
