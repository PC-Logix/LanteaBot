package pcl.lc.irc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.utils.Account;

public class Permissions {


	public static int getPermLevel(User u, MessageEvent event) {
		String NSAccount = Account.getAccount(u, event);
		try {
			PreparedStatement getPerm = IRCBot.getInstance().getPreparedStatement("getUserPerms");
			getPerm.setString(1, NSAccount.toLowerCase());
			getPerm.setString(2, event.getChannel().getName());

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

	public static boolean setPermLevel(String user, MessageEvent event, int level) {
		User u = Account.getUserFromString(user, event);
		if (u == null) {
			return false;
		}
		String NSAccount = Account.getAccount(u, event);
		try {
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			PreparedStatement addPerm = IRCBot.getInstance().getPreparedStatement("setPermLevel");
			addPerm.setString(1, NSAccount);
			addPerm.setString(2, event.getChannel().getName());
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
}
