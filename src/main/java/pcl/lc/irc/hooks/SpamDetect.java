package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TimedHashMap;

public class SpamDetect extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();

	public SpamDetect() {
		try {
			PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
			checkHook.setString(1, "antispam");
			ResultSet results = checkHook.executeQuery();
			while (results.next()) {
				enabledChannels.add(results.getString("channel"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	TimedHashMap<String, String> newUsers = new TimedHashMap<String, String>(5000, null);

	public void onJoin(final JoinEvent join) throws Exception {
		newUsers.put(join.getUser().getNick(), join.getChannel().getName());
	}

	public static boolean isAllUpperCase(String s) {
		if (!StringUtils.isAlphanumeric(s)) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if (Character.isLowerCase(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = Config.commandprefix;
		String trigger = event.getMessage().toLowerCase().trim();
		if (!event.getChannel().getName().isEmpty()) {
			if (event.getMessage().length() > 1) {
				if (enabledChannels.contains(event.getChannel().getName())) {
					if (isAllUpperCase(event.getMessage()) && newUsers.containsKey(event.getUser().getNick())) {
						event.getBot().sendIRC().message("chanserv", "kick " + event.getChannel().getName() + " " + event.getUser().getNick() + " Possible Spam detected!");
					}
				}
				String[] firstWord = StringUtils.split(trigger);
				String triggerWord2 = firstWord[0];
				if (triggerWord2.equals(prefix + "antispam")) {
					boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
					if (isOp || Helper.isChannelOp(event)) {
						String command = event.getMessage().substring(event.getMessage().indexOf("antispam") + 8).trim();
						if (command.equals("disable")) {
							if (enabledChannels.contains(event.getChannel().getName())) {
								try {
									enabledChannels.remove(event.getChannel().getName());
									PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
									disableHook.setString(1, "antispam");
									disableHook.setString(2, event.getChannel().getName());
									disableHook.executeUpdate();
								} catch (Exception e) {
									e.printStackTrace();
								}
								event.respond("Disabled antispam for this channel");
								return;
							}
						} else if (command.equals("enable")) {
							if (!enabledChannels.contains(event.getChannel().getName())) {
								try {
									enabledChannels.add(event.getChannel().getName());
									PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
									enableHook.setString(1, "antispam");
									enableHook.setString(2, event.getChannel().getName());
									enableHook.executeUpdate();
								} catch (Exception e) {
									e.printStackTrace();
								}
								event.respond("Enabled antispam for this channel");
								return;
							}
						} else if (command.equals("list")) {
							event.respond("Enabled antispam channels: " + enabledChannels);
							return;
						}
					}
				}
			}
		}
	}
}
