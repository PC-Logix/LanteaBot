package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class PhraseBan extends AbstractListener {
	private Command local_command;
	private Command add;
	private Command del;
	private Command list;

	private ArrayList<String> phrases;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
		Database.addStatement("CREATE TABLE IF NOT EXISTS BannedPhrases(id INTEGER PRIMARY KEY, phrase VARCHAR(200), channel VARCHAR(200))");

		try {
			phrases = new ArrayList<>();
			Statement statement = Database.getConnection().createStatement();

			ResultSet resultSet = statement.executeQuery("SELECT * FROM BannedPhrases");

			while (resultSet.next()) {
				phrases.add(resultSet.getString("phrase"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void initCommands() {
		local_command = new Command("phraseban", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				super.onExecuteSuccess(command, nick, target, event, params);
			}
		};
		local_command.setHelpText("Ban phrases, all of them");
		local_command.registerAlias("pb");

		add = new Command("add", 0, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Statement statement;
				try {
					statement = Database.getConnection().createStatement();
					statement.executeUpdate("INSERT INTO BannedPhrases (phrase) VALUES ('" + params.toLowerCase() + "')");
					Helper.sendMessage(target, "Added phrase to banlist", nick);
					phrases.add(params.toLowerCase());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		local_command.registerSubCommand(add);

		del = new Command("del", 0, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Statement statement;
				try {
					statement = Database.getConnection().createStatement();
					statement.executeUpdate("DELETE FROM BannedPhrases WHERE phrase = '" + params.toLowerCase() + "'");
					Helper.sendMessage(target, "Removed phrase from banlist", nick);
					phrases.remove(params.toLowerCase());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		local_command.registerSubCommand(del);

		list = new Command("list", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "The words: " + String.join(", ", phrases));
			}
		};
		local_command.registerSubCommand(list);
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
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String[] copyOfRange) {
		String message = event.getMessage();

		if (!Permissions.hasPermission(IRCBot.bot, event, Permissions.MOD)) {
			for (String phrase : phrases) {
				if (message.toLowerCase().contains(phrase)) {
					Helper.sendMessage("chanserv", "kickban " + target + " " + nick + " Banned phrase '" + phrase + "'");
					break;
				}
			}
		}
	}
}
