/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.QuitEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class NetSplitDetect extends ListenerAdapter {
	@Override
	public void onQuit(final QuitEvent event) {
		if(event.getReason().equals("*.net *.split")) {

		}
	}
}
