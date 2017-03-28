package pcl.lc.irc.hooks;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

@SuppressWarnings("rawtypes")
public class Responses extends ListenerAdapter {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		
		if (event.getMessage().toLowerCase().contains(IRCBot.getOurNick().toLowerCase())) {
			if (event.getMessage().toLowerCase().contains("thanks") || event.getMessage().toLowerCase().contains("thank you")) {
				event.respond("You're welcome!");
			}

			if (event.getMessage().toLowerCase().contains("seriously") || event.getMessage().toLowerCase().contains("srsly") || event.getMessage().toLowerCase().contains("how dare you") || event.getMessage().toLowerCase().contains("howdareyou") || event.getMessage().toLowerCase().contains("no u") || event.getMessage().toLowerCase().contains("no you")) {
				event.respond(Helper.get_surprise_response());
			}
		}
	}
}
