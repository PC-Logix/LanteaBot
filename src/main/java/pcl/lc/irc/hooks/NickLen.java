package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;

import com.google.common.collect.ImmutableSortedSet;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

public class NickLen extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();
	public NickLen() {
		try {
			PreparedStatement checkHook = Database.getPreparedStatement("checkHook");
			checkHook.setString(1, "nicklen");
			ResultSet results = checkHook.executeQuery();
			while (results.next()) {
				enabledChannels.add(results.getString("channel"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNickChange(final NickChangeEvent event) {
		if (!IRCBot.isIgnored(event.getUser().getNick())) {					
			ImmutableSortedSet<Channel> chans = IRCBot.bot.getUserChannelDao().getAllChannels();
			for (Iterator<Channel> iter = chans.iterator(); iter.hasNext(); ) {
				Channel chan = iter.next();
				if (enabledChannels.contains(chan.getName())) {
					if (event.getUser().getNick().length() >= 18) {
						event.getUser().send().notice("Your nick is longer than the current allowed limit of 18 characters.  You have been automatically quieted, and will have to /part the channel to change your name.");			
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onJoin(final JoinEvent event) throws Exception {
		super.onJoin(event);
		if (!IRCBot.isIgnored(event.getUser().getNick())) {
			if (enabledChannels.contains(event.getChannel().getName())) {
				if (event.getUser().getNick().length() >= 18) {
					event.getUser().send().notice("Your nick is longer than the current allowed limit. You have been automatically quieted, and will have to part the channel to change your nickname.");			
				}
			}
		}
	}
	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = Config.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		String[] firstWord = StringUtils.split(trigger);
		String triggerWord2 = firstWord[0];
		if (triggerWord2.equals(prefix + "nicklen")) {
			boolean isOp = Permissions.isOp(event.getBot(), event.getUser());
			if (isOp || Helper.isChannelOp(event)) {
				String command = event.getMessage().substring(event.getMessage().indexOf("nicklen") + 7).trim();
				if (command.equals("disable")) {
					if (enabledChannels.contains(event.getChannel().getName())) {
						try {
							enabledChannels.remove(event.getChannel().getName());
							PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
							disableHook.setString(1, "nicklen");
							disableHook.setString(2, event.getChannel().getName());
							disableHook.executeUpdate();
						} catch (Exception e) {
							e.printStackTrace();
						}
						event.respond("Disabled Name Length notice for this channel");
						return;
					}
				} else if (command.equals("enable")) {
					if (!enabledChannels.contains(event.getChannel().getName())) {
						try {
							enabledChannels.add(event.getChannel().getName());
							PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
							enableHook.setString(1, "nicklen");
							enableHook.setString(2, event.getChannel().getName());
							enableHook.executeUpdate();
						} catch (Exception e) {
							e.printStackTrace();
						}
						event.respond("Enabled Name Length notice for this channel");
						return;
					}
				} else if (command.equals("list")) {
					event.respond("Enabled Name Len notice channels: " + enabledChannels);
					return;
				}
			}
		}
	}
}
