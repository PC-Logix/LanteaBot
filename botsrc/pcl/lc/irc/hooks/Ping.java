/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;

import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Ping extends ListenerAdapter {
	public Ping() {
		IRCBot.registerCommand("p");
		IRCBot.registerCommand("ping");
	}

	public static HashMap <String,List<Object>>users = new HashMap<String,List<Object>>(); 
	
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
			if (triggerWord.equals(prefix + "p") || triggerWord.equals(prefix + "ping")) {
			    List<Object> eventData = new ArrayList<Object>();  
			    eventData.add(event.getChannel().getName());   
			    eventData.add(System.currentTimeMillis());
				users.put(event.getUser().getNick(), eventData);
				IRCBot.bot.sendIRC().ctcpCommand(event.getUser().getNick(), "PING " + System.currentTimeMillis());
			}
		}
	}
	
	@Override
	public void onNotice(final NoticeEvent event) {
		if(event.getNotice().startsWith("PING ")) {
			if(users.containsKey(event.getUser().getNick())) {
				long currentTime = System.currentTimeMillis();
				String channel = (String) users.get(event.getUser().getNick()).get(0);
				Long timeStamp = (Long) users.get(event.getUser().getNick()).get(1);
				float time = currentTime - timeStamp;
				IRCBot.bot.sendIRC().message(channel, "Ping reply from " + event.getUser().getNick() + " " + time / 1000 + "s");
			}
		}
	}
}