/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.*;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Blame extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Project problems onto someone else!");
	}

	private void initCommands() {
		local_command = new Command("blame", 5) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendAction(target, "blames " + params + " for " + getEvent());
			}
		};
	}

	private String getEvent() {
		ArrayList<String> events = new ArrayList<>();
		events.add("Inari's lewdness!");
		events.add("the existence of wasps!");
		events.add("E.T for Atari being terrible!");
		events.add("space being cold!");
		events.add("all of the bugs%&");
		events.add("running being exhausting");
		events.add("ruptured tires");
		events.add("the existence of trolls");
		events.add("bridge fees");
		events.add("slow internet speeds");
		events.add("the zombie breakout");
		events.add("driving on the wrong side");
		events.add("Half-life 3 not being out");
		events.add("doubling the time until release by asking questions");
		events.add("the moon not being made of cheese");

		int roll = Helper.getRandomInt(0, (int)(events.size() * 1.25));

		try {
			return events.get(roll);
		} catch (Exception e) {}
		return "adding " + Inventory.getRandomItem(true).getName(false) + " to the inventory!";
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (local_command.shouldExecute(command) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
			target = nick;
		} else {
			target = chan;
		}
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		// TODO Auto-generated method stub
		
	}
}
