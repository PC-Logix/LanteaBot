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

import org.json.JSONObject;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.utils.HTTPQuery;
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
		if (s.length() > 1) {
			if (event.getChannel().getName() != "#oc") {
				if (s.startsWith("http://") || s.startsWith("www.//")) {
					HTTPQuery q = null;
					HTTPQuery supportedServices = null;
					try {
						supportedServices = HTTPQuery.create("http://api.longurl.org/v2/services?format=json");
						supportedServices.connect(true,false);
						String services = supportedServices.readWhole();
						supportedServices.close();
						URL myUrl = new URL(s);
						if (services.contains(myUrl.getHost())) {
							q = HTTPQuery.create("http://api.longurl.org/v2/expand?format=json&url="+URLEncoder.encode(s,"UTF8"));
							s = s.replace("http://", "").replace("https://", "");
							q.connect(true,false);
							String json = q.readWhole().replace("[", "").replace("]", "");
							q.close();
							String jItem = new JSONObject(json).getString("long-url");
							if (jItem.indexOf("youtube") != -1 || jItem.indexOf("youtu.be") != -1) {
								String vinfo = getVideoInfo.getVideoSearch(jItem, true, false);
								event.respond(vinfo);
							} else {
								event.respond(jItem);
							}
						}
					} catch (Exception e) { }
				}
			}
		}
	}
}