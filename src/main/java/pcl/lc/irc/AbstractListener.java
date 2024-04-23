package pcl.lc.irc;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.utils.Helper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public abstract class AbstractListener extends ListenerAdapter
{
	// key = command name, value = description
	private final Map<String, String> commands = new HashMap<>();

	public AbstractListener() {
		initHook();
	}

	/**
	 * Called when the class is loaded, initialize your commands and any Database stuff here
	 */
	protected abstract void initHook();

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
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
	}

	/**
	 * Called on channel messages, and queries, the event does NOT have any channel information regardless of the origin.
	 * Checks if the command is prefixed with the current command prefix
	 * @param nick
	 * @param event
	 * @param command
	 * @param copyOfRange
	 */
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
	}

	/**
	 * Called only on channel messages.  Unlike handleCommand this does NOT check if the command is prefixed by the command prefix.
	 * This is useful for triggering on random words said in a message.
	 * @param sender
	 * @param event
	 * @param args
	 */
	public void handleMessage(String sender, MessageEvent event, String[] args) {

	}

	/**
	 * Called on channel messages, and queries, the event does NOT have any channel information regardless of the origin.
	 * Does NOT check if the command is prefixed with the current command prefix
	 * This is useful for triggering on random words said in a message.
	 * @param nick
	 * @param event
	 * @param copyOfRange
	 */
	public void handleMessage(String nick, GenericMessageEvent event, String[] copyOfRange) {
	}

	@Override
	public void onMessage(final MessageEvent event) {
		for (String str : Config.ignoreMessagesEndingWith) {
			if (event.getMessage().endsWith(str))
				return;
		}
		for (String str : Config.ignoreMessagesStartingWith) {
			if (event.getMessage().startsWith(str)) {
				System.out.println("Ignored '" + event.getMessage() + "' because it starts with '" + str + "'");
				return;
			}
		}
		String[] splitMessage = event.getMessage().split(" ");
		String nickClean = event.getUser().getNick().replaceAll("\\p{C}", "");
		String nick = event.getUser().getNick();
		if (splitMessage[0].startsWith(Config.commandprefix) || splitMessage[0].startsWith(IRCBot.ournick)) {
			if (!IRCBot.isIgnored(nickClean)) {
				handleCommand(nick, event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length), null);
				handleMessage(nick, event, Arrays.copyOfRange(splitMessage, 0, splitMessage.length));
			}
		} else if (Config.parseBridgeCommandsFromUsers.contains(nick) && (splitMessage[0].startsWith("<") && splitMessage[0].endsWith(">") || splitMessage[0].startsWith("(") && splitMessage[0].endsWith(")"))) {
			String sender = splitMessage[0].substring(1,splitMessage[0].length()-1).replaceAll("\\p{C}", "");
			if (!IRCBot.isIgnored(sender)) {
				if (splitMessage[1].startsWith(Config.commandprefix)) {
					handleCommand(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length), nickClean);
				}
				handleMessage(sender, event, Arrays.copyOfRange(splitMessage,1,splitMessage.length));
			}
		} else {
			if (!IRCBot.isIgnored(nickClean)) {
				handleMessage(nick, event, Arrays.copyOfRange(splitMessage, 0, splitMessage.length));
			}
		}
	}

	@Override
	public void onGenericMessage(final GenericMessageEvent event) {
		String[] splitMessage = event.getMessage().split(" ");
		String nickClean = Helper.cleanNick(event.getUser().getNick());
		String nick = Helper.cleanNick(event.getUser().getNick());
		if (splitMessage[0].startsWith(Config.commandprefix)) {
			if (!IRCBot.isIgnored(nickClean)) {
				handleCommand(nick, event, splitMessage[0], Arrays.copyOfRange(splitMessage, 1, splitMessage.length), null);
			}
		} else if (Config.parseBridgeCommandsFromUsers.contains(nickClean) && (splitMessage[0].startsWith("<") && splitMessage[0].endsWith(">") || splitMessage[0].startsWith("(") && splitMessage[0].endsWith(")")) && splitMessage[1].startsWith(Config.commandprefix)) {
			String sender = Helper.cleanNick(splitMessage[0].substring(1,splitMessage[0].length()-1));
			if (!IRCBot.isIgnored(sender)) {
				handleMessage(sender, event, Arrays.copyOfRange(splitMessage,2,splitMessage.length));
				handleCommand(sender, event, splitMessage[1], Arrays.copyOfRange(splitMessage,2,splitMessage.length), nickClean);
			}
		} else {
			if (!IRCBot.isIgnored(Helper.cleanNick(event.getUser().getNick()))) {
				handleMessage(Helper.cleanNick(event.getUser().getNick()), event, Arrays.copyOfRange(splitMessage, 1, splitMessage.length));
			}
		}
	}
}