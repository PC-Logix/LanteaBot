/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.Database;
import pcl.lc.irc.IRCBot;
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

	@Override
	protected void initHook() {
		IRCBot.registerCommand("give", "Gives stuff");
	}

	public String dest;

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "give")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "give")) {
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
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
					IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "Something went wrong (1)");
					return;
				}
				try
				{
					resultSet = getRandomItemNonFavourite.executeQuery();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "Something went wrong (2)");
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
					IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "Something went wrong (3)");
					return;
				}
			}

			int removeResult = Inventory.removeItem(item);

			if (removeResult == 0)
				IRCBot.getInstance().sendMessage(target ,  "\u0001ACTION gives " + target_argument + " " + item + " from her inventory\u0001");
			else if (removeResult == Inventory.ERROR_ITEM_IS_FAVOURITE)
				IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "No! This is my favourite thing! I wont give it away!");
			else if (removeResult == Inventory.ERROR_NO_ROWS_RETURNED)
				IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "No item found to give away.");
			else
				IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "Something went wrong (" + removeResult + ")");
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
