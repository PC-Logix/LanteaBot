package pcl.lc.irc.hooks;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
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
            checkTells.setString(1, event.getUser().getNick());
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
	protected void initCommands() {
		IRCBot.registerCommand("tell", "Sends a tell to the supplied user, with the supplied message " + Config.commandprefix + "tell Michiyo Hello!");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "tell")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
        PircBotX bot = event.getBot();
        String sender = nick;
        if (event.getMessage().startsWith(Config.commandprefix + "tell")) {
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
        		
                PreparedStatement addTell = IRCBot.getInstance().getPreparedStatement("addTell");
                String[] splitMessage = message.split(" ");
                if (splitMessage.length == 1) {
                    event.respond("Who did you want to tell?");
                    return;
                }
                String recipient = splitMessage[1];
                if (splitMessage.length == 2) {
                    event.respond("What did you want to say to " + recipient + "?");
                    return;
                }

                String channel = dest;
                SimpleDateFormat f = new SimpleDateFormat("MMM dd @ HH:mm");
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                String messageOut = StringUtils.join(splitMessage," ", 2, splitMessage.length) + " on " + f.format(new Date()) + " UTC";
                addTell.setString(1, sender);
                addTell.setString(2, recipient);
                addTell.setString(3, channel);
                addTell.setString(4, messageOut);
                addTell.executeUpdate();
                event.respond(recipient + " will be notified of this message when next seen.");
            } catch (Exception e) {
                e.printStackTrace();
                event.respond("An error occurred while processing this command (" + Config.commandprefix + "tell)");
            }
        }

	}
    public void onMessage(final MessageEvent event) {
        try {
            PreparedStatement checkTells = IRCBot.getInstance().getPreparedStatement("getTells");
            checkTells.setString(1, event.getUser().getNick());
            ResultSet results = checkTells.executeQuery();
            while (results.next()) {
                event.getBot().sendIRC().notice(event.getUser().getNick(), results.getString(2) + " in " + results.getString(3) + " said: " + results.getString(4));
            }
            PreparedStatement clearTells = IRCBot.getInstance().getPreparedStatement("removeTells");
            clearTells.setString(1, event.getUser().getNick());
            clearTells.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
