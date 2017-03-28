package pcl.lc.irc.hooks;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Responses extends ListenerAdapter {

	@SuppressWarnings({ "unchecked" })
	@Override
	public void onMessage(final MessageEvent event) throws Exception {
		super.onMessage(event);
		
		if (event.getMessage().toLowerCase().contains(IRCBot.getOurNick().toLowerCase())) {
			ArrayList<String[]> respondTo = new ArrayList<>();
			respondTo.add(new String[]{"thanks", "1"});
			respondTo.add(new String[]{"thank you", "1"});

			respondTo.add(new String[]{"seriously", "2"});
			respondTo.add(new String[]{"srsly", "2"});
			respondTo.add(new String[]{"how dare you", "2"});
			respondTo.add(new String[]{"howdareyou", "2"});
			respondTo.add(new String[]{"no u", "2"});
			respondTo.add(new String[]{"no you", "2"});

			respondTo.add(new String[]{"you're welcome", "3"});
			respondTo.add(new String[]{"youre welcome", "3"});

			respondTo.add(new String[]{"good", "4"});
			respondTo.add(new String[]{"excellent", "4"});
			respondTo.add(new String[]{"nice", "4"});

			for (String[] str : respondTo) {
				if (event.getMessage().toLowerCase().contains(str[0])) {
					respond(str[1], event);
					break;
				}
			}
		}
	}

	private void respond(String type, MessageEvent event) {
		switch (type) {
			case "1":
				event.respond("You're welcome!");
				break;
			case "2":
				event.respond(Helper.get_surprise_response());
				break;
			case "3":
				event.respond("What?");
				break;
			case "4":
				event.respond(Helper.get_thanks_response());
				break;
		}
	}
}
