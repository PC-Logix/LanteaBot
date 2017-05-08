package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
		local_command_lookup = new Command("lookup", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				InetAddress[] inetAddressArray = null;
				try {
					inetAddressArray = InetAddress.getAllByName(params.get(0));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String output = "DNS Info for " + params.get(0) + " ";
				for (InetAddress anInetAddressArray : inetAddressArray) {
					output += anInetAddressArray;
				}
				Helper.sendMessage(target, output.replace(params.get(0) + "/", " ").replaceAll("((?::0\\b){2,}):?(?!\\S*\\b\\1:0\\b)(\\S*)", "::$2"), nick);
			}
		}; local_command_lookup.setHelpText("Returns DNS information");
		local_command_rdns = new Command("rdns", 0, true) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				InetAddress addr = null;
				try {
					addr = InetAddress.getByName(params.get(0));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String host = addr.getCanonicalHostName();
				String output = "Reverse DNS Info for " + params.get(0) + " " + host;
				Helper.sendMessage(target, output, nick);
			}
		}; local_command_rdns.setHelpText("Returns Reverse DNS information");
		IRCBot.registerCommand(local_command_lookup);
		local_command_lookup.registerSubCommand(local_command_rdns);
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command_lookup.tryExecute(command, nick, target, event, copyOfRange);
	}}
