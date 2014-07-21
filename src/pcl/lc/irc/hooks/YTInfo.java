/**
 * 
 */
package pcl.lc.irc.hooks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.utils.getVideoInfo;

/**
 * @author caitlyn
 * 
 */
public class YTInfo extends ListenerAdapter {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String ourinput = event.getMessage().toLowerCase();
		String s = ourinput.trim();
		if (!event.getChannel().getName().equals("#oc")) {
			if (s.length() > 1) {
				int matchStart = 1;
				int matchEnd = 1;
				final Pattern urlPattern = Pattern
						.compile(
								"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
										+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
										+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
								Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
										| Pattern.DOTALL);
				Matcher matcher = urlPattern.matcher(s);
				while (matcher.find()) {
					matchStart = matcher.start(1);
					matchEnd = matcher.end();
					// now you have the offsets of a URL match
				}
				String url = s.substring(matchStart, matchEnd);
				if (url.indexOf("youtube.com") != -1
						|| url.indexOf("youtu.be") != -1) {
					String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

					Pattern compiledPattern = Pattern.compile(pattern);
					Matcher matcher1 = compiledPattern.matcher(url);

					if (matcher1.find()) {
						url = matcher1.group();
					}
					String vinfo = getVideoInfo
							.getVideoSearch(url, true, false);
					event.respond(vinfo);
				}
			}
		}
	}
}
