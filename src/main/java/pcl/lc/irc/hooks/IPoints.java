/**
 * 
 */
package pcl.lc.irc.hooks;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.WhoisEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("rawtypes")
public class IPoints extends ListenerAdapter {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String sender = event.getUser().getNick();
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.contains(prefix + "+")) {
				Pattern p = Pattern.compile("^\\+?\\d+");
				Matcher m = p.matcher(event.getMessage().replace(prefix,""));
				Long newPoints = 0L;
				if (m.find()) {
					String[] splitMessage = event.getMessage().split(" ");
					String recipient = splitMessage[1];
					if (!sender.equals(recipient)) {
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
			} else if (triggerWord.contains(prefix + "points") || triggerWord.equals(prefix + "points")) {
				String[] splitMessage = event.getMessage().split(" ");
				String user;
				System.out.println(splitMessage.length);
				if (splitMessage.length == 1) {
					user = event.getUser().getNick();
				} else {
					user = splitMessage[1];
				}

				if (getAccount(user, event) != null) {
					user = getAccount(user, event);
				}

				PreparedStatement getPoints = IRCBot.getInstance().getPreparedStatement("getPoints");
				getPoints.setString(1, user);
				ResultSet points = getPoints.executeQuery();
				if(points.next()){
					event.respond(user + " has " + points.getLong(1) + " points");
				} else {
					event.respond(user + " has 0 points");
				}
			}
		}
	}

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
}
