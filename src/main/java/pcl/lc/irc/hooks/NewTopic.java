package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.google.common.collect.Lists;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

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

		command_newTopic = new Command("newtopic", 0) {
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
					msg = msg.replace("[randomitem]", Inventory.getRandomItem().getName());
				}
				if (msg.contains("[drama]")) {
					msg = msg.replace("[drama]", Drama.dramaParse());
				}
				Helper.sendMessage(target, msg, nick);
			}
		}; command_newTopic.setHelpText("Generates a new topic");

		command_addTopic = new Command("addtopic", 0, Permissions.MOD) {
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
		
		command_delTopic = new Command("deltopic", 0, Permissions.MOD) {
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

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String nick, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		command_newTopic.tryExecute(command, nick, target, event, copyOfRange);
		command_addTopic.tryExecute(command, nick, target, event, copyOfRange);
		command_delTopic.tryExecute(command, nick, target, event, copyOfRange);
	}

}
