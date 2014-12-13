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
 * @author caitlyn
 * 
 */
@SuppressWarnings("rawtypes")
public class YTInfo extends ListenerAdapter {
	List<String> disabledChannels;
	public YTInfo() throws IOException {
		disabledChannels = new ArrayList<String>(Arrays.asList(IRCBot.prop.getProperty("ytdisabled-channels", "").split(",")));
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
				if (triggerWord2.equals(prefix + "yt")) {
					String account = Account.getAccount(event.getUser(), event);
					if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
						String command = event.getMessage().substring(event.getMessage().indexOf("yt") + 2).trim();
						System.out.println(command);
						if (command.equals("disable")) {
							disabledChannels.add(event.getChannel().getName().toString());
							IRCBot.prop.setProperty("ytdisabled-channels", Joiner.on(",").join(disabledChannels));
							event.respond("Disabled YTInfo for this channel");
							IRCBot.saveProps();
							return;
						} else if (command.equals("enable")) {
							disabledChannels.remove(event.getChannel().getName().toString());
							IRCBot.prop.setProperty("ytdisabled-channels", Joiner.on(",").join(disabledChannels));
							event.respond("Enabled YTInfo for this channel");
							IRCBot.saveProps();
							return;
						} else if (command.equals("list")) {
							event.respond("Disabled YT channels: " + disabledChannels);
							return;
						}
					}
				}
			}

			if (!disabledChannels.contains(event.getChannel().getName().toString())) {
				if (s.length() > 1) {

					int matchStart = 1;
					int matchEnd = 1;
					final Pattern urlPattern = Pattern.compile(
							"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
									+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
									+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
									Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
					Matcher matcher = urlPattern.matcher(s);
					while (matcher.find()) {
						matchStart = matcher.start(1);
						matchEnd = matcher.end();
					}
					String url = s.substring(matchStart, matchEnd);
					if (url.indexOf("youtube.com") != -1 || url.indexOf("youtu.be") != -1) {
						String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
						Pattern compiledPattern = Pattern.compile(pattern);
						Matcher matcher1 = compiledPattern.matcher(url);
						if (matcher1.find()) {
							url = matcher1.group();
						}
						String vinfo = getVideoInfo.getVideoSearch(url, true, false);
						if (vinfo != null) {
							event.respond(vinfo);
						}
					}
				}
			}			
		}

	}
}
