/**
 * 
 */
package pcl.lc.irc.hooks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
public class OCTime extends AbstractListener {
	Command local_command;

	@Override
	protected void initHook() {
		local_command = new Command("octime") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
				Helper.sendMessage(target, dateFormatGmt.format(new Date()), nick);
			}
		};
		local_command.setHelpText("Returns the time in GMT");
		local_command.registerAlias("time");
		IRCBot.registerCommand(local_command);
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
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
