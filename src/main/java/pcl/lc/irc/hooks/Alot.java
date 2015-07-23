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

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;
import pcl.lc.utils.Helper;
import pcl.lc.utils.getVideoInfo;

/**
 * @author Caitlyn
 *
 */
public class Alot extends ListenerAdapter {
	List<String> enabledChannels;
	public Alot() throws IOException {
		enabledChannels = new ArrayList<String>(Arrays.asList(Config.prop.getProperty("alotenabled-channels", "").split(",")));
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
					String account = Account.getAccount(event.getUser(), event);
					if (IRCBot.admins.containsKey(account) || Helper.isOp(event)) {
						String command = event.getMessage().substring(event.getMessage().indexOf("alot") + 4).trim();
						System.out.println(command);
						if (command.equals("enable")) {
							enabledChannels.add(event.getChannel().getName().toString());
							Config.prop.setProperty("alotenabled-channels", Joiner.on(",").join(enabledChannels));
							event.respond("Enabled Alot for this channel");
							Config.saveProps();
							return;
						} else if (command.equals("disable")) {
							if (!enabledChannels.contains(event.getChannel().getName().toString())) {
								enabledChannels.remove(event.getChannel().getName().toString());
								Config.prop.setProperty("alotenabled-channels", Joiner.on(",").join(enabledChannels));
								event.respond("Disabled Alot for this channel");
								Config.saveProps();
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
