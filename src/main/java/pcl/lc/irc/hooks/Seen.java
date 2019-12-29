package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

//Author: smbarbour

@SuppressWarnings("rawtypes")
public class Seen extends AbstractListener {
	String chan;
	String dest;

	private String formatTime(long delta) {
		StringBuilder duration = new StringBuilder();
		if (delta > 86400000L) {
			duration.append(Long.toString(delta / 86400000L)).append("d ");
			delta = delta % 86400000L;
		}
		if (delta > 3600000L) {
			duration.append(Long.toString(delta / 3600000L)).append("h ");
			delta = delta % 3600000L;
		}
		if (delta > 60000L) {
			duration.append(Long.toString(delta / 60000L)).append("m ");
			delta = delta % 60000L;
		}
		if (delta > 1000L) {
			duration.append(Long.toString(delta / 1000L)).append("s ");
		}
		if (duration.length() == 0) {
			duration.append("0s ");
		}
		return duration.toString();
	}

	@Override
	protected void initHook() {
		IRCBot.registerCommand("seen", "Tells you the last time a user was active.  Active means they sent a message");
		Database.addStatement("CREATE TABLE IF NOT EXISTS LastSeen(user PRIMARY KEY, timestamp)");
		Database.addUpdateQuery(4, "ALTER TABLE LastSeen ADD doing DEFAULT NULL");
		Database.addPreparedStatement("updateLastSeen","REPLACE INTO LastSeen(user, timestamp, doing) VALUES (?, ?, ?);");
		Database.addPreparedStatement("getLastSeen","SELECT timestamp, doing FROM LastSeen WHERE LOWER(user) = ? GROUP BY LOWER(user) ORDER BY timestamp desc");
		//I Have NO idea where these came from, or why they are here.
		//Database.addPreparedStatement("updateInfo","REPLACE INTO Info(key, data, doing) VALUES (?, ?, ?);");
		//Database.addPreparedStatement("getInfo","SELECT data FROM Info WHERE key = ?;");
		//Database.addPreparedStatement("getInfoAll","SELECT key, data FROM Info;");
		//Database.addPreparedStatement("removeInfo","DELETE FROM Info WHERE key = ?;");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		if (command.equals(Config.commandprefix + "seen")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		String message = "";
		for( int i = 0; i < copyOfRange.length; i++)
		{
			message = message + " " + copyOfRange[i];
		}
		message = message.trim();
		if (command.equals(Config.commandprefix + "seen")) {
			if (event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				dest = chan;
			} else {
				dest = "query";
			}
			try {
				PreparedStatement getSeen = Database.getPreparedStatement("getLastSeen");
				String target = copyOfRange[0];
				getSeen.setString(1, target.toLowerCase());
				ResultSet results = getSeen.executeQuery();
				if (results.next()) {
					if (dest.equals("query")) {
						event.respond(target + " was last seen " + formatTime(System.currentTimeMillis() - results.getLong(1)) + "ago. Saying: " + ((results.getString(2).isEmpty()) ? "No Record" : results.getString(2)));
					} else {
						Helper.sendMessage(dest, target + " was last seen " + formatTime(System.currentTimeMillis() - results.getLong(1)) + "ago. " + ((results.getString(2) == null) ? "No Record" : results.getString(2)));
					}
				} else {
					if (dest.equals("query")) {
						event.respond(target + " has not been seen");
					} else {
						event.getBot().sendIRC().message(dest, target + " has not been seen");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPart(final PartEvent event) {
		if (!event.getChannel().getMode().contains("s")) {
			User sender = event.getUser();
			try {
				PreparedStatement updateSeen = Database.getPreparedStatement("updateLastSeen");
				updateSeen.setString(1, sender.getNick().toLowerCase());
				updateSeen.setLong(2, System.currentTimeMillis());
				updateSeen.setString(3, "Parting");
				updateSeen.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onJoin(final JoinEvent event) {
		if (!event.getChannel().getMode().contains("s")) {
			User sender = event.getUser();
			try {
				PreparedStatement updateSeen = Database.getPreparedStatement("updateLastSeen");
				updateSeen.setString(1, sender.getNick().toLowerCase());
				updateSeen.setLong(2, System.currentTimeMillis());
				updateSeen.setString(3, "Joining");
				updateSeen.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onQuit(final QuitEvent event) {
		User sender = event.getUser();
		try {
			PreparedStatement updateSeen = Database.getPreparedStatement("updateLastSeen");
			updateSeen.setString(1, sender.getNick().toLowerCase());
			updateSeen.setLong(2, System.currentTimeMillis());
			updateSeen.setString(3, "Quitting");
			updateSeen.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onKick(final KickEvent event) {
		if (!event.getChannel().getMode().contains("s")) {
			User sender = event.getUser();
			try {
				PreparedStatement updateSeen = Database.getPreparedStatement("updateLastSeen");
				updateSeen.setString(1, sender.getNick().toLowerCase());
				updateSeen.setLong(2, System.currentTimeMillis());
				updateSeen.setString(3, "Being Kicked");
				updateSeen.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onAction(final ActionEvent event) throws Exception {
		if (!event.getChannel().getMode().contains("s")) {
			User sender = event.getUser();
			try {
				PreparedStatement updateSeen = Database.getPreparedStatement("updateLastSeen");
				updateSeen.setString(1, sender.getNick().toLowerCase());
				updateSeen.setLong(2, System.currentTimeMillis());
				updateSeen.setString(3, "Action: " + event.getAction());
				updateSeen.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		if (!event.getChannel().getMode().contains("s")) {
			try {
				PreparedStatement updateSeen = Database.getPreparedStatement("updateLastSeen");
				updateSeen.setString(1, sender.toLowerCase());
				updateSeen.setLong(2, System.currentTimeMillis());
				updateSeen.setString(3, "Saying: " + event.getMessage());
				updateSeen.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}