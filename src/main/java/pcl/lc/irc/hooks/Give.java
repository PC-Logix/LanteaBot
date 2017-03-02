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

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Give extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("give");
		IRCBot.registerCommand(local_command, "Gives stuff");
	}

	public String dest;

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
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		if (shouldExecute == 0) {
			String target_argument = copyOfRange[0];
			String item = "";
			for (int i = 1; i < copyOfRange.length; i++)
			{
				item += copyOfRange[i] + " ";
			}
			item = item.trim();

			if (item.equals("random"))
			{
				PreparedStatement getRandomItemNonFavourite = null;
				ResultSet resultSet = null;
				try
				{
					getRandomItemNonFavourite = Database.getPreparedStatement("getRandomItemNonFavourite");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Helper.sendMessage(target ,  "Something went wrong (1)", nick);
					return;
				}
				try
				{
					resultSet = getRandomItemNonFavourite.executeQuery();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					Helper.sendMessage(target ,  "Something went wrong (2)", nick);
					return;
				}

				try
				{
					if (resultSet != null && resultSet.next())
            item = resultSet.getString(2);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					Helper.sendMessage(target ,  "Something went wrong (3)", nick);
					return;
				}
			}

			int removeResult = Inventory.removeItem(item);

			if (removeResult == 0)
				Helper.sendAction(target ,  "gives " + target_argument + " " + item + " from her inventory");
			else if (removeResult == Inventory.ERROR_ITEM_IS_FAVOURITE)
				Helper.sendMessage(target ,  "No! This is my favourite thing! I wont give it away!", nick);
			else if (removeResult == Inventory.ERROR_NO_ROWS_RETURNED)
				Helper.sendMessage(target ,  "No item found to give away.", nick);
			else
				Helper.sendMessage(target ,  "Something went wrong (" + removeResult + ")", nick);
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
