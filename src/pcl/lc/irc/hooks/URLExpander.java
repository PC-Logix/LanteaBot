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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.utils.HTTPQuery;
import pcl.lc.utils.TitleExtractor;
import pcl.lc.utils.getVideoInfo;

/**
 * @author caitlyn
 *
 */
public class URLExpander extends ListenerAdapter {

	
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
		String ourinput = event.getMessage();
		String s = ourinput.trim();
		if (!event.getChannel().getName().equals("#oc")) {
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
								Matcher matcher1 = compiledPattern.matcher(url);

								if (matcher1.find()) {
									url = matcher1.group();
								}
								String vinfo = getVideoInfo.getVideoSearch(url, true, false);
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