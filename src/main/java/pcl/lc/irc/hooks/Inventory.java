/**
 *
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
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

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("inventory", "Interact with the bots inventory");
	}

	public String dest;

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "inventory")) {
			chan = event.getChannel().getName();
		}
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "inventory")) {
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
			System.out.println("Arg: '" + argument + "'");

			if (strings[0].equals("add"))
			{
				try
				{
					PreparedStatement addItem = IRCBot.getInstance().getPreparedStatement("addItem");
					addItem.setString(1, argument);
					if (addItem.executeUpdate() > 0)
					{
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Added '" + argument + "' to inventory");
					}
					else
					{
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
					}
					return;
				}
				catch (Exception e)
				{
					e.printStackTrace();
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
					return;
				}
			}
			else if (strings[0].equals("remove"))
			{
				Integer id;
				PreparedStatement removeItem;
				try
				{
					id = Integer.valueOf(argument);

					try
					{
						removeItem = IRCBot.getInstance().getPreparedStatement("removeItemId");
						removeItem.setString(1, id.toString());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
						return;
					}
				}
				catch (NumberFormatException e)
				{
					try
					{
						removeItem = IRCBot.getInstance().getPreparedStatement("removeItemName");
						removeItem.setString(1, argument);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
						return;
					}
				}
				try
				{
					if (removeItem.executeUpdate() > 0)
          {
            IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Removed item from inventory");
          }
          else
          {
            IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
          }
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
				}
				return;
			}
			else if (strings[0].equals("list"))
			{
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
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " + "Wrong things happened!");
					return;
				}
			}
			else
			{
				IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + "Unknown sub-command '" + strings[0] + "' (Try: add, remove, list)");
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
