package pcl.lc.irc.hooks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class xkcd extends AbstractListener {
	public List<String> enabledChannels = new ArrayList<String>();
	private String chan;
	private Boolean chanOp = false;

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

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("xkcd", "XKCD");
	}

	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
		chanOp = Helper.isChannelOp(event);
	}

	@Override
	public void onMessage(final MessageEvent event) {
		if (event.getMessage().contains("xkcd.com") && enabledChannels.contains(event.getChannel().getName())) {
			Pattern pattern = Pattern.compile(
					"https?://.*(?:xkcd.com/)([0-9]+)", 
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(event.getMessage());
			if (matcher.find()){
				String json = null;
				try {
					json = readUrl("http://xkcd.com/" + matcher.group(1) + "/info.0.json");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONObject obj = null;
				try {
					obj = new JSONObject(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String name = null;
				try {
					name = obj.get("safe_title").toString();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					event.getChannel().send().message("XKCD Comic Name: " + name + " Posted on: " + obj.get("month").toString() + "/" + obj.get("day").toString() + "/" + obj.get("year").toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				event.respond("meh");
			}
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) throws Exception {
		String target;
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		System.out.println(enabledChannels.contains(target));
		if (command.equalsIgnoreCase(Config.commandprefix + "xkcd")) {
			boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
			if (isOp || chanOp) {
				if (copyOfRange[0].equals("enable") && !enabledChannels.contains(chan)) {
					try {
						enabledChannels.add(chan);
						PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
						enableHook.setString(1, "XKCD");
						enableHook.setString(2, chan);
						enableHook.executeUpdate();
						IRCBot.getInstance().sendMessage(target, "Enabled XKCD");
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				} else if (copyOfRange[0].equals("disable") && enabledChannels.contains(chan)) {
					try {
						enabledChannels.remove(chan);
						PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
						disableHook.setString(1, "XKCD");
						disableHook.setString(2, chan);
						disableHook.executeUpdate();
						IRCBot.getInstance().sendMessage(target, "Disabled XKCD");
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
			}
			if(copyOfRange.length > 0) {
				if (isNumeric(copyOfRange[0])) {
					String json = readUrl("http://xkcd.com/" + copyOfRange[0] + "/info.0.json");
					final JSONObject obj = new JSONObject(json);
					String name = obj.get("safe_title").toString();
					IRCBot.getInstance().sendMessage(target, "XKCD Comic Name: " + name + " URL: https://xkcd.com/" + copyOfRange[0]);
				} else {
					IRCBot.getInstance().sendMessage(target, "Invalid ID");
				}
			} else if (command.equals(Config.commandprefix + "xkcd")) {
				URLConnection con = new URL( "http://dynamic.xkcd.com/random/comic/" ).openConnection();
				con.connect();
				InputStream is = con.getInputStream();
				String newurl = con.getURL().toString();
				is.close();
				IRCBot.getInstance().sendMessage(target, "Random XKCD Comic: " + newurl);
			}
		}
	}
}
