/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;
import pcl.lc.utils.HTTPQuery;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TitleExtractor;
import pcl.lc.utils.getVideoInfo;

/**
 * @author caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class URLExpander extends ListenerAdapter {
	List<String> enabledChannels = new ArrayList<String>();
	public URLExpander() throws IOException {
        try {
            PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
            checkHook.setString(1, "URLInfo");
            ResultSet results = checkHook.executeQuery();
            while (results.next()) {
            	enabledChannels.add(results.getString("channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public static boolean find(File f, String searchString) {
		boolean result = false;
		Scanner in = null;
		try {
			in = new Scanner(new FileReader(f));
			while(in.hasNextLine() && !result) {
				if (in.nextLine().contains(searchString))
					return true;
			}
		}
		catch(IOException e) {
			e.printStackTrace();      
		}
		finally {
			try { in.close() ; } catch(Exception e) { /* ignore */ }  
		}
		return false;
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
				if (triggerWord2.equals(prefix + "url")) {
					boolean isOp = Account.isOp(event.getBot(), event.getUser());
					if (isOp  || Helper.isChannelOp(event)) {
						String command = event.getMessage().substring(event.getMessage().indexOf("url") + 3).trim();
						if (command.equals("enable")) {
							if (!enabledChannels.contains(event.getChannel().getName().toString())) {
								enabledChannels.add(event.getChannel().getName().toString());
								try {
									PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
									enableHook.setString(1, "URLInfo");
									enableHook.setString(2, event.getChannel().getName());
									enableHook.executeUpdate();
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;								
							}

						} else if (command.equals("disable")) {
							enabledChannels.remove(event.getChannel().getName().toString());
							try {
								PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
								disableHook.setString(1, "URLInfo");
								disableHook.setString(2, event.getChannel().getName());
								disableHook.executeUpdate();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return;
						} else if (command.equals("list")) {
							event.respond("Enabled URL channels: " + enabledChannels);
							return;
						}
					}
				}
			}

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
					if (url != null) {
						HTTPQuery q = null;
						HTTPQuery supportedServices = null;
						try {
							supportedServices = HTTPQuery.create("http://api.longurl.org/v2/services?format=json");
							supportedServices.connect(true,false);
							String services = supportedServices.readWhole();
							supportedServices.close();
							URL myUrl = new URL(url);
							if (services.contains(myUrl.getHost())) {
								q = HTTPQuery.create("http://api.longurl.org/v2/expand?format=json&url="+URLEncoder.encode(url,"UTF8"));
								url = url.replace("http://", "").replace("https://", "");
								q.connect(true,false);
								String json = q.readWhole().replace("[", "").replace("]", "");
								q.close();
								String jItem = new JSONObject(json).getString("long-url");
								if (jItem.indexOf("youtube") != -1 || jItem.indexOf("youtu.be") != -1) {
									if (Config.botConfig.containsKey("GoogleAPI")) {
										String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
										Pattern compiledPattern = Pattern.compile(pattern);
										Matcher matcher1 = compiledPattern.matcher(jItem);
										if (matcher1.find()) {
											url = matcher1.group();
										}
										String apiKey = Config.botConfig.get("GoogleAPI").toString();
										String vinfo = getVideoInfo.getVideoSearch(url, true, false, apiKey, event.getUser().getHostmask());
										event.respond(vinfo);
									}
								} else {
									String title = TitleExtractor.getPageTitle(jItem);
									event.respond(jItem + " Page title: " + title);
								}
							}
						} catch (Exception e) { }
					}
				}			
			}
		}
	}
}