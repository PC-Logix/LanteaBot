/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.google.common.base.Joiner;

import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;
import pcl.lc.utils.getVideoInfo;

/**
 * @author Caitlyn
 *
 */
public class Alot extends ListenerAdapter {
	List<String> disabledChannels;
	public Alot() throws IOException {
		disabledChannels = new ArrayList<String>(Arrays.asList(IRCBot.prop.getProperty("alotdisabled-channels", "").split(",")));
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		if (!IRCBot.isIgnored(event.getUser().getNick())) {
			String ourinput = event.getMessage();
			String s = ourinput.trim();
			String trigger2 = event.getMessage().toLowerCase().trim();
			String prefix = IRCBot.commandprefix;

			if (s.length() > 1) {

				String[] firstWord = StringUtils.split(trigger2);
				String triggerWord2 = firstWord[0];
				if (triggerWord2.equals(prefix + "alot")) {
					String account = Account.getAccount(event.getUser(), event);
					if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
						String command = event.getMessage().substring(event.getMessage().indexOf("alot") + 4).trim();
						System.out.println(command);
						if (command.equals("disable")) {
							disabledChannels.add(event.getChannel().getName().toString());
							IRCBot.prop.setProperty("alotdisabled-channels", Joiner.on(",").join(disabledChannels));
							event.respond("Disabled Alot for this channel");
							IRCBot.saveProps();
							return;
						} else if (command.equals("enable")) {
							disabledChannels.remove(event.getChannel().getName().toString());
							IRCBot.prop.setProperty("alotdisabled-channels", Joiner.on(",").join(disabledChannels));
							event.respond("Enabled Alot for this channel");
							IRCBot.saveProps();
							return;
						} else if (command.equals("list")) {
							event.respond("Disabled Alot channels: " + disabledChannels);
							return;
						}
					}
				}
			}

			if (!disabledChannels.contains(event.getChannel().getName().toString())) {
				if (s.length() > 1) {
					if (s.toLowerCase().contains("alot")){
						IRCBot.bot.sendIRC().message(event.getChannel().getName().toString(), "ALOT: http://4.bp.blogspot.com/_D_Z-D2tzi14/S8TRIo4br3I/AAAAAAAACv4/Zh7_GcMlRKo/s400/ALOT.png");
					}
				}
			}			
		}

	}
	
}
