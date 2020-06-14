/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TimedHashMap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Ping extends AbstractListener {
	private Command ping;
	private Command msp;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(ping);
		IRCBot.registerCommand(msp);
	}

	private static TimedHashMap<String, List<Object>> users = new TimedHashMap<String, List<Object>>(60000, null);
	private static TimedHashMap<String, List<Object>> usersMSP = new TimedHashMap<String, List<Object>>(60000, null);

	private void initCommands() {
		ping = new Command("ping") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				sendPing(params, nick, false);
			}
		}; ping.setHelpText("Sends a CTCP Ping to you, or the user supplied to check latency");
		ping.registerAlias("p");
		msp = new Command("msp") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				sendPing(params, nick, true);
			}
		}; msp.setHelpText("Sends a CTCP Ping to you, or the user supplied to check latency, replies with milliseconds");
		msp.registerAlias("msping");
	}

	private void sendPing(ArrayList<String> params, String nick, boolean ms) {
		List<Object> eventData = new ArrayList<Object>();
		eventData.add(target);
		if (params.size() > 0) {
			for (String n : params) {
				((ms) ? usersMSP : users).put(n.toLowerCase(), eventData);
				IRCBot.bot.sendIRC().ctcpCommand(n.toLowerCase(), "PING " + System.currentTimeMillis());
			}
		} else {
			((ms) ? usersMSP : users).put(nick.toLowerCase(), eventData);
			IRCBot.bot.sendIRC().ctcpCommand(nick.toLowerCase(), "PING " + System.currentTimeMillis());
		}
		eventData.add(System.currentTimeMillis());
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		ping.tryExecute(command, nick, target, event, copyOfRange);
		msp.tryExecute(command, nick, target, event, copyOfRange);
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
				IRCBot.bot.sendIRC().message(channel, "Ping reply from " + Helper.antiPing(event.getUser().getNick()) + " " + time + "ms");
				usersMSP.remove(event.getUser().getNick().toLowerCase());
			}
		}
	}
}
