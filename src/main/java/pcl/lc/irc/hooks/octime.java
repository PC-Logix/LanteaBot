/**
 * 
 */
package pcl.lc.irc.hooks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class octime extends ListenerAdapter {
	public octime() {
		IRCBot.registerCommand("octime");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);

		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "octime")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
					dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
					//Local time zone   
					SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
					//Time in GMT
					Date octime = dateFormatLocal.parse( dateFormatGmt.format(new Date()) );
					event.respond(octime.toString());
				} 
			}			
		}
	}
}
