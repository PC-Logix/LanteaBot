package pcl.lc.irc;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class IRCEventListener extends ListenerAdapter {

	@Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {
		
	}
	
	@Override
	public void onJoin(final JoinEvent event)  throws Exception {
		event.respond("Hello, world!");
		event.getBot().sendRaw().rawLine("/quit Goodbye, world!");
	}
}
