/**
 * 
 */
package pcl.lc.irc.hooks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.google.common.base.Joiner;

import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Account;
import pcl.lc.utils.HTTPQuery;
import pcl.lc.utils.TitleExtractor;
import pcl.lc.utils.getVideoInfo;

/**
 * @author caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class URLExpander extends ListenerAdapter {
	List<String> disabledChannels;
	public URLExpander() throws IOException {
		disabledChannels = new ArrayList<String>(Arrays.asList(IRCBot.prop.getProperty("urldisabled-channels", "").split(",")));
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
			String prefix = IRCBot.commandprefix;

			if (s.length() > 1) {

				String[] firstWord = StringUtils.split(trigger2);
				String triggerWord2 = firstWord[0];
				if (triggerWord2.equals(prefix + "url")) {
					String account = Account.getAccount(event.getUser(), event);
					if (IRCBot.admins.containsKey(account) || event.getChannel().isOp(event.getUser())) {
						String command = event.getMessage().substring(event.getMessage().indexOf("url") + 3).trim();
						System.out.println(command);
						if (command.equals("disable")) {
							disabledChannels.add(event.getChannel().getName().toString());
							IRCBot.prop.setProperty("urldisabled-channels", Joiner.on(",").join(disabledChannels));
							event.respond("Disabled URLInfo for this channel");
							IRCBot.saveProps();
							return;
						} else if (command.equals("enable")) {
							disabledChannels.remove(event.getChannel().getName().toString());
							IRCBot.prop.setProperty("urldisabled-channels", Joiner.on(",").join(disabledChannels));
							event.respond("Enabled URLInfo for this channel");
							IRCBot.saveProps();
							return;
						} else if (command.equals("list")) {
							event.respond("Disabled URL channels: " + disabledChannels);
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
								System.out.println(jItem);
								if (jItem.indexOf("youtube") != -1 || jItem.indexOf("youtu.be") != -1) {
									String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
									Pattern compiledPattern = Pattern.compile(pattern);
									Matcher matcher1 = compiledPattern.matcher(jItem);
									if (matcher1.find()) {
										url = matcher1.group();
									}
									String vinfo = getVideoInfo.getVideoSearch(url, true, false);
									System.out.println(url);
									event.respond(vinfo);
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