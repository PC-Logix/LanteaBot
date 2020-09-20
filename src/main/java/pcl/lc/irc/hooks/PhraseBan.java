package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelUserEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
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
	private Command set;

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
		local_command = new Command("phraseban") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				this.trySubCommandsMessage(params);
			}
		};
		local_command.setHelpText("Ban phrases, all of them");
		local_command.registerAlias("pb");

		add = new Command("add", Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				Statement statement;
				statement = Database.getConnection().createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM BannedPhrases WHERE phrase = '" + params.toLowerCase() + "'");
				if (result.next()) {
					Helper.sendMessage(target, "Phrase already banned.", nick);
					return;
				}
				statement.executeUpdate("INSERT INTO BannedPhrases (phrase) VALUES ('" + params.toLowerCase() + "')");
				Helper.sendMessage(target, "Added phrase to banlist", nick);
				phrases.add(params.toLowerCase());
			}
		};
		local_command.registerSubCommand(add);

		del = new Command("del", Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				Statement statement;
				statement = Database.getConnection().createStatement();
				statement.executeUpdate("DELETE FROM BannedPhrases WHERE phrase = '" + params.toLowerCase() + "'");
				Helper.sendMessage(target, "Removed phrase from banlist", nick);
				phrases.remove(params.toLowerCase());
			}
		};
		del.registerAlias("rem");
		local_command.registerSubCommand(del);

		list = new Command("list", Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "The words: " + String.join(", ", phrases));
			}
		};
		local_command.registerSubCommand(list);

		clear = new Command("clear", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				Statement statement = Database.getConnection().createStatement();
				statement.executeUpdate("DELETE FROM BannedPhrases");
				phrases.clear();
				Helper.sendMessage(target, "All banned phrases cleared!", nick);
			}
		};
		local_command.registerSubCommand(clear);

		exadd = new Command("exadd", Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws SQLException {
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				Statement statement = Database.getConnection().createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM ExemptNicks WHERE nick = '" + params.get(0).toLowerCase() + "'");
				if (result.next()) {
					Helper.sendMessage(target, "Nick already exempt.", nick);
					return;
				}
				statement.executeUpdate("INSERT INTO ExemptNicks (nick) VALUES ('" + params.get(0).toLowerCase() + "')");
				Helper.sendMessage(target, "Added to exempt list!", nick);
			}
		};
		local_command.registerSubCommand(exadd);

		exdel = new Command("exdel", Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws SQLException {
				if (params.isEmpty()) {
					Helper.sendMessage(target, "You must specify a nick.", nick);
					return;
				}
				Statement statement = Database.getConnection().createStatement();
				statement.executeUpdate("DELETE FROM ExemptNicks WHERE nick = '" + params.get(0).toLowerCase() + "'");
				Helper.sendMessage(target, "Removed from exempt list");
			}
		};
		exdel.registerAlias("exrem");
		local_command.registerSubCommand(exdel);

		set = new Command("set", Permissions.MOD) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				if (params.size() < 2) {
					Helper.sendMessage(target, "Specify setting to set then value. Settings: 'duration'", nick);
					return;
				}
				switch (params.get(0)) {
					case "duration":
						Database.storeJsonData("phraseban_duration", params.get(1));
						Helper.sendMessage(target, "Setting updated!");
						return;
					default:
						Helper.sendMessage(target, "Unknown setting '" + params.get(0) + "'", nick);
				}
			}
		};
		local_command.registerSubCommand(set);
	}

	public String chan;
	public String target = null;

	@Override
	public void handleMessage(String sender, MessageEvent event, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String[] copyOfRange) {
		String message = event.getMessage();
		try {
			Statement statement = Database.getConnection().createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM ExemptNicks WHERE nick = '" + nick.toLowerCase() + "'");
			if (result.next())
				return;

			if (Permissions.hasPermission(IRCBot.bot, event, Permissions.MOD))
				return;

			for (String phrase : phrases) {
				if (message.toLowerCase().contains(phrase)) {
					String duration = Database.getJsonData("phraseban_duration");
					if (duration.isEmpty())
						duration = "24h";
					TimedBans.setTimedBan(((GenericChannelUserEvent) event).getChannel(), nick, "", duration, "Banned phrase '" + phrase + "'", "PhraseBan");
					Helper.sendMessage("chanserv", "kickban " + target + " " + nick + " Banned phrase '" + phrase + "'");
					if (IRCBot.getOurNick().equals("ForeBot"))
						Helper.sendMessage(target, "BAN!");
					break;
				}
			}
		} catch (Exception e) {
			Helper.sendMessage(target, "An error occurred while updating values.");
			e.printStackTrace();
		}
	}
}
