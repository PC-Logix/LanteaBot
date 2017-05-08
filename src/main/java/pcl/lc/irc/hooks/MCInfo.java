/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.mcping.MinecraftPing;
import pcl.lc.utils.mcping.MinecraftPingReply;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class MCInfo extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("mcinfo", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String server = "";
				try {
					server = params.get(0);
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "No server specified. Specify an address or IP.");
					return;
				}
				String port = "25565";
				try {
					port = params.get(1);
				} catch (Exception ignored) {}
				System.out.println(server + ":" + port);
				MinecraftPingReply data;
				try {
					data = new MinecraftPing().getPing(server, Integer.parseInt(port));
					Helper.sendMessage(target, "Server info: Version: " + data.getVersion() + " MOTD: " + data.getMotd() + " Players: " + data.getOnlinePlayers() + "/" + data.getMaxPlayers(), nick);
				} catch (UnknownHostException e) {
					Helper.sendMessage(target, "Server could not be found.", nick);
				} catch (SocketTimeoutException e) {
					Helper.sendMessage(target, "Connection timed out on port " + port + ". " + ((port == "25565") ? "Does the server use a custom port?" : "Did you specify the right port?"), nick);
				} catch (IOException e) {
					e.printStackTrace();
					Helper.sendMessage(target, "Something went wrong.", nick);
				}
			}
		};
		local_command.setHelpText("Returns information about a MC Server");
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
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}