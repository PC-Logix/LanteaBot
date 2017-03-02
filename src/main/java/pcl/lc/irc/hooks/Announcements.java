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

import pcl.lc.irc.*;
import pcl.lc.utils.CommentedProperties;


/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Announcements extends ListenerAdapter implements Runnable {
	private Command local_command_add;
	private Command local_command_list;
	private Command local_command_remove;
	private Command local_command_reload;

	public static Builder config = new Configuration.Builder();
	public static CommentedProperties prop = new CommentedProperties();
	public static HashMap<String, List<Object>> Announcements = new HashMap<String, List<Object>>();
	private Boolean chanOp = false;
	private int permLevel = 0;
	private int requiredPermLevel = 5;
	XStream xstream = new XStream(new DomDriver());
	
	public static void saveProps() {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream("announcements.xml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			prop.store(output, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setConfig() {
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
	
	public Announcements() {
		local_command_add = new Command("addannounce", 0);
		IRCBot.registerCommand(local_command_add, "Add announce message");
		local_command_list = new Command("listannounce", 0);
		IRCBot.registerCommand(local_command_list, "List announce messages");
		local_command_remove = new Command("removeannounce", 0);
		IRCBot.registerCommand(local_command_remove, "Remove announce message");
		local_command_reload = new Command("reloadannounce", 0);
		IRCBot.registerCommand(local_command_reload, "Reload announce messages");
		Database.addStatement("CREATE TABLE IF NOT EXISTS Announcements(channel, schedule, title, message)");
		Database.addPreparedStatement("addAnnounce", "INSERT INTO Announcements(channel, schedule, message) VALUES (?,?,?);");
		Database.addPreparedStatement("getAnnounce", "SELECT schedule, title, message FROM Announcements WHERE channel = ?;");
		Database.addPreparedStatement("delAnnounce", "DELETE FROM Announcements WHERE title = ? AND channel = ?;");
		setConfig();
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		if (Permissions.hasPermission(event.getBot(), event, requiredPermLevel)) {
			String ourInput = event.getMessage().toLowerCase();
			String trigger = ourInput.trim();
			if (trigger.length() > 1) {
				String[] firstWord = StringUtils.split(trigger);
				String command = firstWord[0];
				if (local_command_add.shouldExecute(command) == 0) {
					event.respond("Merp");
				} else if (local_command_list.shouldExecute(command) == 0) {
					
				} else if (local_command_remove.shouldExecute(command) == 0) {
					
				} else if (local_command_reload.shouldExecute(command) == 0) {
					setConfig();
				}
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
