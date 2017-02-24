/**
 *
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
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
public class Inventory extends AbstractListener {
	private static double favourite_chance = 0.01;

	static int ERROR_ITEM_IS_FAVOURITE = 1;
	static int ERROR_INVALID_STATEMENT = 2;
	static int ERROR_SQL_ERROR = 3;
	static int ERROR_NO_ROWS_RETURNED = 4;
	static int ERROR_ID_NOT_SET = 5;

	static int removeItem(String id)
	{
		return removeItem(id, false);
	}

	/**
	 * Will not remove the item marked as favourite unless override is true
	 * Returns 0 on success or higher on failure
	 * @param id_or_name String
	 * @param override_favourite boolean
	 * @return int
	 */
	private static int removeItem(String id_or_name, boolean override_favourite)
	{
		Integer id = null;
		Boolean id_is_string = true;
		PreparedStatement removeItem;
		try
		{
			id = Integer.valueOf(id_or_name);
			id_is_string = false;

			try
			{
				removeItem = IRCBot.getInstance().getPreparedStatement("removeItemId");
				removeItem.setString(1, id.toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return ERROR_INVALID_STATEMENT;
			}
		}
		catch (NumberFormatException e)
		{
			try
			{
				removeItem = IRCBot.getInstance().getPreparedStatement("removeItemName");
				removeItem.setString(1, id_or_name);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return ERROR_INVALID_STATEMENT;
			}
		}

		if (!override_favourite)
		{
			PreparedStatement getItem;
			System.out.println("id_is_string: " + id_is_string);
			System.out.println("id_or_name: " + id_or_name);
			System.out.println("id: " + id);
			try
			{
				if (id_is_string)
				{
					getItem = IRCBot.getInstance().getPreparedStatement("getItemByName");
					getItem.setString(1, id_or_name);
				}
				else if (id != null)
				{
					getItem = IRCBot.getInstance().getPreparedStatement("getItem");
					getItem.setInt(1, id);
				}
				else
					return ERROR_ID_NOT_SET;

				ResultSet result = getItem.executeQuery();

				if (result.next())
				{
					if (result.getBoolean(4))
						return ERROR_ITEM_IS_FAVOURITE;
				}
				else
					return ERROR_NO_ROWS_RETURNED;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return ERROR_INVALID_STATEMENT;
			}
		}

		try
		{
			if (removeItem.executeUpdate() > 0)
			{
				return 0;
			}
			else
			{
				return ERROR_NO_ROWS_RETURNED;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return ERROR_SQL_ERROR;
		}
	}

	private static String addItem(String item)
	{
		try
		{
			boolean favourite = false;
			int fav_roll = Helper.getRandomInt(0, 100);
			System.out.println("Favourite roll: " + fav_roll);
			if (fav_roll < (100 * favourite_chance))
			{
				System.out.println("New favourite! Clearing old favourite.");
				favourite = true;
				PreparedStatement clearFavourite = IRCBot.getInstance().getPreparedStatement("clearFavourite");
				clearFavourite.executeUpdate();
			}
			System.out.println("Favourites cleared, adding item");
			PreparedStatement addItem = IRCBot.getInstance().getPreparedStatement("addItem");
			addItem.setString(1, item);
			addItem.setInt(2, (favourite) ? 1 : 0);
			if (addItem.executeUpdate() > 0)
			{
				if (favourite)
					return "Added '" + item + "' to inventory. I love this! This is my new favourite thing!";
				else
					return "Added '" + item + "' to inventory.";
			}
			else
			{
				return "Wrong things happened! (1)";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "Wrong things happened! (2)";
		}
	}

	@Override
	protected void initHook() {
		IRCBot.registerCommand("inventory", "Interact with the bots inventory");
		Database.addStatement("CREATE TABLE IF NOT EXISTS Inventory(id INTEGER PRIMARY KEY, item_name, uses_left INTEGER)");
		Database.addPreparedStatement("getItems", "SELECT id, item_name, uses_left, is_favourite FROM Inventory;");
		Database.addPreparedStatement("getItem", "SELECT id, item_name, uses_left FROM Inventory WHERE id = ?;");
		Database.addPreparedStatement("getItemByName", "SELECT id, item_name, uses_left, is_favourite FROM Inventory WHERE item_name = ?;");
		Database.addPreparedStatement("getRandomItem", "SELECT id, item_name, uses_left, is_favourite FROM Inventory ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItemNonFavourite", "SELECT id, item_name, uses_left, is_favourite FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("addItem", "INSERT INTO Inventory (id, item_name, is_favourite) VALUES (NULL, ?, ?)");
		Database.addPreparedStatement("removeItemId", "DELETE FROM Inventory WHERE id = ?");
		Database.addPreparedStatement("removeItemName", "DELETE FROM Inventory WHERE item_name = ?");
		Database.addPreparedStatement("decrementUses", "UPDATE Inventory SET uses_left = uses_left - 1 WHERE id = ?");
		Database.addPreparedStatement("clearFavourite", "UPDATE Inventory SET is_favourite = 0 WHERE is_favourite = 1");
	}

	public String dest;

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "inventory") || command.equals(Config.commandprefix + "inv")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "inventory") || command.equals(Config.commandprefix + "inv")) {
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
			String message = "";
			for (String aCopyOfRange : copyOfRange)
			{
				message = message + " " + aCopyOfRange;
			}
			String s = message.trim();

			String[] strings = s.split(" ");
			String sub_command = strings[0];
			String argument = "";
			for (int i = 1; i < strings.length; i++)
			{
				argument += strings[i] + " ";
			}
			argument = argument.trim();

			switch (sub_command)
			{
				case "add":
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + addItem(argument));
					break;
				case "remove":
					int removeResult = removeItem(argument, true);
					if (removeResult == 0)
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Removed item from inventory");
					else
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened! (" + removeResult + ")");
					break;
				case "list":
					try
					{
						PreparedStatement statement = IRCBot.getInstance().getPreparedStatement("getItems");
						ResultSet resultSet = statement.executeQuery();
						String items = "";
						while (resultSet.next())
						{
							items += resultSet.getString(2) + ", ";
						}
						items = StringUtils.strip(items, ", ");
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + items);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened! (5)");
					}
					break;
				default:
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Unknown sub-command '" + sub_command + "' (Try: add, remove, list)");
					break;
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
