/**
 * 
 */
package pcl.lc.irc.hooks;

import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;
import pcl.lc.utils.mcping.MinecraftPing;
import pcl.lc.utils.mcping.MinecraftPingOptions;
import pcl.lc.utils.mcping.MinecraftPingReply;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class MCInfo extends ListenerAdapter {
	public MCInfo() {
		IRCBot.registerCommand("mcinfo");
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		String prefix = IRCBot.commandprefix;
		String ourinput = event.getMessage().toLowerCase();
		String trigger = ourinput.trim();
		if (trigger.length() > 1) {
			String[] firstWord = StringUtils.split(trigger);
			String triggerWord = firstWord[0];
			if (triggerWord.equals(prefix + "mcinfo")) {
				String[] request = ourinput.split("\\s+");
				String server = request[1];
				String port = request[2];
				System.out.println(server + ":" + port);
				MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname(server).setPort(Integer.parseInt(port)));
				System.out.println(data.getDescription() + "  --  " + data.getPlayers().getOnline() + "/" + data.getPlayers().getMax());
			}
		}
	}
}