package pcl.lc.irc;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractListener extends ListenerAdapter
{
	// key = command name, value = description
	private final Map<String, String> commands = new HashMap<>();

	public AbstractListener() {
		initCommands();
	}

	protected abstract void initCommands();

	public Map<String, String> getCommands() {
		return commands;
	}

	public abstract void handleCommand(String sender, MessageEvent event, String command, String[] args);

	@Override
	public void onMessage(final MessageEvent event) {
		String[] splitMessage = event.getMessage().split(" ");
		if (splitMessage[0].startsWith(Config.commandprefix)) {
			handleCommand(event.getUser().getNick(), event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length));
		} else if (splitMessage[0].startsWith("<") && splitMessage[0].endsWith(">") && splitMessage[1].startsWith(Config.commandprefix)) {
			String sender = splitMessage[0].substring(1,splitMessage[0].length()-1);
			handleCommand(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length));
		}
	}
}