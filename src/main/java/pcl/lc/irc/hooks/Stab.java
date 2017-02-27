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
		local_command = new Command("stab", 60);
		System.out.println("Register Stab: '" + local_command.toString() + "'");
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
		long shouldExecute = local_command.shouldExecute(command);
		System.out.println("ShouldExecute " + shouldExecute);
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
				if (s != "" && uses != null && uses > 1)
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
					dust = ", the " + item.replace("a ", "").replace("A ", "").replace("an ", "").replace("the ", "") + " crumbles to dust.";
				}

				ArrayList<String> actions = new ArrayList<>();
				actions.add("stabs");
				actions.add("hits");
				actions.add(Helper.antiPing("slaps"));

				int action = Helper.getRandomInt(0, actions.size() - 1);
				System.out.println("Action: " + action);

				if (s == "")
					Helper.sendMessage(target ,  "\u0001ACTION flails at nothingness" + (!item.equals("") ? " with " : "") + item + "\u0001");
				else if (!s.equals(IRCBot.ournick))
					Helper.sendMessage(target ,  "\u0001ACTION " + actions.get(action) + " " + s + (!item.equals("") ? " with " : "") + item + " doing " + Helper.rollDice("1d20") + " damage" + dust + "\u0001");
				else
					Helper.sendMessage(target ,  "\u0001ACTION uses " + (!item.equals("") ? item : " an orbital death ray") + " to vaporize " + Helper.antiPing(nick) + dust + "\u0001");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (shouldExecute > 0)
			Helper.sendMessage(target ,  Helper.antiPing(nick) + ": " + "I cannot execute this command right now. Wait " + Helper.timeString(Helper.parse_seconds((int) shouldExecute)) + ".");
		else
			System.out.println("Unable to execute command '" + command + "' does not match '" + local_command.getCommand() + "' shouldExecute: " + local_command.shouldExecute(command));
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
