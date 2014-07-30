package pcl.lc.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.pircbotx.User;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.WhoisEvent;

import pcl.lc.irc.IRCBot;

public class Account {
	@SuppressWarnings("rawtypes")
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
				event.getUser().send().notice("Please enter a valid username!");
			}

			return user;
		}

	}
}
