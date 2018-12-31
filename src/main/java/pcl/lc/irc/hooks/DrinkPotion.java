package pcl.lc.irc.hooks;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
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
public class DrinkPotion extends AbstractListener {
	private Command local_command;
	private Command get_random;
	private static ArrayList<String> colors = new ArrayList<>();
	private static ArrayList<String> consistencies = new ArrayList<>();
	private static ArrayList<String> effects = new ArrayList<>();
	private static HashMap<String, Integer> potions = new HashMap<>();
	private static String day_of_potioning = "";

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(get_random);

		colors.add("blue");
		colors.add("red");
		colors.add("yellow");
		colors.add("purple");
		colors.add("green");
		colors.add("cyan");
		colors.add("tan");
		colors.add("black");
		colors.add("white");
		colors.add("pink");
		colors.add("gold");
		colors.add("silver");
		colors.add("tomato");
		colors.add("lime");
		colors.add("sky blue");
		colors.add("citrus");
		colors.add("strawberry");
		colors.add("chocolate");
		colors.add("tuna");

		consistencies.add("viscous");
		consistencies.add("cloudy");
		consistencies.add("fluffy");
		consistencies.add("thick");
		consistencies.add("smelly");
		consistencies.add("fragrant");
		consistencies.add("light");
		consistencies.add("shiny");
		consistencies.add("porous");
		consistencies.add("ripe");
		consistencies.add("muddy");
		consistencies.add("shimmering");
		consistencies.add("gloomy");

		effects.add(" looks confused as nothing happens.");
		effects.add(" turns into a catgirl.");
		effects.add(" turns into a newt.");
		effects.add(" turns into a toad.");
		effects.add("'s hair turns to the color of {color}.");
		effects.add("'s skin turns to the color of {color}.");
		effects.add("'s toes turn invisible.");
		effects.add("'s hair grows three times longer.");
		effects.add(" gains the proportional strength of a chihuahua.");
		effects.add(" gains the ability to not be seen.");
		effects.add(" gains knowledge about a random useless subject.");
		effects.add(" gains an extra strand of hair on their face.");
		effects.add(" grows whiskers.");
		effects.add(" grows a tail from a {animal}.");
		effects.add(" shrinks by a negligible amount.");
		effects.add(" grows slightly.");
		effects.add(" suddenly craves pie.");
		effects.add(" gains the ability to talk to bricks.");
		effects.add(" gains a strong urge to recycle the potion bottle.");
		effects.add("'s bed is suddenly slightly less comfortable.");
		effects.add(" gains a negligible amount of luck.");
		effects.add(" realizes this was actually a {color} {consistency} potion.");
		effects.add(" remembers an important appointment.");
		effects.add(" grows a mustache.");
		effects.add(" has a sudden but short lived desire to run around in a circle.");
		effects.add(" temporarily gains the ability to summon safety pins.");
		effects.add(" gains one point of strength.");
		effects.add(" gains one point of agility.");
		effects.add(" gains one point of speed.");
		effects.add(" recovers some mana.");
		effects.add(" loses one point of strength.");
		effects.add(" loses one point of agility.");
		effects.add(" loses one point of speed.");
		effects.add(" gains an additional bone.");
		effects.add(" is suddenly more aware of cute things nearby");
		effects.add(" loses exactly a handful of luck");
		effects.add("'s clothes turn completely black");
		effects.add("'s pockets suddenly contain a number of marbles");
		effects.add("'s favourite hat is suddenly on fire");
		effects.add(" has a single tear roll down their cheek for some reason");
		effects.add("'s nose vanish for one minute.");
		effects.add(" feels like a champion!");
		effects.add("'s nail turns to the color of {color}");
		effects.add("'s bones turn blue");
	}

	private void initCommands() {
		local_command = new Command("drink", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				if (params.size() > 0) {
					if (params.get(0).equals("^")) {
						List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
						for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
							if (entry.getValue().get(0).equals(target)) {
								if (entry.getValue().get(2).toLowerCase().contains("potion")) {
									String[] words = entry.getValue().get(2).split(" ");
									params = new ArrayList<>();
									params.addAll(Arrays.asList(words));
									break;
								}
							}
						}
					}

					int effect = getPotionEffect(params);

					if (effect > 0) {
						String replace_color = colors.get(Helper.getRandomInt(0, colors.size() - 1));
						String replace_consistency = consistencies.get(Helper.getRandomInt(0, consistencies.size() - 1));
						Helper.sendMessage(target, nick + effects.get(effect).replace("{color}", replace_color).replace("{consistency}", replace_consistency).replace("{animal}", Helper.getRandomAnimal(true)));
					}
					else
						Helper.sendMessage(target, "This doesn't seem to be a potion I recognize...");
				}
				else
					Helper.sendMessage(target, "Drink what?");
			}
		};
		local_command.setHelpText("Drink a potion with a certain consistency and color and something might happen.");

		get_random = new Command("randompotion", 10) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, "You get a " + getRandomPotion(), nick);
			}
		};
		get_random.setHelpText("Get a random potion");
		get_random.registerAlias("potion");
		get_random.registerAlias("randpotion");
		get_random.registerAlias("gimmepotion");
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
		get_random.tryExecute(command, nick, target, event, copyOfRange);
	}

	public int getPotionEffect(ArrayList<String> params) {
		boolean is_potion = false;
		int color = -1;
		int consistency = -1;

		System.out.println(params.toString());

		for (String param : params) {
			param = param.toLowerCase();
			if (color == -1 && colors.indexOf(param) != -1)
				color = colors.indexOf(param);
			if (consistency == -1 && consistencies.indexOf(param) != -1)
				consistency = consistencies.indexOf(param);
			if (param.replace(".", "").equals("potion"))
				is_potion = true;
		}

		System.out.println("Color: " + color);
		System.out.println("Consistency: " + consistency);
		System.out.println("Has_potion: " + is_potion);

		if (!is_potion || color == -1 || consistency == -1) {
			return -1;
		}

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

	private void setCombinationEffect(int consistency, int color, int effect) {
		String key = consistency + "," + color;
		potions.put(key, effect);
	}

	private int getCombinationEffect(int consistency, int color) {
		String key = consistency + "," + color;
		return potions.get(key);
	}

	public static String getRandomPotion() {
		int color = Helper.getRandomInt(0, colors.size() - 1);
		int consistency = Helper.getRandomInt(0, consistencies.size() - 1);

		return consistencies.get(consistency) + " " + colors.get(color) + " potion";
	}
}
