package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class DbVersion extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("dbversion", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					ResultSet result = Database.ExecuteQuery("PRAGMA user_version");
					if (result.next()) {
						Helper.sendAction(target, "Database version: " + result.getString("user_version"));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		local_command.setHelpText("Get current database version");
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
