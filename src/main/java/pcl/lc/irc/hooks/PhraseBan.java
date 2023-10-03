package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelUserEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
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
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				this.trySubCommandsMessage(params);
				return new CommandChainStateObject();
			}
		};
		local_command.setHelpText("Ban phrases, all of them");
		local_command.registerAlias("pb");

		add = new Command("add", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Phrase")), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				String phrase = this.argumentParser.getArgument("Phrase");
				Statement statement;
				statement = Database.getConnection().createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM BannedPhrases WHERE phrase = '" + phrase.toLowerCase() + "'");
				if (result.next()) {
					Helper.sendMessage(target, "Phrase already banned.", nick);
					return new CommandChainStateObject(CommandChainState.ERROR, "Prase already banned");
				}
				statement.executeUpdate("INSERT INTO BannedPhrases (phrase) VALUES ('" + phrase.toLowerCase() + "')");
				Helper.sendMessage(target, "Added phrase to banlist", nick);
				phrases.add(params.toLowerCase());
				return new CommandChainStateObject();
			}
		};
		local_command.registerSubCommand(add);

		del = new Command("del", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Phrase")), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				String phrase = this.argumentParser.getArgument("Phrase");
				Statement statement;
				statement = Database.getConnection().createStatement();
				statement.executeUpdate("DELETE FROM BannedPhrases WHERE phrase = '" + phrase.toLowerCase() + "'");
				Helper.sendMessage(target, "Removed phrase from banlist", nick);
				phrases.remove(params.toLowerCase());
				return new CommandChainStateObject();
			}
		};
		del.registerAlias("rem");
		local_command.registerSubCommand(del);

		list = new Command("list", Permissions.EVERYONE) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "The words: " + String.join(", ", phrases));
				return new CommandChainStateObject();
			}
		};
		local_command.registerSubCommand(list);

		clear = new Command("clear", Permissions.ADMIN) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws SQLException {
				Statement statement = Database.getConnection().createStatement();
				statement.executeUpdate("DELETE FROM BannedPhrases");
				phrases.clear();
				Helper.sendMessage(target, "All banned phrases cleared!", nick);
				return new CommandChainStateObject();
			}
		};
		local_command.registerSubCommand(clear);

		exadd = new Command("exadd", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Phrase")), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws SQLException {
				String phrase = this.argumentParser.getArgument("Phrase");
				Statement statement = Database.getConnection().createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM ExemptNicks WHERE nick = '" + phrase.toLowerCase() + "'");
				if (result.next()) {
					Helper.sendMessage(target, "Nick already exempt.", nick);
					return null;
				}
				statement.executeUpdate("INSERT INTO ExemptNicks (nick) VALUES ('" + phrase.toLowerCase() + "')");
				Helper.sendMessage(target, "Added to exempt list!", nick);
				return new CommandChainStateObject();
			}
		};
		local_command.registerSubCommand(exadd);

		exdel = new Command("exdel", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING, "Phrase")), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws SQLException {
				String phrase = this.argumentParser.getArgument("Phrase");
				Statement statement = Database.getConnection().createStatement();
				statement.executeUpdate("DELETE FROM ExemptNicks WHERE nick = '" + phrase.toLowerCase() + "'");
				Helper.sendMessage(target, "Removed from exempt list");
				return new CommandChainStateObject();
			}
		};
		exdel.registerAlias("exrem");
		local_command.registerSubCommand(exdel);

		set = new Command("set", new CommandArgumentParser(2, new CommandArgument(ArgumentTypes.STRING, "Setting"), new CommandArgument(ArgumentTypes.STRING, "Value")), Permissions.MOD) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) throws Exception {
				String setting = this.argumentParser.getArgument("Setting");
				String value = this.argumentParser.getArgument("Value");
				switch (setting.toLowerCase()) {
					case "duration":
						Database.storeJsonData("phraseban_duration", value);
						Helper.sendMessage(target, "Setting updated!");
						return null;
					default:
						Helper.sendMessage(target, "Unknown setting '" + setting + "'", nick);
				}
				return new CommandChainStateObject();
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
					Helper.sendMessage("chanserv", "kickban " + target + " " + nick + " PhraseBan");
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
