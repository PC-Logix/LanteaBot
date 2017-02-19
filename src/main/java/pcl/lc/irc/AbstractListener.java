package pcl.lc.irc;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
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

	/**
	 * Called only on channel messages. Checks if the command is prefixed with the current command prefix
	 * @param sender
	 * @param event
	 * @param command
	 * @param args
	 */
	public abstract void handleCommand(String sender, MessageEvent event, String command, String[] args);
	
	/**
	 * Called on channel messages, and queries, the event does NOT have any channel information regardless of the origin.
	 * Checks if the command is prefixed with the current command prefix
	 * @param nick
	 * @param event
	 * @param command
	 * @param copyOfRange
	 */
	public abstract void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange);
	
	/**
	 * Called only on channel messages.  Unlike handleCommand this does NOT check if the command is prefixed by the command prefix.
	 * This is useful for triggering on random words said in a message.
	 * @param sender
	 * @param event
	 * @param command
	 * @param args
	 */
	public abstract void handleMessage(String sender, MessageEvent event, String command, String[] args);
	
	/**
	 * Called on channel messages, and queries, the event does NOT have any channel information regardless of the origin.
	 * Does NOT check if the command is prefixed with the current command prefix
	 * This is useful for triggering on random words said in a message.
	 * @param nick
	 * @param event
	 * @param command
	 * @param copyOfRange
	 */
	public abstract void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange);

	@Override
	public void onMessage(final MessageEvent event) {
		String[] splitMessage = event.getMessage().split(" ");
		if (splitMessage[0].startsWith(Config.commandprefix)) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				handleCommand(event.getUser().getNick(), event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length));
			}
		} else if ((splitMessage[0].startsWith("<") && splitMessage[0].endsWith(">") || splitMessage[0].startsWith("(") && splitMessage[0].endsWith(")")) && splitMessage[1].startsWith(Config.commandprefix)) {
			String sender = splitMessage[0].substring(1,splitMessage[0].length()-1);
			if (!IRCBot.isIgnored(sender)) {
				handleMessage(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length));
				handleCommand(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length));
			}
		} else {
			handleMessage(event.getUser().getNick(), event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length));
		}
	}
	
	@Override
	public void onGenericMessage(final GenericMessageEvent event) {
		String[] splitMessage = event.getMessage().split(" ");
		if (splitMessage[0].startsWith(Config.commandprefix)) {
			if (!IRCBot.isIgnored(event.getUser().getNick())) {
				handleCommand(event.getUser().getNick(), event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length));
			}
		} else if ((splitMessage[0].startsWith("<") && splitMessage[0].endsWith(">") || splitMessage[0].startsWith("(") && splitMessage[0].endsWith(")")) && splitMessage[1].startsWith(Config.commandprefix)) {
			String sender = splitMessage[0].substring(1,splitMessage[0].length()-1);
			if (!IRCBot.isIgnored(sender)) {
				handleMessage(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length));
				handleCommand(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length));
			}
		} else {
			handleMessage(event.getUser().getNick(), event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length));
		}
	}
}