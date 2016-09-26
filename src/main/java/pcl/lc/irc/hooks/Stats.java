package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

@SuppressWarnings("rawtypes")
public class Stats extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("stats", "Get channel stats links");
	}


	@Override
	public void handleCommand(String sender, final MessageEvent event, String command, String[] args) {
		String prefix = Config.commandprefix;
		if (command.equals(prefix + "stats")) {
			if (args.length == 0) {
				event.respond("Stats: https://oclogs.pc-logix.com/stats.html");
			}
			if (args.length == 1) {
				String user = args[0];
				event.respond("Stats: https://oclogs.pc-logix.com/user?cid=oc&nick=" + user);
			}
		}
	}


	@Override
	public void handleCommand(String nick, GenericMessageEvent event,
			String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}
