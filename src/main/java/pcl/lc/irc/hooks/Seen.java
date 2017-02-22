package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

//Author: smbarbour

@SuppressWarnings("rawtypes")
public class Seen extends AbstractListener {
	String chan;
	String dest;

	/*    @Override
    public void onJoin(final JoinEvent event) throws Exception {
        User sender = event.getUser();
        try {
            PreparedStatement updateSeen = IRCBot.getInstance().getPreparedStatement("updateLastSeen");
            updateSeen.setString(1, sender.getNick());
            updateSeen.setLong(2, System.currentTimeMillis());
            updateSeen.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

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
	protected void initCommands() {
		IRCBot.registerCommand("seen", "Tells you the last time a user was active.  Active means they sent a message");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "seen")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
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
				PreparedStatement getSeen = IRCBot.getInstance().getPreparedStatement("getLastSeen");
				String target = copyOfRange[0];
				getSeen.setString(1, target.toLowerCase());
				ResultSet results = getSeen.executeQuery();
				if (results.next()) {
					if (dest.equals("query")) {
						event.respond(target + " was last seen " + formatTime(System.currentTimeMillis() - results.getLong(1)) + "ago.");
					} else {
						event.getBot().sendIRC().message(dest, target + " was last seen " + formatTime(System.currentTimeMillis() - results.getLong(1)) + "ago.");
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
	public void onAction(final ActionEvent event) throws Exception {
		User sender = event.getUser();
		try {
			PreparedStatement updateSeen = IRCBot.getInstance().getPreparedStatement("updateLastSeen");
			updateSeen.setString(1, sender.getNick().toLowerCase());
			updateSeen.setLong(2, System.currentTimeMillis());
			//updateSeen.setString(3, event.getAction());
			updateSeen.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		try {
			PreparedStatement updateSeen = IRCBot.getInstance().getPreparedStatement("updateLastSeen");
			updateSeen.setString(1, sender.toLowerCase());
			updateSeen.setLong(2, System.currentTimeMillis());
			//updateSeen.setString(3, event.getMessage());
			updateSeen.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}