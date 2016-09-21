/**
 * 
 */
package pcl.lc.irc.hooks;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.WhoisEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("rawtypes")
public class IPoints extends AbstractListener {


	public static String getAccount(String u, MessageEvent event) {
		String user = null;
		if (IRCBot.authed.containsKey(u)) {
			return IRCBot.authed.get(u);
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u);
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				event.getUser().send().notice("Please enter a valid username!");
			}
			return user;
		}
	}

	public static String getAccount(String u, GenericMessageEvent event) {
		String user = null;
		if (IRCBot.authed.containsKey(u)) {
			return IRCBot.authed.get(u);
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u);
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				event.getUser().send().notice("Please enter a valid username!");
			}
			return user;
		}
	}

	@Override
	protected void initCommands() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		String prefix = Config.commandprefix;
		if (command.contains(prefix + "+")) {
			Pattern p = Pattern.compile("^\\+?\\d+");
			Matcher m = p.matcher(event.getMessage().replace(prefix,""));
			Long newPoints = 0L;
			if (m.find()) {
				String[] splitMessage = event.getMessage().split(" ");
				String recipient = splitMessage[1];
				if (!nick.equals(recipient)) {
					try {
						PreparedStatement addPoints = IRCBot.getInstance().getPreparedStatement("addPoints");
						PreparedStatement getPoints = IRCBot.getInstance().getPreparedStatement("getPoints");
						PreparedStatement getPoints2 = IRCBot.getInstance().getPreparedStatement("getPoints");
						if (splitMessage.length == 1) {
							event.respond("Who did you want give points to?");
							return;
						}

						if (getAccount(recipient, event) != null) {
							recipient = getAccount(recipient, event);
						}

						getPoints.setString(1, recipient);
						ResultSet points = getPoints.executeQuery();
						if(points.next()){
							newPoints = points.getLong(1) + Long.parseLong(splitMessage[0].replaceAll("[^\\.0123456789]",""));
						} else {
							newPoints = Long.parseLong(splitMessage[0].replaceAll("[^\\.0123456789]",""));
						}

						addPoints.setString(1, recipient);
						addPoints.setDouble(2, newPoints);
						addPoints.executeUpdate();

						getPoints2.setString(1, recipient);
						ResultSet points2 = getPoints2.executeQuery();
						if(points.next()){
							event.respond(splitMessage[1] + " now has " + points2.getLong(1) + " points");
						} else {
							event.respond("Error getting " + splitMessage[1] + "'s points");      	
						}
					} catch (Exception e) {
						e.printStackTrace();
						event.respond("An error occurred while processing this command");
					}
				} else {
					event.respond("You can not give yourself points.");
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

			if (getAccount(user, event) != null) {
				user = getAccount(user, event);
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
						event.respond(user + " has " + points.getLong(1) + " points");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					event.respond(user + " has 0 points");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}