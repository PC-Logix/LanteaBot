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
import org.pircbotx.hooks.events.WhoisEvent;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.utils.Account;
import pcl.lc.utils.Account.ExpiringToken;

public class Permissions {
	public static int ADMIN = 4;
	public static int USER = 0;

	public static boolean hasPermission(PircBotX bot, GenericMessageEvent event, Integer minLevel) {
		return hasPermission(bot, event.getUser(), event, minLevel);
	}

	public static boolean hasPermission(PircBotX bot, User u) {
		return hasPermission(bot, u, null, null);
	}

	public static boolean hasPermission(PircBotX bot, User u, GenericMessageEvent event, Integer minLevel) {
		if (isOp(bot, u))
			return true;
		if (event != null && minLevel != null && getPermLevel(u, event) >= minLevel)
			return true;
		return false;
	}

	public static int getPermLevel(User u, GenericMessageEvent event) {
		String NSAccount = Account.getAccount(u, (MessageEvent) event);
		if (NSAccount == null) {
			return 0;
		}
		try {
			PreparedStatement getPerm = Database.getPreparedStatement("getUserPerms");
			getPerm.setString(1, NSAccount);
			getPerm.setString(2, ((MessageEvent) event).getChannel().getName());

			ResultSet results = getPerm.executeQuery();
			if (results.next()) {
				return results.getInt(1);
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static boolean setPermLevel(String user, GenericMessageEvent event, int level) {
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
			addPerm.setInt(3, level);
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
		String nsRegistration = "";
		if (Account.userCache.containsKey(user.getUserId()) && Account.userCache.get(user.getUserId()).getExpiration().after(Calendar.getInstance().getTime())) {
			nsRegistration = Account.userCache.get(user.getUserId()).getValue();
			IRCBot.log.debug(user.getNick() + " is cached");
		} else {
			IRCBot.log.debug(user.getNick() + " is NOT cached");
			user.isVerified();
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
				future.add(Calendar.MINUTE,10);
				Account.userCache.put(user.getUserId(), new ExpiringToken(future.getTime(),nsRegistration));
				IRCBot.log.debug(user.getUserId().toString() + " added to cache: " + nsRegistration + " expires at " + future.toString());
			}
		}
		if (IRCBot.instance.getOps().contains(nsRegistration)) {
			return true;
		} else {
			return false;
		}
	}
}
