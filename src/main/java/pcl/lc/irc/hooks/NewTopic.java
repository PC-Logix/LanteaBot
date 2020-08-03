package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
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
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String msg = "";
				try {
					PreparedStatement statement;
					statement = Database.getPreparedStatement("getRandomTopic");
					ResultSet resultSet = statement.executeQuery();
					if (resultSet.next())
						msg = "#" + resultSet.getInt(1) + " " + resultSet.getString(2);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		}; command_newTopic.setHelpText("Generates a new topic");

		command_addTopic = new Command("addtopic", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement addCommand = Database.getPreparedStatement("addTopic");
					addCommand.setString(1, params);
					addCommand.executeUpdate();
					Helper.sendMessage(target, "Ok", nick);
				}
				catch (Exception e) {
					e.printStackTrace();
					event.respond("An error occurred while processing this command");
				}
			}
		};

		command_delTopic = new Command("deltopic", Permissions.TRUSTED) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					PreparedStatement delCommand = Database.getPreparedStatement("delTopic");
					delCommand.setInt(1, Integer.valueOf(params));
					delCommand.executeUpdate();
					Helper.sendMessage(target, "Ok", nick);
				}
				catch (Exception e) {
					e.printStackTrace();
					event.respond("An error occurred while processing this command");
				}
			}
		};

		IRCBot.registerCommand(command_newTopic);
		IRCBot.registerCommand(command_delTopic);
		IRCBot.registerCommand(command_addTopic);
	}
}
