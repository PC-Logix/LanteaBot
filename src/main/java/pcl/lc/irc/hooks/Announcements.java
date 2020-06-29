package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.hooks.events.MessageEvent;

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
		local_command_announce.registerSubCommand(local_command_add);
		local_command_announce.registerSubCommand(local_command_list);
		local_command_announce.registerSubCommand(local_command_remove);
		local_command_announce.registerSubCommand(local_command_reload);
		IRCBot.registerCommand(local_command_announce);
		Database.addStatement("CREATE TABLE IF NOT EXISTS Announcements(channel, schedule, lastran, title, message)");
		Database.addPreparedStatement("addAnnounce", "INSERT INTO Announcements(channel, schedule, message) VALUES (?,?,?);");
		Database.addPreparedStatement("getAnnounce", "SELECT schedule, title, message FROM Announcements WHERE channel = ?;");
		Database.addPreparedStatement("getAllAnnounce", "SELECT schedule, title, message, channel FROM Announcements;");
		Database.addPreparedStatement("delAnnounce", "DELETE FROM Announcements WHERE title = ? AND channel = ?;");
		Database.addUpdateQuery(8, "ALTER TABLE Announcements ADD lastran INTEGER DEFAULT 0 NULL");
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (IRCBot.bot != null) {
						long epoch = System.currentTimeMillis();
						PreparedStatement getAllAnnounce = Database.getPreparedStatement("getAllAnnounce");
						//getTimedBans.setString(1, epoch);
						ResultSet results = getAllAnnounce.executeQuery();
						if (results.next()) {
							IRCBot.getInstance();
							for (Channel chan : IRCBot.bot.getUserBot().getChannels()) {
								if (chan.getName().equals(results.getString(1))) {
									PreparedStatement getAnnounce = Database.getPreparedStatement("getAnnounce");
									getAnnounce.setString(1, chan.getName());
									ResultSet res = getAnnounce.executeQuery();
								}
							}
						}
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	private void initCommands() {
		local_command_announce = new Command("announce", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				Helper.sendMessage(target, this.trySubCommandsMessage(params), nick);
			}
		}; local_command_announce.setHelpText("Has sub-commands: add, list, remove");
		local_command_add = new Command("add", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_add.setHelpText("Add announce message");
		local_command_list = new Command("list", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {

				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_list.setHelpText("List announce messages");
		local_command_remove = new Command("remove", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_remove.setHelpText("Remove announce message");
		local_command_reload = new Command("reload", Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "This command doesn't do anything.", nick);
			}
		}; local_command_reload.setHelpText("Reload announce messages");
	}
}