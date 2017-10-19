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
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Account;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

@SuppressWarnings("rawtypes")
public class IPoints extends AbstractListener {
	private String target;
	private Command command_points;
	private Command command_reset_points;
	@Override
	protected void initHook() {
		Database.addStatement("CREATE TABLE IF NOT EXISTS InternetPoints(nick STRING UNIQUE PRIMARY KEY, points)");
		Database.addPreparedStatement("getPoints", "SELECT Points FROM InternetPoints WHERE nick = ?;");
		Database.addPreparedStatement("addPoints", "INSERT OR REPLACE INTO InternetPoints VALUES (?, ?)");
		Database.addPreparedStatement("setPoints", "INSERT OR REPLACE INTO InternetPoints VALUES (?, ?)");
		command_points = new Command("points", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
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
		}; command_points.setHelpText("Checks the points for yourself, or another user");
		command_reset_points = new Command("resetpoints", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					System.out.println(params);
					PreparedStatement setPoints = IRCBot.getInstance().getPreparedStatement("setPoints");
					String[] splitMessage = params.split(" ");
					String user;
					System.out.println(splitMessage.length);
					if (splitMessage[0].isEmpty()) {
						user = event.getUser().getNick();
					} else {
						user = splitMessage[0];
					}

					if (Account.getAccount(user, event) != null) {
						user = Account.getAccount(user, event);
					}
					
					setPoints.setString(1, user);
					setPoints.setBigDecimal(2, new BigDecimal(0));
					setPoints.executeUpdate();

					IRCBot.getInstance().sendMessage(target, nick + ": points reset");

				} catch (Exception e) {
					e.printStackTrace();
					IRCBot.getInstance().sendMessage(target, nick + ": " +  "An error occurred while processing this command");
				}
			}
		}; command_reset_points.setHelpText("Resets a users points, requires Bot Admin");
		IRCBot.registerCommand(command_points);
		IRCBot.registerCommand(command_reset_points);
	}
	@Override
	public void handleMessage(String sender, MessageEvent event, String message, String[] args) {
		System.out.println(message);
		Pattern p = Pattern.compile("(.+?)(\\+\\+)");
		Matcher m = p.matcher(message);
		if (m.matches() && event.getChannel().getUsersNicks().contains(m.group(1))) {
			target = event.getChannel().getName();
			String recipient = m.group(1);
			BigDecimal newPoints = BigDecimal.ZERO;
			if (!sender.equals(recipient)) {
				try {
					PreparedStatement addPoints = IRCBot.getInstance().getPreparedStatement("addPoints");
					PreparedStatement getPoints = IRCBot.getInstance().getPreparedStatement("getPoints");
					PreparedStatement getPoints2 = IRCBot.getInstance().getPreparedStatement("getPoints");

					if (Account.getAccount(recipient, event) != null) {
						recipient = Account.getAccount(recipient, event);
					}

					getPoints.setString(1, recipient);
					ResultSet points = getPoints.executeQuery();
					if(points.next()){
						newPoints = points.getBigDecimal(1).add(new BigDecimal(1));
					} else {
						newPoints = new BigDecimal(1);
					}

					addPoints.setString(1, recipient);
					addPoints.setBigDecimal(2, newPoints);
					addPoints.executeUpdate();

					getPoints2.setString(1, recipient);
					ResultSet points2 = getPoints2.executeQuery();
					if(points.next()){
						IRCBot.getInstance().sendMessage(target, sender + ": " +  recipient + " now has " + points2.getBigDecimal(1) + " points");
					} else {
						IRCBot.getInstance().sendMessage(target, sender + ": " +  "Error getting " + recipient + "'s points");      	
					}
				} catch (Exception e) {
					e.printStackTrace();
					IRCBot.getInstance().sendMessage(target, sender + ": " +  "An error occurred while processing this command");
				}
			} else {
				IRCBot.getInstance().sendMessage(target, sender + ": " +  "You can not give yourself points.");
			}
		}
	}
	
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.contains(Config.commandprefix + "+") || command.contains(Config.commandprefix + "points") || command.equals(Config.commandprefix + "points")) {
			target = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		command_points.tryExecute(command, nick, target, event, copyOfRange);
		command_reset_points.tryExecute(command, nick, target, event, copyOfRange);
	}
}