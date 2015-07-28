package pcl.lc.irc.hooks;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

public class Thanks extends ListenerAdapter {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		
		if (event.getMessage().toLowerCase().contains(IRCBot.ournick)) {
			if (event.getMessage().toLowerCase().contains("thanks") || event.getMessage().toLowerCase().contains("thank you")) {
				event.respond("Your welcome!");
			}
		}
	}
	
}
