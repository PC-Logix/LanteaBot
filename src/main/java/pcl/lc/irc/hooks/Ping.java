/**
 *
 */
package pcl.lc.irc.hooks;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TimedHashMap;

/**
 * @author Caitlyn
 * 
 */
@SuppressWarnings("rawtypes")
public class Ping extends ListenerAdapter {
	public Ping() {
		IRCBot.registerCommand("p", "Sends a CTCP Ping to you, or the user supplied to check latency");
		IRCBot.registerCommand("ping", "Sends a CTCP Ping to you, or the user supplied to check latency");
		IRCBot.registerCommand("msp", "Sends a CTCP Ping to you, or the user supplied to check latency, replies with milliseconds");
	}

	public static TimedHashMap<String, List<Object>> users = new TimedHashMap<String, List<Object>>(60000, null);
	public static TimedHashMap<String, List<Object>> usersMSP = new TimedHashMap<String, List<Object>>(60000, null);

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] message = StringUtils.split(trigger);
			String triggerWord = message[0];
			if (triggerWord.equals(prefix + "p") || triggerWord.equals(prefix + "ping")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					List<Object> eventData = new ArrayList<Object>();
					eventData.add(event.getChannel().getName());
					eventData.add(System.currentTimeMillis());
					if (message.length > 1) {
						for (User u : event.getChannel().getUsers()) {
							if(u.getNick().toLowerCase().equals(message[1])) {
								users.put(message[1].toLowerCase(), eventData);
								IRCBot.bot.sendIRC().ctcpCommand(message[1].toLowerCase(), "PING " + System.currentTimeMillis());	
							}
						}
					} else {
						users.put(event.getUser().getNick().toLowerCase(), eventData);
						IRCBot.bot.sendIRC().ctcpCommand(event.getUser().getNick(), "PING " + System.currentTimeMillis());					
					}
				}
			} else if (triggerWord.equals(prefix + "msp") || triggerWord.equals(prefix + "msping")) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					List<Object> eventData = new ArrayList<Object>();
					eventData.add(event.getChannel().getName());
					eventData.add(System.currentTimeMillis());
					if (message.length > 1) {
						for (User u : event.getChannel().getUsers()) {
							if(u.getNick().toLowerCase().equals(message[1])) {
								usersMSP.put(message[1].toLowerCase(), eventData);
								IRCBot.bot.sendIRC().ctcpCommand(message[1].toLowerCase(), "PING " + System.currentTimeMillis());	
							}
						}
					} else {
						usersMSP.put(event.getUser().getNick().toLowerCase(), eventData);
						IRCBot.bot.sendIRC().ctcpCommand(event.getUser().getNick(), "PING " + System.currentTimeMillis());					
					}
				}
			}			
		}
	}

	@Override
	public void onNotice(final NoticeEvent event) {
		if (event.getNotice().startsWith("PING ")) {
			if (users.containsKey(event.getUser().getNick().toLowerCase())) {
				long currentTime = System.currentTimeMillis();
				String channel = (String) users.get(event.getUser().getNick().toLowerCase()).get(0);
				Long timeStamp = (Long) users.get(event.getUser().getNick().toLowerCase()).get(1);
				float time = currentTime - timeStamp;

				DecimalFormat df = new DecimalFormat("#.##");

				IRCBot.bot.sendIRC().message(channel, "Ping reply from " + Helper.antiPing(event.getUser().getNick()) + " " + df.format(time / 1000) + "s");
				users.remove(event.getUser().getNick().toLowerCase());
			} else if (usersMSP.containsKey(event.getUser().getNick().toLowerCase())) {
				long currentTime = System.currentTimeMillis();
				String channel = (String) usersMSP.get(event.getUser().getNick().toLowerCase()).get(0);
				Long timeStamp = (Long) usersMSP.get(event.getUser().getNick().toLowerCase()).get(1);
				float time = currentTime - timeStamp;

				DecimalFormat df = new DecimalFormat("#.##");

				IRCBot.bot.sendIRC().message(channel, "Ping reply from " + Helper.antiPing(event.getUser().getNick()) + " " + time + "ms");
				usersMSP.remove(event.getUser().getNick().toLowerCase());
			}
		}
	}
}
