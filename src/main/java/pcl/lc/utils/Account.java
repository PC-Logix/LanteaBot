package pcl.lc.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class Account {
	public static Map<UUID,ExpiringToken> userCache = new HashMap<>();
	
	private static class ExpiringToken
	{
		private final Date expiration;
		private final String value;

		private ExpiringToken(Date expiration, String value) {
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
				Logger.getLogger(MessageEvent.class.getName()).log(Level.SEVERE, null, ex);
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
				Logger.getLogger(MessageEvent.class.getName()).log(Level.SEVERE, null, ex);
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
				Logger.getLogger(MessageEvent.class.getName()).log(Level.SEVERE, null, ex);
				waitForQueue.close();
				return null;
			}

			return user;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isOp(PircBotX sourceBot, User user) {
		String nsRegistration = "";
		if (userCache.containsKey(user.getUserId()) && userCache.get(user.getUserId()).getExpiration().after(Calendar.getInstance().getTime())) {
			nsRegistration = userCache.get(user.getUserId()).getValue();
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
				userCache.put(user.getUserId(), new ExpiringToken(future.getTime(),nsRegistration));
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
