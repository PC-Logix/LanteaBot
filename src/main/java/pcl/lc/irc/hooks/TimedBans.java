package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.Database;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Helper;

public class TimedBans extends AbstractListener {

	@Override
	protected void initHook() {
		Database.addStatement("CREATE TABLE IF NOT EXISTS TimedBans(channel, username, hostmask, expires, placedby, reason, type)");
		Database.addUpdateQuery(1, "ALTER TABLE TimedBans ADD type");
		Database.addPreparedStatement("addTimedBan", "INSERT INTO TimedBans(channel, username, hostmask, expires, placedby, reason, type) VALUES (?,?,?,?,?,?,?);");
		Database.addPreparedStatement("getTimedBans", "SELECT channel, username, hostmask, expires, placedby, reason, type FROM TimedBans WHERE expires <= ?;");
		Database.addPreparedStatement("delTimedBan", "DELETE FROM TimedBans WHERE expires = ? AND username = ? AND channel = ? AND type = ?;");
		IRCBot.registerCommand("tban", "Timed ban: %tban User Time Reason Ex: %tban MGR 24h Spamming");
		IRCBot.registerCommand("tquiet", "Timed quiet: %tquiet User Time Reason Ex: %tquiet MGR 24h Spamming");
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					long epoch = System.currentTimeMillis();
					PreparedStatement getTimedBans = Database.getPreparedStatement("getTimedBans");
					getTimedBans.setLong(1, epoch);
					ResultSet results = getTimedBans.executeQuery();
					if (results.next()) {
						IRCBot.getInstance();
						for (Channel chan : IRCBot.bot.getUserBot().getChannels()) {
							if (chan.getName().equals(results.getString(1))) {
								if (results.getString(7).equals("ban")){
									IRCBot.getInstance().sendMessage(results.getString(1), "Timed ban of " + results.getString(2) + " Expired. Placed by: " + results.getString(5));
									IRCBot.getInstance().sendMessage("chanserv", "unban " + results.getString(1) + " " + results.getString(2));
								} else {
									IRCBot.getInstance().sendMessage(results.getString(1), "Timed quiet of " + results.getString(2) + " Expired. Placed by: " + results.getString(5));
									IRCBot.getInstance().sendMessage("chanserv", "unquiet " + results.getString(1) + " " + results.getString(2));
								}
								PreparedStatement delTimedBan = Database.getPreparedStatement("delTimedBan");
								delTimedBan.setLong(1, results.getLong(4));
								delTimedBan.setString(2, results.getString(2));
								delTimedBan.setString(3, results.getString(1));
								delTimedBan.setString(4, results.getString(7));
								delTimedBan.execute();
							}
						}
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if ((command.equals(Config.commandprefix + "tban") || command.equals(Config.commandprefix + "timedban") || command.equals(Config.commandprefix + "tquiet")) && Permissions.hasPermission(IRCBot.bot, event, 4)) {
			String type;
			if (command.contains("ban")){
				type = "ban";
			} else {
				type = "quiet";
			}
			if (args[0].equals(IRCBot.getOurNick())) {
				event.getBot().sendIRC().message(event.getChannel().getName(),"No");
				return;
			}
			if (args.length < 3){
				event.getBot().sendIRC().message(event.getChannel().getName(), "format %tban Username Time Reason: %tban MGR 24h Being MGR");
				return;
			}
			String reason = "";
			try {
				for( int i = 2; i < args.length; i++)
				{
					reason = reason + " " + args[i];
				}
				reason = reason.trim();
				String hostname = null;
				long time = Helper.getFutureTime(args[1]);
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
				String expiresTime = sdf.format(new Date(time));
				PreparedStatement addTimedBan = Database.getPreparedStatement("addTimedBan");
				//channel, username, hostmask, expires, placedby, reason
				addTimedBan.setString(1, event.getChannel().getName());
				addTimedBan.setString(2, args[0]);
				for(User u : event.getChannel().getUsers()) {
					if (u.getNick().equals(args[0])) {
						hostname = u.getHostmask();
					}
				}
				addTimedBan.setString(3, hostname);
				addTimedBan.setLong(4, time);
				addTimedBan.setString(5, sender);
				addTimedBan.setString(6, reason);
				addTimedBan.setString(7, type);
				addTimedBan.executeUpdate();
				if (type.equals("ban")) {
					event.getBot().sendIRC().message("chanserv", "ban " + event.getChannel().getName() + " " + args[0]);
					if (event.getChannel().getUsers().contains(args[0])){
						event.getBot().sendIRC().message("chanserv", "kick " + event.getChannel().getName() + " " + " Reason: " + reason + " | For: " + args[1] + " | Expires: " + expiresTime);
					}
				} else {
					event.getBot().sendIRC().message("chanserv", "quiet " + event.getChannel().getName() + " " + args[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
				event.getBot().sendIRC().message(event.getChannel().getName(), sender + ": " + "An error occurred while processing this command (" + command + ")");
			}
		} else if (command.equals(Config.commandprefix + "tlist")) {
			//TODO: Actually add this!
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}

}
