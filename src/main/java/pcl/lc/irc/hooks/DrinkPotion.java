package pcl.lc.irc.hooks;

import org.joda.time.DateTime;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class DrinkPotion extends AbstractListener {
	private Command local_command;
	private static ArrayList<String> colors = new ArrayList<>();
	private static ArrayList<String> consistancies = new ArrayList<>();
	private static ArrayList<String> effects = new ArrayList<>();
	private static HashMap<String, Integer> potions = new HashMap<>();
	private static String day_of_potioning = "";

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);

		colors.add("blue");
		colors.add("red");
		colors.add("yellow");
		colors.add("purple");
		colors.add("green");
		colors.add("cyan");
		colors.add("tan");
		colors.add("black");
		colors.add("white");

		consistancies.add("viscous");
		consistancies.add("cloudy");
		consistancies.add("fluffy");
		consistancies.add("thick");
		consistancies.add("smelly");
		consistancies.add("fragrant");
		consistancies.add("light");
		consistancies.add("shiny");
		consistancies.add("porous");
		consistancies.add("ripe");
		consistancies.add("muddy");

		effects.add(" looks confused as nothing happens.");
		effects.add(" turns into a catgirl.");
		effects.add(" turns into a newt.");
		effects.add(" turns into a toad.");
		effects.add("'s hair turns {color}.");
		effects.add("'s skin turns {color}.");
		effects.add("'s toes turn invisible.");
		effects.add("'s hair grows three times longer.");
		effects.add(" gains the proportional strength of a chihuahua.");
		effects.add(" gains the ability to not be seen.");
		effects.add(" gains knowledge about a random useless subject.");
		effects.add(" gains an extra strand of hair on their face.");
		effects.add(" grows whiskers.");
		effects.add(" grows a tail from a random animal.");
		effects.add(" shrinks by a negligible amount.");
		effects.add(" grows slightly.");
		effects.add(" suddenly craves pie.");
		effects.add(" gains the ability to speak with bricks.");
		effects.add(" gains a strong urge to recycle the potion bottle.");
		effects.add("'s bed is suddenly slightly less comfortable.");
		effects.add(" gains a negligible amount of luck.");
		effects.add(" realizes this was actually a {color} {consistency} potion.");
		effects.add(" remembers an important appointment.");
		effects.add(" grows a mustache.");
		effects.add(" has a sudden but short lived desire to run around in a circle.");
		effects.add(" temporarily gains the ability to summon safety pins.");
		effects.add(" gains one point of strength");
		effects.add(" gains one point of agility");
		effects.add(" gains one point of speed");
		effects.add(" recovers some mana");
		effects.add(" looses one point of strength");
		effects.add(" looses one point of agility");
		effects.add(" looses one point of speed");
	}

	private void initCommands() {
		local_command = new Command("drink", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() > 0) {
					int effect = getPotionEffect(params);

					if (effect > 0) {
						String replace_color = colors.get(Helper.getRandomInt(0, colors.size() - 1));
						String replace_consistency = consistancies.get(Helper.getRandomInt(0, consistancies.size() - 1));
						Helper.sendMessage(target, nick + effects.get(effect).replace("{color}", replace_color).replace("{consistency}", replace_consistency));
					}
					else
						Helper.sendMessage(target, "This doesn't seem to be a potion I recognize...");
				}
				else
					Helper.sendMessage(target, "Drink what?");
			}
		};
		local_command.setHelpText("Drink a potion with a certain consistancy and color and something might happen.");
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

	public int getPotionEffect(ArrayList<String> params) {
		boolean is_potion = false;
		int color = 0;
		int consistency = 0;

		System.out.println(params.toString());

		for (String param : params) {
			param = param.toLowerCase();
			if (color == 0 && colors.indexOf(param) != -1)
				color = colors.indexOf(param);
			if (consistency == 0 && consistancies.indexOf(param) != -1)
				consistency = consistancies.indexOf(param);
			if (param.equals("potion"))
				is_potion = true;
		}

		if (!is_potion || color == 0 || consistency == 0) {
			return -1;
		}

		System.out.println("Color: " + color);
		System.out.println("Consistency: " + consistency);
		System.out.println("Has_potion: " + is_potion);

		if (day_of_potioning.equals(DateTime.now().toString("yyyy-MM-dd"))) {
			resetPotionList();
		}

		if (combinationHasEffect(consistency, color)) {
			int effect = getCombinationEffect(consistency, color);
			System.out.println("Effect recorded for " + consistency + "," + color + ": " + effect);
			return effect;
		} else {
			int effect = Helper.getRandomInt(0, effects.size() - 1);
			System.out.println("No effect recorded for " + consistency + "," + color + ", Assign " + effect);
			setCombinationEffect(consistency, color, effect);
			return effect;
		}
	}

	private void resetPotionList() {
		System.out.println("Resetting potion list!");
		potions = new HashMap<>();
		day_of_potioning = DateTime.now().toString("yyyy-MM-dd");
	}

	private boolean combinationHasEffect(int consistency, int color) {
		System.out.println(potions.toString());
		String key = consistency + "," + color;
		if (potions.containsKey(key))
			return true;
		return false;
	}

	private void setCombinationEffect(int consistancy, int color, int effect) {
		String key = consistancy + "," + color;
		potions.put(key, effect);
	}

	private int getCombinationEffect(int consistancy, int color) {
		String key = consistancy + "," + color;
		return potions.get(key);
	}
}
