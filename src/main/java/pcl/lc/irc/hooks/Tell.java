package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.inflector.Noun;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

//Author: smbarbour

@SuppressWarnings("rawtypes")
public class Tell extends AbstractListener {
	Command local_command;
	String dest;
	String chan;

	@Override
	protected void initHook() {
		local_command = new Command("tell") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				try {
					PreparedStatement addTell = Database.getPreparedStatement("addTell");
					if (params.size() == 0) {
						Helper.sendMessage(target, "Who did you want to tell?", nick);
						return;
					}
					String recipient = params.get(0);
					recipient = recipient.replaceAll("\\s*\\p{Punct}+\\s*$", "");
					if (params.size() == 1) {
						Helper.sendMessage(target, "What did you want to say to " + recipient + "?", nick);
						return;
					}
					String channel = dest;
					SimpleDateFormat f = new SimpleDateFormat("MMM dd @ HH:mm");
					f.setTimeZone(TimeZone.getTimeZone("UTC"));
					String messageOut = String.join(" ", params) + " on " + f.format(new Date()) + " UTC";
					addTell.setString(1, nick);
					addTell.setString(2, recipient.toLowerCase());
					addTell.setString(3, channel);
					addTell.setString(4, messageOut);
					addTell.executeUpdate();
					Helper.sendMessage(target, recipient + " will be notified of this message when next seen.", nick);
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "An error occurred while processing this command (" + Config.commandprefix + "tell)", nick);
				}
			}
		};
		local_command.setHelpText("Sends a tell to the supplied user, with the supplied message " + Config.commandprefix + "tell Michiyo Hello!");
		IRCBot.registerCommand(local_command);
		Database.addStatement("CREATE TABLE IF NOT EXISTS Tells(id, sender, rcpt, channel, message, time)");
		Database.addPreparedStatement("addTell","INSERT INTO Tells(sender, rcpt, channel, message) VALUES (?, ?, ?, ?);");
		Database.addPreparedStatement("getTells","SELECT rowid, sender, channel, message FROM Tells WHERE LOWER(rcpt) = ?;");
		Database.addPreparedStatement("removeTells","DELETE FROM Tells WHERE LOWER(rcpt) = ?;");
	}

	@Override
	public void onJoin(final JoinEvent event) {
		if (event.getUser().getNick().equals(IRCBot.getOurNick()))
			return;
		int numTells = 0;
		try {
			PreparedStatement checkTells = Database.getPreparedStatement("getTells");
			checkTells.setString(1, event.getUser().getNick().toLowerCase());
			ResultSet results = checkTells.executeQuery();
			while (results.next()) {
				numTells++;
			}
			if (numTells > 0) {
				event.getUser().send().notice("You have " + numTells + " " + Noun.pluralOf("tell", numTells) + " currently waiting for you.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		if (command.equals(Config.commandprefix + "tell")) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		String sender = nick;
		if (command.equals(Config.commandprefix + "tell")) {
			if (event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				dest = chan;
			} else {
				dest = "query";
			}
			String message = "";
		}

	}

	@Override
	public void handleMessage(String sender, MessageEvent event,  String[] args) {
		try {
			String nick = "";
			if (nick.contains("@")) {
				nick = "@" + sender;
				PreparedStatement checkTells = Database.getPreparedStatement("getTells");
				checkTells.setString(1, nick.toLowerCase());
				ResultSet results = checkTells.executeQuery();
				while (results.next()) {
					Helper.sendMessage("Corded", nick.replace("@", "") +": " + results.getString(2) + " in " + results.getString(3) + " said: " + results.getString(4));
				}
				PreparedStatement clearTells = Database.getPreparedStatement("removeTells");
				clearTells.setString(1, nick.toLowerCase());
				clearTells.execute();
			} else {
				PreparedStatement checkTells = Database.getPreparedStatement("getTells");
				checkTells.setString(1, sender.toLowerCase());
				ResultSet results = checkTells.executeQuery();
				while (results.next()) {
					Helper.sendNotice(sender, results.getString(2) + " in " + results.getString(3) + " said: " + results.getString(4));
				}
				PreparedStatement clearTells = Database.getPreparedStatement("removeTells");
				clearTells.setString(1, sender.toLowerCase());
				clearTells.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
