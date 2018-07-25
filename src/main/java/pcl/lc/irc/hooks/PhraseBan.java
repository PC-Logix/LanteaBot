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
	private Command clear;
	private Command exadd;
	private Command exdel;

	private ArrayList<String> phrases;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
		Database.addStatement("CREATE TABLE IF NOT EXISTS BannedPhrases(id INTEGER PRIMARY KEY, phrase VARCHAR(200), channel VARCHAR(200))");
		Database.addStatement("CREATE TABLE IF NOT EXISTS ExemptNicks(id INTEGER PRIMARY KEY, nick VARCHAR(200), channel VARCHAR(200))");

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
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				Statement statement;
				try {
					statement = Database.getConnection().createStatement();
					ResultSet result = statement.executeQuery("SELECT * FROM BannedPhrases WHERE phrase = '" + params.toLowerCase() + "'");
					if (result.next()) {
						Helper.sendMessage(target, "Phrase already banned.", nick);
						return;
					}
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
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
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
		del.registerAlias("rem");
		local_command.registerSubCommand(del);

		list = new Command("list", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "The words: " + String.join(", ", phrases));
			}
		};
		local_command.registerSubCommand(list);

		clear = new Command("clear", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					Statement statement = Database.getConnection().createStatement();
					statement.executeUpdate("DELETE FROM BannedPhrases");
					phrases.clear();
					Helper.sendMessage(target, "All banned phrases cleared!", nick);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		local_command.registerSubCommand(clear);

		exadd = new Command("exadd", 0, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				try {
					Statement statement = Database.getConnection().createStatement();
					ResultSet result = statement.executeQuery("SELECT * FROM ExemptNicks WHERE nick = '" + params.get(0) + "'");
					if (result.next()) {
						Helper.sendMessage(target, "Nick already exempt.", nick);
						return;
					}
					statement.executeUpdate("INSERT INTO ExemptNicks (nick) VALUES ('" + params.get(0) + "')");
					Helper.sendMessage(target, "Added to exempt list!", nick);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		local_command.registerSubCommand(exadd);

		exdel = new Command("exdel", 0, Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				try {
					Statement statement = Database.getConnection().createStatement();
					statement.executeUpdate("DELETE FROM ExemptNicks WHERE nick = '" + params.get(0) + "'");
					Helper.sendMessage(target, "Removed from exempt list");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		exdel.registerAlias("exrem");
		local_command.registerSubCommand(exdel);
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
		try {
			Statement statement = Database.getConnection().createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM ExemptNicks WHERE nick = '" + nick + "'");
			if (result.next())
				return;

			if (Permissions.hasPermission(IRCBot.bot, event, Permissions.MOD))
				return;

			for (String phrase : phrases) {
				if (message.toLowerCase().contains(phrase)) {
					Helper.sendMessage("chanserv", "kickban " + target + " " + nick + " Banned phrase '" + phrase + "'");
					if (IRCBot.getOurNick().equals("ForeBot"))
						Helper.sendMessage(target, "BAN!");
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
