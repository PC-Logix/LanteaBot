package pcl.lc.irc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.WhoisEvent;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.utils.Account;
import pcl.lc.utils.Database;
import pcl.lc.utils.Account.ExpiringToken;
import sun.net.www.content.text.Generic;

public class Permissions {
	public static String EVERYONE = "Nobody";
	public static String TRUSTED = "Trusted";
	public static String MOD = "Moderator";
	public static String ADMIN = "Admin";
	// "ranks" contains each of the above permissions in order of magnitude with lowest first, highest level at the end
	public static String[] ranks = new String[]{
		EVERYONE,
		TRUSTED,
		MOD,
		ADMIN,
	};

	public static boolean hasPermission(PircBotX bot, GenericMessageEvent event, String minRank) {
		return hasPermission(bot, event.getUser(), event, minRank);
	}

	public static boolean hasPermission(PircBotX bot, User u) {
		return hasPermission(bot, u, null, null);
	}

	public static boolean hasPermission(PircBotX bot, User u, GenericMessageEvent event, String minRank) {
		if (minRank == null)
			return true;
		if (isOp(bot, u))
			return true;
		if (event != null && getPermLevel(u, event) >= getPermLevel(minRank))
			return true;
		return false;
	}

	public static int getPermLevel(String rank) {
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].equals(rank))
				return i;
		}
		return 0;
	}

	public static int getPermLevel(User u, GenericMessageEvent event) {
		String target = "";
		String NSAccount = "";
		if (event instanceof MessageEvent) {
			NSAccount = Account.getAccount(u, (MessageEvent) event);
			target = ((MessageEvent) event).getChannel().getName();
		} else if (event instanceof PrivateMessageEvent) {
			return 0;
		}
		if (NSAccount == null) {
			return 0;
		}
		try {
			PreparedStatement getPerm = Database.getPreparedStatement("getUserPerms");
			getPerm.setString(1, NSAccount);
			getPerm.setString(2, target);

			ResultSet results = getPerm.executeQuery();
			if (results.next()) {
				String rank = results.getString(1);
				for (int i = 0; i < ranks.length; i++) {
					if (ranks[i].equals(rank))
						return i;
				}
				return 0;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static String getRanks() {
		String ranksString = "";
		for (String rank : ranks){
			ranksString += ", " + rank;
		}
		return ranksString.replace(", ", "");
	}

	public static String getRank(GenericMessageEvent event) {
		User u = event.getUser();
		String target = "";
		String NSAccount = "";
		if (event instanceof MessageEvent) {
			NSAccount = Account.getAccount(u, (MessageEvent) event);
			target = ((MessageEvent) event).getChannel().getName();
		} else if (event instanceof PrivateMessageEvent) {
			return "";
		}
		if (NSAccount == null) {
			return "";
		}
		try {
			PreparedStatement getPerm = Database.getPreparedStatement("getUserPerms");
			getPerm.setString(1, NSAccount);
			getPerm.setString(2, target);

			ResultSet results = getPerm.executeQuery();
			if (results.next()) {
				return results.getString(1);
			} else {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static boolean setPermLevel(String user, GenericMessageEvent event, String level) {
		User u = Account.getUserFromString(user, (MessageEvent) event);
		if (u == null) {
			return false;
		}
		String NSAccount = Account.getAccount(u, (MessageEvent) event);
		try {
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			PreparedStatement addPerm = Database.getPreparedStatement("setPermLevel");
			addPerm.setString(1, NSAccount);
			addPerm.setString(2, ((MessageEvent) event).getChannel().getName());
			addPerm.setString(3, level);
			addPerm.setString(4, event.getUser().getNick());
			addPerm.setString(5, dateFormatGmt.format(new Date()));
			if (addPerm.executeUpdate() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isOp(PircBotX sourceBot, User user) {
		long startTime = System.currentTimeMillis();
		String nsRegistration = "";
		if (IRCBot.getDebug())
			System.out.println(Thread.currentThread().getStackTrace()[2].getClassName() +"#"+ Thread.currentThread().getStackTrace()[2].getMethodName());
		if (Account.userCache.containsKey(user.getUserId()) && Account.userCache.get(user.getUserId()).getExpiration().after(Calendar.getInstance().getTime())) {
			nsRegistration = Account.userCache.get(user.getUserId()).getValue();
			Calendar future = Calendar.getInstance();
			if (!IRCBot.getDebug()) {
				System.out.println("Not Debugging setting cache to 10 hours");
				future.add(Calendar.HOUR,10);
			} else {
				System.out.println("Debugging setting cache to 30 seconds");
				future.add(Calendar.SECOND,30);
			}
			Account.userCache.put(user.getUserId(), new ExpiringToken(future.getTime(),nsRegistration));
			IRCBot.log.debug(user.getNick() + " is cached");
		} else {
			IRCBot.log.debug(user.getNick() + " is NOT cached");
			try {
				sourceBot.sendRaw().rawLine("WHOIS " + user.getNick() + " " + user.getNick());
				WaitForQueue waitForQueue = new WaitForQueue(sourceBot);
				WhoisEvent whoisEvent = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				if (whoisEvent.getRegisteredAs() != null) {
					nsRegistration = whoisEvent.getRegisteredAs();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!nsRegistration.isEmpty()) {
				Calendar future = Calendar.getInstance();
				if (!IRCBot.getDebug())
					future.add(Calendar.MINUTE,10);
				else
					future.add(Calendar.SECOND,30);
				Account.userCache.put(user.getUserId(), new ExpiringToken(future.getTime(),nsRegistration));
				IRCBot.log.debug(user.getUserId().toString() + " added to cache: " + nsRegistration + " expires at " + future.getTime().toString());
			}
		}
		long endTime = System.currentTimeMillis();
		if (IRCBot.getDebug())
			System.out.println("That took " + (endTime - startTime) + " milliseconds");
		if (IRCBot.instance.getOps().contains(nsRegistration)) {
			return true;
		} else {
			return false;
		}
	}
}
