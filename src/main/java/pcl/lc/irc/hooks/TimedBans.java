package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

public class TimedBans extends AbstractListener {
	Command command_timed;
	Command command_ban;
	Command command_quiet;
	Command command_list;

	@Override
	protected void initHook() {
		command_timed = new Command("timed", Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, trySubCommandsMessage(params), nick);
			}
		};
		command_ban = new Command("ban") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				if (params.size() < 2) {
					Helper.sendMessage(target, "You must specify a target, a time and optionally a reason.");
					return;
				}
				setTimedEvent("ban", nick, target, params.get(0), params.get(1), String.join(" ", Arrays.copyOfRange(params.toArray(new String[]{}), 2, params.size())), null);
			}
		};
		command_ban.setHelpText("Timed ban: %tban User Time Reason Ex: %tban MGR 24h Spamming");
		command_quiet = new Command("quiet") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				if (params.size() < 2) {
					Helper.sendMessage(target, "You must specify a target, a time and optionally a reason.");
					return;
				}
				setTimedEvent("quiet", nick, target, params.get(0), params.get(1), String.join(" ", Arrays.copyOfRange(params.toArray(new String[]{}), 2, params.size())), null);
			}
		};
		command_quiet.setHelpText("Timed quiet: %tquiet User Time Reason Ex: %tquiet MGR 24h Spamming");
		command_list = new Command("list") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				PreparedStatement getTimedBans = Database.getPreparedStatement("getTimedBansForChannel");
				getTimedBans.setString(1, target);
				ResultSet results = getTimedBans.executeQuery();
				int count = 0;
				while (results.next()) {
					count++;
//						IRCBot.getInstance();
					if (results.getString(7).equals("ban")) {
						Date date = new Date(results.getLong(4));
						DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
						String formatted = format.format(date);
						Helper.sendMessage(target, "Timed ban of " + results.getString(2) + " Expires at " + formatted + " UTC. Placed by: " + results.getString(5));
					} else {
						Date date = new Date(results.getLong(4));
						DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
						String formatted = format.format(date);
						Helper.sendMessage(target, "Timed quiet of " + results.getString(2) + " Expires at " + formatted + " UTC. Placed by: " + results.getString(5));
					}
				}
				if (count == 0)
					Helper.sendMessage(target, "There are no bans or quiets at the moment. Why not add a few?");
			}
		};
		command_list.setHelpText("List timed bans and quiets.");
		command_timed.registerSubCommand(command_ban);
		command_timed.registerSubCommand(command_quiet);
		command_timed.registerSubCommand(command_list);
		command_timed.registerAlias("tquiet", "quiet");
		command_timed.registerAlias("tban", "ban");
		command_timed.registerAlias("tlist", "list");
		IRCBot.registerCommand(command_timed);
		Database.addStatement("CREATE TABLE IF NOT EXISTS TimedBans(channel, username, hostmask, expires, placedby, reason, type)");
		Database.addUpdateQuery(1, "ALTER TABLE TimedBans ADD type");
		Database.addPreparedStatement("addTimedBan", "INSERT INTO TimedBans(channel, username, hostmask, expires, placedby, reason, type) VALUES (?,?,?,?,?,?,?);");
		Database.addPreparedStatement("getTimedBans", "SELECT channel, username, hostmask, expires, placedby, reason, type FROM TimedBans WHERE expires <= ?;");
		Database.addPreparedStatement("getTimedBansForChannel", "SELECT channel, username, hostmask, expires, placedby, reason, type FROM TimedBans WHERE channel <= ?;");
		Database.addPreparedStatement("delTimedBan", "DELETE FROM TimedBans WHERE expires = ? AND username = ? AND channel = ? AND type = ?;");
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					IRCBot.getInstance();
					if (IRCBot.bot != null) {
						long epoch = System.currentTimeMillis();
						PreparedStatement getTimedBans = Database.getPreparedStatement("getTimedBans");
						getTimedBans.setLong(1, epoch);
						ResultSet results = getTimedBans.executeQuery();
						if (results.next()) {
							IRCBot.getInstance();
							for (Channel chan : IRCBot.bot.getUserBot().getChannels()) {
								if (chan.getName().equals(results.getString(1))) {
									if (results.getString(7).equals("ban")) {
										Helper.sendMessage("chanserv", "unban " + results.getString(1) + " " + results.getString(3));
									} else {
										Helper.sendMessage("chanserv", "unquiet " + results.getString(1) + " " + results.getString(3));
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
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private void setTimedEvent(String type, String senderNick, String targetChannel, String targetNick, String timeStr, String reason, User[] users) throws Exception {
		String hostname = null;
		long time = Helper.getFutureTime(timeStr);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		String expiresTime = sdf.format(new Date(time));
		PreparedStatement addTimedBan = Database.getPreparedStatement("addTimedBan");
		//1 channel,2 username,3 hostmask,4 expires,5 placedby,6 reason,7 type
		addTimedBan.setString(1, targetChannel);
		addTimedBan.setString(2, targetNick);
		if (users != null) {
			for (User u : users) {
				if (u.getNick().equals(targetNick)) {
					hostname = "*!*@" + u.getHostname();
				}
			}
		}
		if (hostname == null) {
			hostname = targetNick;
		}
		addTimedBan.setString(3, hostname);
		addTimedBan.setLong(4, time);
		addTimedBan.setString(5, senderNick);
		addTimedBan.setString(6, reason);
		addTimedBan.setString(7, type);
		addTimedBan.executeUpdate();
		if (type.equals("ban")) {
			Helper.sendMessage("chanserv", "ban " + targetChannel + " " + targetNick);
			Helper.sendMessage("chanserv", "kick " + targetChannel + " " + targetNick + " Reason: " + reason + " | For: " + timeStr + " | Expires: " + expiresTime);
		} else {
			Helper.sendMessage("chanserv", "quiet " + targetChannel + " " + targetNick);
		}
	}

	public static void setTimedBan(Channel channel, String nick, String hostname, String length, String reason, String module) {
		try {
			reason = reason.trim();
			long time = Helper.getFutureTime(length);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
			String expiresTime = sdf.format(new Date(time));
			PreparedStatement addTimedBan = Database.getPreparedStatement("addTimedBan");
			//1 channel,2 username,3 hostmask,4 expires,5 placedby,6 reason,7 type
			addTimedBan.setString(1, channel.getName());
			addTimedBan.setString(2, nick);
			for (User u : channel.getUsers()) {
				if (u.getNick().equals(nick)) {
					hostname = u.getHostname();
				}
			}
			addTimedBan.setString(3, "*!*@" + hostname);
			addTimedBan.setLong(4, time);
			addTimedBan.setString(5, module);
			addTimedBan.setString(6, reason);
			addTimedBan.setString(7, "ban");
			addTimedBan.executeUpdate();
			IRCBot.bot.sendIRC().message("chanserv", "ban " + channel.getName() + " " + nick);
			IRCBot.bot.sendIRC().message("chanserv", "kick " + channel.getName() + " " + nick + " Reason: " + reason + " | For: " + length + " | Expires: " + expiresTime);

		} catch (Exception e) {
			e.printStackTrace();
			IRCBot.bot.sendIRC().message(channel.getName(), "DNSBL" + ": " + "An error occurred while processing this automated ban");
		}
	}
}
