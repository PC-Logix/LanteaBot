package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class RandomChoice extends AbstractListener {
	private Command local_command;
	private List<String> output;

	@Override
	protected void initHook() {
		output = new ArrayList<>();
		output.add("Some {choice} sounds nice");
		output.add("I'm 40% {choice}!");
		output.add("You *could* do {choice}, I guess.");
		output.add("Why not {count}?");
		output.add("I sense some {choice} in your future!");
		output.add("{choice} is for cool kids!");
		output.add("Definitely {choice}. or maybe {other_choice}...");
		output.add("If I had a gold nugget for every time someone asked me about {choice}");
		output.add("The proof is in the pudding. Definitely {choice}.");
		output.add("I received a message from future you, said to go with {choice}.");
		output.add("I was that {choice} is the best choice in a vision");
		output.add("You'll want to go with {choice}.");
		output.add("Elementary dear Watson, {choice} is the obvious choice!");
		output.add("My grandfather always told me that {choice} is the way to go!");
		output.add("If I've learned anything in life it's that you always pick {choice}");
		output.add("Once you get a taste of {choice} you can't stop.");
		output.add("One the one hand, there's {choice} but then there's also {other_choice}");
		output.add("Somebody once told me to roll with {choice}");

		local_command = new Command("choose", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String[] parts = params.split(" or ");
				String msg = output.get(Helper.getRandomInt(0, output.size() - 1));

				msg = msg.replaceAll("\\{choice}", parts[Helper.getRandomInt(0, parts.length - 1)].trim());
				if (parts.length > 1)
					msg = msg.replaceAll("\\{other_choice}", parts[Helper.getRandomInt(0, parts.length - 1)].trim());
				else
					msg = msg.replaceAll("\\{other_choice}", "something else");
				String count = "";
				switch (parts.length)
				{
					case 1:
						msg = "The choice seems obvious..."; break;
					case 2:
						count = "both"; break;
					case 3:
						count = "all three"; break;
					case 4:
						count = "all four"; break;
					case 5:
						count = "all five"; break;
					case 6:
						count = "all six"; break;
					case 7:
						count = "all seven"; break;
					case 8:
						count = "all eight"; break;
					case 9:
						count = "all nine"; break;
					case 10:
						count = "all ten"; break;
					default:
						count = "all " + parts.length;
				}
				if (count != "")
					msg = msg.replaceAll("\\{count}", count);

				Helper.sendMessage(target, msg, nick);
			}
		}; local_command.setHelpText("Randomly picks a choice for you.");
		local_command.registerAlias("choice");
		local_command.registerAlias("pick");
		IRCBot.registerCommand(local_command);
	}

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		chan = event.getChannel().getName();
	}

	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		target = Helper.getTarget(event);
		local_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
