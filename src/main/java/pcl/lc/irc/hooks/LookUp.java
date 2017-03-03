package pcl.lc.irc.hooks;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class LookUp extends AbstractListener {
	private Command local_command_lookup;
	private Command local_command_rdns;

	@Override
	protected void initHook() {
		local_command_lookup = new Command("lookup", 0);
		local_command_rdns = new Command("rdns", 0);
		IRCBot.registerCommand(local_command_lookup, "Returns DNS information");
		IRCBot.registerCommand(local_command_rdns, "Returns Reverse DNS information");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (local_command_lookup.shouldExecuteBool(command)) {
			chan = event.getChannel().getName();
		}
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		if (local_command_lookup.shouldExecuteBool(command, nick)) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				InetAddress[] inetAddressArray = null;
				try {
					inetAddressArray = InetAddress.getAllByName(copyOfRange[0]);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String output = "DNS Info for " + copyOfRange[0] + " ";
				for (int i = 0; i < inetAddressArray.length; i++) {
					output += inetAddressArray[i];
				}
				event.respond(output.replace(copyOfRange[0] + "/", " ").replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2"));
			}
		} else if (local_command_rdns.shouldExecuteBool(command, nick)) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				InetAddress addr = null;
				try {
					addr = InetAddress.getByName(copyOfRange[0]);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String host = addr.getCanonicalHostName();
				String output = "Reverse DNS Info for " + copyOfRange[0] + " " + host;
				event.respond(output);
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
