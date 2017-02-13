/**
 * 
 */
package pcl.lc.irc.hooks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author smbarbour
 *
 */
@SuppressWarnings("rawtypes")
public class Twitter extends ListenerAdapter {
	private final twitter4j.Twitter twitter;
	List<String> enabledChannels = new ArrayList<String>();
	public Twitter() {
		if(Config.getTwitCKey() != null || Config.getTwitCSecret() != null || Config.getTwitToken() != null || Config.getTwitTSecret() != null) {
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

			try {
				PreparedStatement checkHook = IRCBot.getInstance().getPreparedStatement("checkHook");
				checkHook.setString(1, "Twitter");
				ResultSet results = checkHook.executeQuery();
				while (results.next()) {
					enabledChannels.add(results.getString("channel"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Please make sure the twitter API settings are set in your config.  Disabling Twitter Hook");
			twitter = null;
		}
	}


	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		PircBotX bot = event.getBot();
		if(Config.getTwitCKey() != null || Config.getTwitCSecret() != null || Config.getTwitToken() != null || Config.getTwitTSecret() != null) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				String prefix = Config.commandprefix;
				String ourinput = event.getMessage().toLowerCase();
				String trigger = ourinput.trim();
				if (trigger.length() > 1) {
					String[] firstWord = StringUtils.split(trigger);
					String triggerWord = firstWord[0];
					String param = event.getMessage().substring(event.getMessage().indexOf(triggerWord) + triggerWord.length()).trim();		
					if (triggerWord.equals(prefix + "twitter")) {
						boolean isOp = IRCBot.getInstance().isOp(event.getBot(), event.getUser());
						if (isOp || Helper.isChannelOp(event)) {
							if (param.equals("enable") && !enabledChannels.contains(event.getChannel().getName())) {
								try {
									enabledChannels.add(event.getChannel().getName());
									PreparedStatement enableHook = IRCBot.getInstance().getPreparedStatement("enableHook");
									enableHook.setString(1, "Twitter");
									enableHook.setString(2, event.getChannel().getName());
									enableHook.executeUpdate();
									event.respond("Enabled Twitter");
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							} else if (param.equals("disable") && enabledChannels.contains(event.getChannel().getName())) {
								try {
									enabledChannels.remove(event.getChannel().getName());
									PreparedStatement disableHook = IRCBot.getInstance().getPreparedStatement("disableHook");
									disableHook.setString(1, "Twitter");
									disableHook.setString(2, event.getChannel().getName());
									disableHook.executeUpdate();
									event.respond("Disabled Twitter");
								} catch (Exception e) {
									e.printStackTrace();
								}
								return;
							}
						}
					}
				}

				if(enabledChannels.contains(event.getChannel().getName()) && twitter != null) {
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
		}
	}
}