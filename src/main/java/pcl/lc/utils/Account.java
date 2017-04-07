package pcl.lc.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.WhoisEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;

public class Account {
	public static Map<UUID,ExpiringToken> userCache = new HashMap<>();
	
	public static class ExpiringToken
	{
		private final Date expiration;
		private final String value;

		public ExpiringToken(Date expiration, String value) {
			this.expiration = expiration;
			this.value = value;
		}

		public Date getExpiration() {
			return expiration;
		}

		public String getValue() {
			return value;
		}

	}
	
	public static User getUserFromString(String user, MessageEvent event){
		Channel chan = event.getChannel();
		Iterator<User> it = chan.getUsers().iterator();
		while (it.hasNext()) {
			User userInfo = it.next();
			String userName = userInfo.getNick();
			if (userName.equals(user)) {
				return userInfo;
			}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")

	public static String getAccount(String u, MessageEvent event) {
		String user = null;
		if (IRCBot.authed.containsKey(u)) {
			return IRCBot.authed.get(u);
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u);
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				event.getUser().send().notice("Please enter a valid username!");
			}
			return user;
		}
	}

	public static String getAccount(String u, GenericMessageEvent event) {
		String user = null;
		if (IRCBot.authed.containsKey(u)) {
			return IRCBot.authed.get(u);
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u);
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				event.getUser().send().notice("Please enter a valid username!");
			}
			return user;
		}
	}
	
	public static String getAccount(User u, MessageEvent event) {
		String user = null;
		if (IRCBot.authed.containsKey(u.getNick())) {
			return IRCBot.authed.get(u.getNick());
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u.getNick());
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				IRCBot.log.error(ex.getMessage());
				waitForQueue.close();
				return null;
			}
			return user;
		}
	}

	@SuppressWarnings("rawtypes")
	public static String getAccount(User u, PrivateMessageEvent event) {
		String user = null;
		if (IRCBot.authed.containsKey(u.getNick())) {
			return IRCBot.authed.get(u.getNick());
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u.getNick());
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				IRCBot.log.error(ex.getMessage());
				waitForQueue.close();
				return null;
			}

			return user;
		}
	}

	@SuppressWarnings("rawtypes")
	public static String getAccount(User u, ActionEvent event) {
		String user = null;
		
		if (IRCBot.authed.containsKey(u.getNick())) {
			return IRCBot.authed.get(u.getNick());
		} else {
			event.getBot().sendRaw().rawLineNow("WHOIS " + u.getNick());
			WaitForQueue waitForQueue = new WaitForQueue(event.getBot());
			WhoisEvent test;
			try {
				test = waitForQueue.waitFor(WhoisEvent.class);
				waitForQueue.close();
				user = test.getRegisteredAs();
			} catch (InterruptedException ex) {
				IRCBot.log.error(ex.getMessage());
				waitForQueue.close();
				return null;
			}

			return user;
		}
	}
	
	@Deprecated
	public static boolean isOp(PircBotX sourceBot, User user) {
		return Permissions.isOp(sourceBot, user);
	}  
}
