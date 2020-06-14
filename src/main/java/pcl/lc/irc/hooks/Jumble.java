package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.*;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Jumble extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "It jumbles things. Try it. You'll be hooked.");
	}

	private void initCommands() {
		local_command = new Command("jumble") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = "";
				if (params == "") {
					ArrayList<String> messages = new ArrayList<>();
					int limit = 10; // Max number of messages to look back for
					int counter = 0;
					List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
					for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
						if (counter == limit)
							break;
						if (entry.getValue().get(0).equals(target) && !entry.getValue().get(1).equals(IRCBot.getOurNick())) { //Look for correct channel and ignore messages from self
							if (entry.getValue().get(2).split(" ").length > 2) { //Select messages with more than two words
								messages.add(entry.getValue().get(2));
								counter++;
							}
						}
					}
					str = messages.get(Helper.getRandomInt(0, messages.size() - 1));
				} else {
					if (params.equals("^")) {
						List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
							if (entry.getValue().get(0).equals(target) && !entry.getValue().get(1).equals(IRCBot.getOurNick())) { //Look for correct channel and ignore messages from self
								if (entry.getValue().get(2).split(" ").length > 2) { //Select messages with more than two words
									str = entry.getValue().get(2);
									break;
								}
							}
						}
					} else {
						str = params;
					}
				}
				ArrayList<String> words = new ArrayList<>(Arrays.asList(str.split(" ")));
				Collections.shuffle(words);

				str = "";
				for (String word : words) {
					str += word + " ";
				}
				str = str.trim();
				Helper.sendMessage(target, str);
			}
		};
		local_command.registerAlias("yoda");
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
