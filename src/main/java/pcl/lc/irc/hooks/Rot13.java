/**
 * 
 */
package pcl.lc.irc.hooks;

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
public class Rot13 extends AbstractListener {
	private Command rot;

	@Override
	protected void initHook() {
		rot = new Command("rot13", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, rot13(params), nick);
			}
		};
		rot.setHelpText("Applies the ROT13 cipher to the supplied text");
		IRCBot.registerCommand(rot);
	}

	private static String rot13(String input) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if       (c >= 'a' && c <= 'm') c += 13;
			else if  (c >= 'A' && c <= 'M') c += 13;
			else if  (c >= 'n' && c <= 'z') c -= 13;
			else if  (c >= 'N' && c <= 'Z') c -= 13;
			sb.append(c);
		}
		return sb.toString();
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		rot.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}
}
