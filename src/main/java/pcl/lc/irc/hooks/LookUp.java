package pcl.lc.irc.hooks;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
public class LookUp extends AbstractListener {

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("lookup", "Returns DNS information");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "lookup")) {
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
		} else if (command.equals(Config.commandprefix + "rdns")) {
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
