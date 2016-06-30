/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class OnAction extends ListenerAdapter {
	public OnAction() {
		System.out.println("onAction listener loaded");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onAction(final ActionEvent event) throws Exception {
		super.onAction(event);
			if (event.getAction().contains("pets " + IRCBot.ournick)) {
				event.getChannel().send().action("Purrs");
			} else if (event.getAction().contains("glomps " + IRCBot.ournick)) {
				event.getChannel().send().action("gets out the pepper spray");
			} else {
				List<String> list = new ArrayList<String>();
				list.add(event.getChannel().getName().toString());
				list.add(event.getUser().getNick().toString());
				list.add("*** " + event.getMessage());
				IRCBot.messages.put(UUID.randomUUID(), list);
			}			
		}
	}
