package pcl.lc.irc.hooks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.CommentedProperties;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;


/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Announcements extends AbstractListener {
	private Command local_command_announce;
	private Command local_command_add;
	private Command local_command_list;
	private Command local_command_remove;
	private Command local_command_reload;

	public static Builder config = new Configuration.Builder();
	public static CommentedProperties prop = new CommentedProperties();
	public static HashMap<String, List<Object>> Announcements = new HashMap<>();

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command_announce);
		local_command_announce.registerSubCommand(local_command_add);
		local_command_announce.registerSubCommand(local_command_list);
		local_command_announce.registerSubCommand(local_command_remove);
		local_command_announce.registerSubCommand(local_command_reload);
		Database.addStatement("CREATE TABLE IF NOT EXISTS Announcements(channel, schedule, title, message)");
		Database.addPreparedStatement("addAnnounce", "INSERT INTO Announcements(channel, schedule, message) VALUES (?,?,?);");
		Database.addPreparedStatement("getAnnounce", "SELECT schedule, title, message FROM Announcements WHERE channel = ?;");
		Database.addPreparedStatement("delAnnounce", "DELETE FROM Announcements WHERE title = ? AND channel = ?;");
		setConfig();
	}

	private void initCommands() {
		local_command_announce = new Command("announce", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(params), nick);
			}
		}; local_command_announce.setHelpText("Has sub-commands: add, list, remove");
		local_command_add = new Command("add", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_add.setHelpText("Add announce message");
		local_command_list = new Command("list", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_list.setHelpText("List announce messages");
		local_command_remove = new Command("remove", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_remove.setHelpText("Remove announce message");
		local_command_reload = new Command("reload", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_reload.setHelpText("Reload announce messages");
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
		local_command_announce.tryExecute(command, nick, target, event, copyOfRange);
		local_command_add.tryExecute(command, nick, target, event, copyOfRange);
		local_command_list.tryExecute(command, nick, target, event, copyOfRange);
		local_command_remove.tryExecute(command, nick, target, event, copyOfRange);
		local_command_reload.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}

	private static void setConfig() {
		InputStream input = null;

		try {

			File file = new File("announcements.xml");
			if (!file.exists()) {
				System.out.println("Creating announcements.xml");
				file.createNewFile();
			}

			input = new FileInputStream(file);
			// load a properties file
			prop.load(input);
			Announcements.clear();
			for(String key : prop.stringPropertyNames()) {
				List<Object> eventData = new ArrayList<Object>();
				eventData.add("Channel");
				eventData.add("Event");
				eventData.add("Message");
				Announcements.put(key, eventData);
			}
			IRCBot.log.info(Announcements.toString());
			System.out.println(Announcements.toString());

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}