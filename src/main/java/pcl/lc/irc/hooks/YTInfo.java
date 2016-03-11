/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import pcl.lc.utils.getVideoInfo;

/**
 * @author caitlyn
 * 
 */
@SuppressWarnings("rawtypes")
public class YTInfo extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();
	public YTInfo() throws IOException {
        try {
            PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
            checkHook.setString(1, "YouTube");
            ResultSet results = checkHook.executeQuery();
            while (results.next()) {
            	enabledChannels.add(results.getString("channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public static String extractYTId(String ytUrl) {
	    String vId = null;
	    Pattern pattern = Pattern.compile(
	                     "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$", 
	                     Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(ytUrl);
	    if (matcher.matches()){
	        vId = matcher.group(1);
	    }
	    return vId;
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
				if (triggerWord2.equals(prefix + "ytc")) {
					boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
					if (isOp || Helper.isChannelOp(event)) {
						String command = event.getMessage().substring(event.getMessage().indexOf("ytc") + 3).trim();
						if (command.equals("enable")) {
							if (!enabledChannels.contains(event.getChannel().getName())) {
						        try {
						        	enabledChannels.add(event.getChannel().getName());
						            PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
						            enableHook.setString(1, "YouTube");
						            enableHook.setString(2, event.getChannel().getName());
						            enableHook.executeUpdate();
						        } catch (Exception e) {
						            e.printStackTrace();
						        }
								event.respond("Enabled YTInfo for this channel");
								return;		
							}

						} else if (command.equals("disable")) {
							if (enabledChannels.contains(event.getChannel().getName())) {
						        try {
						        	enabledChannels.remove(event.getChannel().getName());
						            PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
						            disableHook.setString(1, "YouTube");
						            disableHook.setString(2, event.getChannel().getName());
						            disableHook.executeUpdate();
						        } catch (Exception e) {
						            e.printStackTrace();
						        }
								event.respond("Disable YTInfo for this channel");
								return;
							}
						} else if (command.equals("list")) {
							event.respond("Enabled YT channels: " + enabledChannels);
							return;
						}
					}
				} else {
					if (enabledChannels.contains(event.getChannel().getName().toString())) {
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
							    String vId = null;
							    String reg = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})";
							    Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
							    Matcher matcher1 = pattern.matcher(url);
							    if (matcher1.find()) {
							        vId = matcher1.group(1);
							    } 
								String apiKey = Config.botConfig.get("GoogleAPI").toString();
								String vinfo = getVideoInfo.getVideoSearch(vId, true, false, apiKey);
								if (vinfo != null) {
									event.getChannel().send().message(vinfo);
								}
							}
						}
					}	
				}
			}		
		}
	}
}
