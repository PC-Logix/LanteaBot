/**
 * 
 */
package pcl.lc.irc.hooks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class OCTime extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("octime", "Returns the time in GMT");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "octime")) {
			if (!IRCBot.isIgnored(nick)) {
				SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
				//Local time zone   
				SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				//Time in GMT
				Date octime = null;
				try {
					octime = dateFormatLocal.parse( dateFormatGmt.format(new Date()) );
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				event.respond(dateFormatGmt.format(new Date()));
			} 
		}	
	}
	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}
