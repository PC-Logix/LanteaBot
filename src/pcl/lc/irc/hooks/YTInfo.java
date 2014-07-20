/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import pcl.lc.utils.getVideoInfo;
/**
 * @author caitlyn
 *
 */
public class YTInfo extends ListenerAdapter {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String ourinput = event.getMessage().toLowerCase();
		String s = ourinput.trim();
		if (s.length() > 1) {
			if (event.getChannel().getName() != "#oc") {
				if (s.indexOf("youtube.com") != -1 || s.indexOf("youtu.be") != -1) {
					String vinfo = getVideoInfo.getVideoSearch(s, true, false);
					event.respond(vinfo);
				}
			}
		}
	}
}
