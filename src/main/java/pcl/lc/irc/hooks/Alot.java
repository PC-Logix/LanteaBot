/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
public class Alot extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();
	public Alot() throws IOException {
        try {
            PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
            checkHook.setString(1, "alot");
            ResultSet results = checkHook.executeQuery();
            while (results.next()) {
            	enabledChannels.add(results.getString("channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		if (!IRCBot.isIgnored(event.getUser().getNick())) {
			String ourinput = event.getMessage();
			String s = ourinput.trim();
			String trigger2 = event.getMessage().toLowerCase().trim();
			String prefix = Config.commandprefix;

			if (s.length() > 1) {

				String[] firstWord = StringUtils.split(trigger2);
				String triggerWord2 = firstWord[0];
				if (triggerWord2.equals(prefix + "alot")) {
					boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
					if (isOp || Helper.isChannelOp(event)) {
						String command = event.getMessage().substring(event.getMessage().indexOf("alot") + 4).trim();
						if (command.equals("enable")) {
							enabledChannels.add(event.getChannel().getName().toString());
							try {
								PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
								enableHook.setString(1, "alot");
								enableHook.setString(2, event.getChannel().getName());
								enableHook.executeUpdate();
								event.respond("Enabled Alot for this channel");
							} catch (Exception e) {
								e.printStackTrace();
							}
							return;
						} else if (command.equals("disable")) {
							if (!enabledChannels.contains(event.getChannel().getName().toString())) {
								enabledChannels.remove(event.getChannel().getName().toString());
								try {
									PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
									disableHook.setString(1, "alot");
									disableHook.setString(2, event.getChannel().getName());
									disableHook.executeUpdate();
									event.respond("Disabled Alot for this channel");
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;								
							}

						} else if (command.equals("list")) {
							event.respond("Ensabled Alot channels: " + enabledChannels);
							return;
						}
					}
				}
			}

			if (enabledChannels.contains(event.getChannel().getName().toString())) {
				if (s.length() > 1) {
					if (s.toLowerCase().contains("alot")){
						IRCBot.bot.sendIRC().message(event.getChannel().getName().toString(), "ALOT: http://tinyurl.com/y42zurt");
					}
				}
			}			
		}

	}

}
