/**
 * 
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class dice extends AbstractListener {

	/**
	 * Returns a random number between min (inclusive) and max (exclusive)
	 * http://stackoverflow.com/questions/1527803/generating-random-whole-numbers-in-javascript-in-a-specific-range#1527820
	 */
	public double getRandomArbitrary(Integer min, Integer max) {
		return Math.random() * (max - min) + min;
	}

	/**
	 * Returns a random integer between min (inclusive) and max (inclusive)
	 * Using Math.round() will give you a non-uniform distribution!
	 * http://stackoverflow.com/questions/1527803/generating-random-whole-numbers-in-javascript-in-a-specific-range#1527820
	 */
	public Integer getRandomInt(Integer min, Integer max) {
		return (int) (Math.floor(Math.random() * (max - min + 1)) + min);
	}

	public String rollDice(String dice) {
		final String regex = "(\\d\\d?\\d?)d(\\d\\d?\\d?)";

		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(dice);

		if (matcher.matches()) {
			Integer num_dice = Math.min(100, Integer.valueOf(matcher.group(1)));
			Integer dice_size = Integer.valueOf(matcher.group(2));

			ArrayList<Integer> results = new ArrayList<>(100);
			for (Integer i = 0; i < num_dice; i++)
			{
				Integer steps = getRandomInt(1, 12);
				Integer gone = 0;
				Integer result = 1;
				for (result = 1; gone < steps; gone++)
				{
					if (Objects.equals(result, dice_size))
						result = 0;
					result++;
				}
				results.add(result);
			}
			return results.toString();
		}
		else {
			return "Invalid dice format (Eg 1d6)";
		}
	}

	@Override
	protected void initCommands() {
		IRCBot.registerCommand("roll", "Rolls dice");
	}

	public String dest;

	public String chan;
	public String target = null;
	@Override
	public void handleCommand(String sender, MessageEvent event, String command, String[] args) {
		if (command.equals(Config.commandprefix + "roll")) {
			chan = event.getChannel().getName();
		}
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void handleCommand(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {
		if (command.equals(Config.commandprefix + "roll")) {
			if (!event.getClass().getName().equals("org.pircbotx.hooks.events.MessageEvent")) {
				target = nick;
			} else {
				target = chan;
			}
			String message = "";
			for (String aCopyOfRange : copyOfRange)
			{
				message = message + " " + aCopyOfRange;
			}
			String s = message.trim();
			IRCBot.getInstance().sendMessage(target ,  Helper.antiPing(nick) + ": " + rollDice(s));
		}
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
