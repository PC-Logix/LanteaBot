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
				if (params.toLowerCase().equals(IRCBot.getOurNick().toLowerCase()))
					params = Helper.parseSelfReferral("himself");
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
		events.add("all NaN bugs");
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
		events.add("leaking the secrets");
		events.add("even suggesting such things");
		events.add("letting jerks join the channel");
		events.add("this text not being very interesting");
		events.add("the cats taking over in secret");
		events.add("certain comic book heroes dying and coming back, and dying again");
		events.add("that one thing. You know");
		events.add("it not being Friday. Or if it is Friday then for it being Friday");
		events.add("sticking googly eyes on all of the paintings");
		events.add("forgetting to feed the tentacle pit");
		events.add("that one spoon going missing");
		events.add("no typ so gud");
		events.add("whatever that was");
		events.add("the thing that just happened");
		events.add("the return of Vecna");
		events.add("1 not equalling 2");
		events.add("not being fast enough");
		events.add("doughnuts");
		events.add("the last nightmare I had");
		events.add("the next person getting bap'd");

		int roll = Helper.getRandomInt(0, (int)(events.size() * 1.25));

		try {
			return events.get(roll);
		} catch (Exception e) {}
		return "adding " + Inventory.getRandomItem(true).getName(false) + " to the inventory!";
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args, String callingRelay) {
		if (local_command.shouldExecute(command, event) >= 0) {
			chan = event.getChannel().getName();
		}
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange, String callingRelay) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
