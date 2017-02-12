/**
 * 
 */
package pcl.lc.irc.hooks;

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
public class Stab extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("stab", "Stab things with things");
	}

	public String dest;

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "stab")) {
			chan = event.getChannel().getName();
		}
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "stab")) {
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
			try
			{
				PreparedStatement statement = IRCBot.getInstance().getPreparedStatement("getItems");
				ResultSet resultSet = statement.executeQuery();

				String item = "";
				try
				{
					if (resultSet.next())
						item = " with " + resultSet.getString(2);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				String s = message.trim();
				if (!s.equals(IRCBot.ournick))
					IRCBot.getInstance().sendMessage(target ,  "\u0001ACTION stabs " + s + item + "\u0001");
				else
					IRCBot.getInstance().sendMessage(target ,  "\u0001ACTION stabs " + Helper.antiPing(nick) + item + "\u0001");
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
