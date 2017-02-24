/**
 * 
 */
package pcl.lc.irc.hooks;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.Database;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;
import pcl.lc.utils.Helper;

@SuppressWarnings("rawtypes")
public class IPoints extends AbstractListener {
	private String chan;

	@Override
	protected void initHook() {
		Database.addStatement("CREATE TABLE IF NOT EXISTS InternetPoints(nick STRING UNIQUE PRIMARY KEY, points)");
		Database.addPreparedStatement("getPoints", "SELECT Points FROM InternetPoints WHERE nick = ?;");
		Database.addPreparedStatement("addPoints", "INSERT OR REPLACE INTO InternetPoints VALUES (?, ?)");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.contains(Config.commandprefix + "+") || command.contains(Config.commandprefix + "points") || command.equals(Config.commandprefix + "points")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		String prefix = Config.commandprefix;
		String target;
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		if (command.contains(prefix + "+")) {
			Pattern p = Pattern.compile("^\\+?\\d+");
			Matcher m = p.matcher(event.getMessage().replace(prefix,""));
			BigDecimal newPoints = BigDecimal.ZERO;
			String message = "";
			for( int i = 0; i < copyOfRange.length; i++)
			{
				message = message + " " + copyOfRange[i];
			}
			if (m.find()) {
				String[] splitMessage = event.getMessage().split(" ");
				String recipient = copyOfRange[0];
				if (!nick.equals(recipient)) {
					try {
						PreparedStatement addPoints = IRCBot.getInstance().getPreparedStatement("addPoints");
						PreparedStatement getPoints = IRCBot.getInstance().getPreparedStatement("getPoints");
						PreparedStatement getPoints2 = IRCBot.getInstance().getPreparedStatement("getPoints");
						if (splitMessage.length == 1) {
							IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  "Who did you want give points to?");
							return;
						}

						if (Account.getAccount(recipient, event) != null) {
							recipient = Account.getAccount(recipient, event);
						}

						getPoints.setString(1, recipient);
						ResultSet points = getPoints.executeQuery();
						if(points.next()){
							newPoints = points.getBigDecimal(1).add(new BigDecimal(splitMessage[0].replaceAll("[^\\.0123456789]","")));
						} else {
							newPoints = new BigDecimal(splitMessage[0].replaceAll("[^\\.0123456789]",""));
						}

						addPoints.setString(1, recipient);
						addPoints.setBigDecimal(2, newPoints);
						addPoints.executeUpdate();

						getPoints2.setString(1, recipient);
						ResultSet points2 = getPoints2.executeQuery();
						if(points.next()){
							IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  splitMessage[1] + " now has " + points2.getBigDecimal(1) + " points");
						} else {
							IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  "Error getting " + splitMessage[1] + "'s points");      	
						}
					} catch (Exception e) {
						e.printStackTrace();
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  "An error occurred while processing this command");
					}
				} else {
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  "You can not give yourself points.");
				}
			}
		} else if (command.contains(prefix + "points") || command.equals(prefix + "points")) {
			String[] splitMessage = event.getMessage().split(" ");
			String user;
			if (splitMessage.length == 1) {
				user = event.getUser().getNick();
			} else {
				user = splitMessage[1];
			}

			if (Account.getAccount(user, event) != null) {
				user = Account.getAccount(user, event);
			}

			PreparedStatement getPoints = null;
			try {
				getPoints = IRCBot.getInstance().getPreparedStatement("getPoints");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				getPoints.setString(1, user);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ResultSet points = null;
			try {
				points = getPoints.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(points.next()){
					try {
						IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  user + " has " + points.getBigDecimal(1) + " points");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					IRCBot.getInstance().sendMessage(target, Helper.antiPing(nick) + ": " +  user + " has 0 points");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
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