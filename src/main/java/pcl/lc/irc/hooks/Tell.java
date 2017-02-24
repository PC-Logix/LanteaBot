package pcl.lc.irc.hooks;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.Database;
import pcl.lc.irc.IRCBot;

//Author: smbarbour

@SuppressWarnings("rawtypes")
public class Tell extends AbstractListener {

	public String dest;

	public String chan;
	
	@Override
	public void onJoin(final JoinEvent event) {
		int numTells = 0;
        try {
            PreparedStatement checkTells = IRCBot.getInstance().getPreparedStatement("getTells");
            checkTells.setString(1, event.getUser().getNick().toLowerCase());
            ResultSet results = checkTells.executeQuery();
            while (results.next()) {
            	numTells++;
            }
            if (numTells > 0)
            	event.getUser().send().notice("You have " + numTells + " tell(s) currently waiting for you.");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	protected void initHook() {
		IRCBot.registerCommand("tell", "Sends a tell to the supplied user, with the supplied message " + Config.commandprefix + "tell Michiyo Hello!");
		Database.addStatement("CREATE TABLE IF NOT EXISTS Tells(id, sender, rcpt, channel, message, time)");
		Database.addPreparedStatement("addTell","INSERT INTO Tells(sender, rcpt, channel, message) VALUES (?, ?, ?, ?);");
		Database.addPreparedStatement("getTells","SELECT rowid, sender, channel, message FROM Tells WHERE LOWER(rcpt) = ?;");
		Database.addPreparedStatement("removeTells","DELETE FROM Tells WHERE LOWER(rcpt) = ?;");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "tell")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
        String sender = nick;
        if (command.equals(Config.commandprefix + "tell")) {
			if (event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				dest = chan;
			} else {
				dest = "query";
			}
			String message = "";
        	try {
    			for( int i = 0; i < copyOfRange.length; i++)
    			{
    				message = message + " " + copyOfRange[i];
    			}
        		message = message.trim();
                PreparedStatement addTell = Database.getPreparedStatement("addTell");
                if (copyOfRange.length == 0) {
                	event.getBot().sendIRC().message(dest, sender + ": " + "Who did you want to tell?");
                    return;
                }
                String recipient = copyOfRange[0];
                if (copyOfRange.length == 1) {
                	event.getBot().sendIRC().message(dest, sender + ": " + "What did you want to say to " + recipient + "?");
                    return;
                }

                String channel = dest;
                SimpleDateFormat f = new SimpleDateFormat("MMM dd @ HH:mm");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                String messageOut = StringUtils.join(copyOfRange," ", 1, copyOfRange.length) + " on " + f.format(new Date()) + " UTC";
                System.out.println(messageOut);
                addTell.setString(1, sender);
                addTell.setString(2, recipient.toLowerCase());
                addTell.setString(3, channel);
                addTell.setString(4, messageOut);
                addTell.executeUpdate();
            	event.getBot().sendIRC().message(dest, sender + ": " + recipient + " will be notified of this message when next seen.");
            } catch (Exception e) {
                e.printStackTrace();
            	event.getBot().sendIRC().message(dest, sender + ": " + "An error occurred while processing this command (" + Config.commandprefix + "tell)");
            }
        }

	}

	@Override
    public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
        try {
            PreparedStatement checkTells = IRCBot.getInstance().getPreparedStatement("getTells");
            checkTells.setString(1, sender.toLowerCase());
            ResultSet results = checkTells.executeQuery();
            while (results.next()) {
                event.getBot().sendIRC().notice(sender, results.getString(2) + " in " + results.getString(3) + " said: " + results.getString(4));
            }
            PreparedStatement clearTells = IRCBot.getInstance().getPreparedStatement("removeTells");
            clearTells.setString(1, sender.toLowerCase());
            clearTells.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
    	// TODO Auto-generated method stub
    	
    }
}
