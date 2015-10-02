package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class xkcd extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();
	public xkcd() {
        try {
            PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
            checkHook.setString(1, "XKCD");
            ResultSet results = checkHook.executeQuery();
            while (results.next()) {
            	enabledChannels.add(results.getString("channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	private static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read); 

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		if (!IRCBot.isIgnored(event.getUser().getNick())) {
			String prefix = Config.commandprefix;
			String ourinput = event.getMessage().toLowerCase();
			String trigger = ourinput.trim();
			if (trigger.length() > 1) {
				String[] firstWord = StringUtils.split(trigger);
				String triggerWord = firstWord[0];
				String param = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();		
				if (triggerWord.equals(prefix + "xkcd")) {
					boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
					if (isOp || Helper.isOp(event)) {
						if (param.equals("enable") && !enabledChannels.contains(event.getChannel().getName())) {
					        try {
					        	enabledChannels.add(event.getChannel().getName());
					            PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
					            enableHook.setString(1, "XKCD");
					            enableHook.setString(2, event.getChannel().getName());
					            enableHook.executeUpdate();
					            event.respond("Enabled XKCD");
					        } catch (Exception e) {
					            e.printStackTrace();
					        }
							return;
						} else if (param.equals("disable") && enabledChannels.contains(event.getChannel().getName())) {
					        try {
					        	enabledChannels.remove(event.getChannel().getName());
					            PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
					            disableHook.setString(1, "XKCD");
					            disableHook.setString(2, event.getChannel().getName());
					            disableHook.executeUpdate();
					            event.respond("Disabled XKCD");
					        } catch (Exception e) {
					            e.printStackTrace();
					        }
							return;
						}
					}
					if(param.length() > 0) {
						if (isNumeric(param)) {
							String json = readUrl("http://xkcd.com/" + param + "/info.0.json");
							final JSONObject obj = new JSONObject(json);
							String name = obj.get("safe_title").toString();
							event.respond("XKCD Comic Name:" + name + " URL: https://xkcd.com/" + param);
						} else {
							event.respond("Invalid ID");
						}
					} else if (triggerWord.equals(prefix + "xkcd")) {
						URLConnection con = new URL( "http://dynamic.xkcd.com/random/comic/" ).openConnection();
						con.connect();
						InputStream is = con.getInputStream();
						String newurl = con.getURL().toString();
						is.close();
						event.respond("Random XKCD Comic: " + newurl);
					}
				} else if (event.getMessage().contains("xkcd.com") && enabledChannels.contains(event.getChannel().getName())) {
					URL aURL = new URL(triggerWord);
					String json = readUrl("http://xkcd.com/" + aURL.getPath().replace("/", "") + "/info.0.json");
					final JSONObject obj = new JSONObject(json);
					String name = obj.get("safe_title").toString();
					event.getChannel().send().message("XKCD Comic Name: " + name + " Posted on: " + obj.get("month").toString() + "/" + obj.get("day").toString() + "/" + obj.get("year").toString());
				}
			}			
		}
	}
}