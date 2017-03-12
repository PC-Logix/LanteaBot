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
public class Stab extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("stab", 0);
		IRCBot.registerCommand(local_command, "Stab things with things");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (local_command.shouldExecute(command) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		long shouldExecute = local_command.shouldExecute(command, nick);
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		if (shouldExecute == 0) {
			local_command.updateLastExecution();
			String message = "";
			for (String aCopyOfRange : copyOfRange)
			{
				message = message + " " + aCopyOfRange;
			}
			try
			{
				Item item = Inventory.getRandomItem(false);
				String dust = "";
				if (item != null) {
					dust = item.decrementUses();
				}

				String target = message.trim();

				ArrayList<String> actions = new ArrayList<>();
				actions.add("stabs");
				actions.add("hits");
				actions.add("shivs");
				actions.add("strikes");
				actions.add(Helper.antiPing("slaps"));
				int action = Helper.getRandomInt(0, actions.size() - 1);

				if (target == "")
					Helper.sendAction(this.target,"flails at nothingness" + (item != null ? " with " + item.getName() : ""));
				else if (!target.toLowerCase().contains(IRCBot.ournick.toLowerCase()))
					Helper.sendAction(this.target,actions.get(action) + " " + target + (item != null ? " with " + item.getName() : "") + " doing " + Helper.rollDiceString("1d20") + " damage" + dust);
				else
					Helper.sendAction(this.target,"uses " + (item != null ? item.getName() : " an orbital death ray") + " to vaporize " + Helper.antiPing(nick) + dust);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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
