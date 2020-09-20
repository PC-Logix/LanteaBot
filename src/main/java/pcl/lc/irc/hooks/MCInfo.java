/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
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
		local_command = new Command("mcinfo") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws IOException {
				String server = "";
				try {
					server = params.get(0);
				} catch (Exception e) {
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
				}
			}
		};
		local_command.setHelpText("Returns information about a MC Server");
	}
}