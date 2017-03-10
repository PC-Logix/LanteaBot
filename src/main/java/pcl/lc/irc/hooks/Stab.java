/**
 * 
 */
package pcl.lc.irc.hooks;

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
				PreparedStatement statement = Database.getPreparedStatement("getRandomItemNonFavourite");
				ResultSet resultSet = statement.executeQuery();

				String item = "";
				Integer id = null;
				Integer uses = null;
				try
				{
					if (resultSet.next())
					{
						id = resultSet.getInt(1);
						item = resultSet.getString(2);
						uses = resultSet.getInt(3);
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				String s = message.trim();

				String dust = "";
				if (s != "" && uses != null && uses == -1)
				{
					//Do nothing, item cannot break
				}
				else if (s != "" && uses != null && uses > 1)
				{
					statement = Database.getPreparedStatement("decrementUses");
					statement.setInt(1, id);
					statement.executeUpdate();
					System.out.println("Decrement uses for item " + id);
				}
				else if (s != "" && uses != null)
				{
					statement = Database.getPreparedStatement("removeItemId");
					statement.setInt(1, id);
					statement.executeUpdate();
					System.out.println("Remove item " + id);
					dust = ", the " + item.replace("a ", "").replace("A ", "").replace("an ", "").replace("the ", "") + " " + Inventory.getItemBreakString() + ".";
				}

				ArrayList<String> actions = new ArrayList<>();
				actions.add("stabs");
				actions.add("hits");
				actions.add(Helper.antiPing("slaps"));

				int action = Helper.getRandomInt(0, actions.size() - 1);
				System.out.println("Action: " + action);

				if (s == "")
					Helper.sendAction(target,"flails at nothingness" + (!item.equals("") ? " with " : "") + item);
				else if (!s.equals(IRCBot.ournick))
					Helper.sendAction(target,actions.get(action) + " " + s + (!item.equals("") ? " with " : "") + item + " doing " + Helper.rollDiceString("1d20") + " damage" + dust);
				else
					Helper.sendAction(target,"uses " + (!item.equals("") ? item : " an orbital death ray") + " to vaporize " + Helper.antiPing(nick) + dust);
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
