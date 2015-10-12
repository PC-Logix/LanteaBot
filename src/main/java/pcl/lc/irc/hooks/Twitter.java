/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author smbarbour
 *
 */
@SuppressWarnings("rawtypes")
public class Twitter extends ListenerAdapter {
	private final twitter4j.Twitter twitter;

	public Twitter() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(Config.getTwitCKey())
				.setOAuthConsumerSecret(Config.getTwitCSecret())
				.setOAuthAccessToken(Config.getTwitToken())
				.setOAuthAccessTokenSecret(Config.getTwitTSecret());
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
		try {
			System.out.println("Twitter User ID: " + twitter.getId());
			System.out.println("Twitter Screen Name: " + twitter.getScreenName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		PircBotX bot = event.getBot();
		String[] splitMessage = event.getMessage().split(" ");
		for (String aSplitMessage : splitMessage) {
			if (aSplitMessage.contains("twitter.com") && aSplitMessage.contains("/status/")) {
				int index = aSplitMessage.lastIndexOf("/") + 1;
				long status = Long.parseLong(aSplitMessage.substring(index));
				try {
					Status lookup = twitter.showStatus(status);
					bot.sendIRC().message(event.getChannel().getName(), lookup.getCreatedAt() + " @" + lookup.getUser().getScreenName() + ": " + lookup.getText());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}