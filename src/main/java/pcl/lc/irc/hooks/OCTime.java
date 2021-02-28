/**
 * 
 */
package pcl.lc.irc.hooks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.CommandChainState;
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
			public CommandChainState onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
				Helper.sendMessage(target, dateFormatGmt.format(new Date()), nick);
				return CommandChainState.FINISHED;
			}
		};
		local_command.setHelpText("Returns the time in GMT");
		local_command.registerAlias("time");
		IRCBot.registerCommand(local_command);
	}
}
