/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
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
		ping = new Command("ping", new CommandArgumentParser(0, new CommandArgument("Nick", ArgumentTypes.LIST))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				ArrayList<String> targetUsers = this.argumentParser.getList("Nick");
				if (this.callingRelay != null && targetUsers.size() == 0) {
					Helper.sendMessage(target, "Sorry. You can't get your ping from over a bridge. You can ping irc users by passing one or more as arguments.", nick);
					return CommandChainState.FINISHED;
				}
				sendPing(targetUsers, nick, false, target);
				return CommandChainState.FINISHED;
			}
		}; ping.setHelpText("Sends a CTCP Ping to you, or the user supplied to check latency");
		ping.registerAlias("p");
		msp = new Command("msp", new CommandArgumentParser(0, new CommandArgument("Nick", ArgumentTypes.LIST))) {
			@Override
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				ArrayList<String> targetUsers = this.argumentParser.getList("Nick");
				if (this.callingRelay != null && targetUsers.size() == 0) {
					Helper.sendMessage(target, "Sorry. You can't get your ping from over a bridge. You can ping irc users by passing one or more as arguments.", nick);
					return CommandChainState.FINISHED;
				}
				sendPing(targetUsers, nick, true, target);
				return CommandChainState.FINISHED;
			}
		}; msp.setHelpText("Sends a CTCP Ping to you, or the user supplied to check latency, replies with milliseconds");
		msp.registerAlias("msping");
	}

	private void sendPing(ArrayList<String> params, String nick, boolean ms) {
		sendPing(params, nick, ms, null);
	}

	private void sendPing(ArrayList<String> params, String nick, boolean ms, String target) {
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
	@Override
	public void onNotice(final NoticeEvent event) {
		if (event.getNotice().startsWith("PING ")) {
			if (users.containsKey(event.getUser().getNick().toLowerCase())) {
				long currentTime = System.currentTimeMillis();
				String target = (String) users.get(event.getUser().getNick().toLowerCase()).get(0);
				Long timeStamp = (Long) users.get(event.getUser().getNick().toLowerCase()).get(1);
				float time = currentTime - timeStamp;

				DecimalFormat df = new DecimalFormat("#.##");

				Helper.sendMessage(target, "Ping reply from " + Helper.antiPing(event.getUser().getNick()) + " " + df.format(time / 1000) + "s");
				users.remove(event.getUser().getNick().toLowerCase());
			} else if (usersMSP.containsKey(event.getUser().getNick().toLowerCase())) {
				long currentTime = System.currentTimeMillis();
				String target = (String) usersMSP.get(event.getUser().getNick().toLowerCase()).get(0);
				Long timeStamp = (Long) usersMSP.get(event.getUser().getNick().toLowerCase()).get(1);
				float time = currentTime - timeStamp;
				Helper.sendMessage(target, "Ping reply from " + Helper.antiPing(event.getUser().getNick()) + " " + time + "ms");
				usersMSP.remove(event.getUser().getNick().toLowerCase());
			}
		}
	}
}
