/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Rainbow extends AbstractListener {
	private Command local_command;

	public String makeRainbow(String message) {
		Integer rainbow = 0;
		String messageOut = "";
		for (int i = 0; i < message.length(); i++){
			char c = message.charAt(i);
			if (rainbow == 0) {
				messageOut = messageOut + Colors.RED + c;
			} else if (rainbow == 1) {
				messageOut = messageOut + Colors.OLIVE + c;
			} else if (rainbow == 2) {
				messageOut = messageOut + Colors.YELLOW + c;
			} else if (rainbow == 3) {
				messageOut = messageOut + Colors.GREEN + c;
			} else if (rainbow == 4) {
				messageOut = messageOut + Colors.BLUE + c;
			} else if (rainbow == 5) {
				messageOut = messageOut + Colors.DARK_BLUE + c;
			} else if (rainbow == 6) {
				messageOut = messageOut + Colors.MAGENTA + c;
			}
			rainbow++;
			if (rainbow >= 6) {
				rainbow = 0;
			}
		}
		return messageOut;
	}

	@Override
	protected void initHook() {
		local_command = new Command("rainbow", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target,  Helper.antiPing(nick) + ": " + makeRainbow(params), nick);
			}
		}; local_command.setHelpText("Replies with a rainbow version of the supplied text");
		IRCBot.registerCommand(local_command);
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

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}
}
