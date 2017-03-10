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
		local_command = new Command("pet", 60 * 5);
		local_command.registerAlias("stroke");
		IRCBot.registerCommand(local_command, "Give pets");
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
				PreparedStatement statement = Database.getPreparedStatement("getRandomItem");
				ResultSet resultSet = statement.executeQuery();

				String item = "";
				Integer id = null;
				Integer uses = null;
				Boolean is_favourite = false;
				try
				{
					if (resultSet.next())
					{
						id = resultSet.getInt(1);
						item = resultSet.getString(2);
						uses = resultSet.getInt(3);
						is_favourite = resultSet.getBoolean(4);
					}
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				String s = message.trim();

				String dust = "";
				if (uses != null && uses != -1 && s != "" && s != IRCBot.ournick && !is_favourite)
				{
					statement = Database.getPreparedStatement("removeItemId");
					statement.setInt(1, id);
					statement.executeUpdate();
					System.out.println("Remove item " + id);
					dust = ", the " + StringEscapeUtils.unescapeHtml4(item).replace("a ", "").replace("A ", "").replace("an ", "").replace("the ", "") + " " + Inventory.getItemBreakString() + ".";
				}

				ArrayList<String> actions = new ArrayList<>();
				actions.add("pets");
				actions.add("brushes");

				DiceRoll roll = Helper.rollDice(uses + "d4");

				int action = Helper.getRandomInt(0, actions.size() - 1);
				System.out.println("Action: " + action);

				if (s == "")
					Helper.sendAction(target,"flails at nothingness" + (!item.equals("") ? " with " : "") + StringEscapeUtils.unescapeHtml4(item));
				else if (!s.equals(IRCBot.ournick))
					Helper.sendAction(target,actions.get(action) + " " + s + (!item.equals("") ? " with " : "") + StringEscapeUtils.unescapeHtml4(item) + "." + ((roll != null) ? " " + s + " recovers " + roll.getSum() + " health!" : "") + dust);
				else
					Helper.sendMessage(target,"I'm not going to pet myself in public. It'd be rude.", nick);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		} else {
			String result = local_command.getCannotExecuteReason(shouldExecute);
			if (result != "")
				Helper.sendMessage(target, result, nick);
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
