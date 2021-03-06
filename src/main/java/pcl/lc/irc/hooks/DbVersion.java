package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Forecaster
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
		local_command = new Command("dbversion") {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				ResultSet result = Database.ExecuteQuery("PRAGMA user_version");
				if (result.next()) {
					Helper.sendAction(target, "Database version: " + result.getString("user_version"));
				}
				return new CommandChainStateObject();
			}
		};
		local_command.setHelpText("Get current database version");
	}
}
