package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.ArgumentTypes;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
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
		IRCBot.registerCommand(local_command);
	}

	private void initCommands() {
		local_command = new Command("jumble", new CommandArgumentParser(1, new CommandArgument(ArgumentTypes.STRING))) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = this.argumentParser.getArgument(0);
				if (str == null) {
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
					if (str.equals("^")) {
						List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
							if (entry.getValue().get(0).equals(target) && !entry.getValue().get(1).equals(IRCBot.getOurNick())) { //Look for correct channel and ignore messages from self
								if (entry.getValue().get(2).split(" ").length > 2) { //Select messages with more than two words
									str = entry.getValue().get(2);
									break;
								}
							}
						}
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
		local_command.setHelpText("It jumbles things. Try it. You'll be hooked.");
	}
}
