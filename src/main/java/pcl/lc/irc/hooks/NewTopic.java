package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

public class NewTopic extends AbstractListener {
	private Command command_newTopic;
	private Command command_delTopic;
	private Command command_addTopic;

	@Override
	protected void initHook() {

		Database.addStatement("CREATE TABLE IF NOT EXISTS Topics(id INTEGER PRIMARY KEY, topic)");
		Database.addPreparedStatement("addTopic", "INSERT INTO Topics(topic) VALUES (?);");
		Database.addPreparedStatement("getRandomTopic", "SELECT id, topic FROM Topics ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("delTopic", "DELETE FROM Topics WHERE id = ?;");

		command_newTopic = new Command("newtopic") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				String msg = "";
				PreparedStatement statement;
				statement = Database.getPreparedStatement("getRandomTopic");
				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next())
					msg = "#" + resultSet.getInt(1) + " " + resultSet.getString(2);
				if (msg.contains("[randomitem]")) {
					msg = msg.replace("[randomitem]", Inventory.getRandomItem().getName(true));
				}
				if (msg.contains("[drama]")) {
					msg = msg.replace("[drama]", Drama.dramaParse());
				}
				if (msg.contains("[randomuser]")) {
					Random rnd = new Random();
					int i = rnd.nextInt(Helper.getNamesFromTarget(target).size());
					String user = Helper.getNamesFromTarget(target).toArray()[i].toString();
					msg = msg.replace("[randomuser]", user);
				}
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, msg, nick);
			}
		};
		command_newTopic.setHelpText("Generates a new topic");

		command_addTopic = new Command("addtopic", new CommandArgumentParser(1, new CommandArgument("Topic", "String")), Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				PreparedStatement addCommand = Database.getPreparedStatement("addTopic");
				addCommand.setString(1, this.argumentParser.getArgument("Topic"));
				addCommand.executeUpdate();
				Helper.sendMessage(target, "Ok", nick);
			}
		};

		command_delTopic = new Command("deltopic", new CommandArgumentParser(1, new CommandArgument("TopicID", "Integer")), Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) throws Exception {
				PreparedStatement delCommand = Database.getPreparedStatement("delTopic");
				delCommand.setInt(1, this.argumentParser.getInt("TopicID"));
				delCommand.executeUpdate();
				Helper.sendMessage(target, "Ok", nick);
			}
		};

		IRCBot.registerCommand(command_newTopic);
		IRCBot.registerCommand(command_delTopic);
		IRCBot.registerCommand(command_addTopic);
	}
}
