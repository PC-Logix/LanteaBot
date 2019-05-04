package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.joda.time.DateTime;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.ColorEntry;
import pcl.lc.utils.Helper;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class DrinkPotion extends AbstractListener {
	private Command local_command;
	private Command get_random;
	private Command potion_stats;
	private Command discovered;
	private static ArrayList<ColorEntry> colorEntries = new ArrayList<>();
	private static ArrayList<String> consistencies = new ArrayList<>();
	private static ArrayList<String> effects = new ArrayList<>();
	private static HashMap<String, EffectEntry> potions = new HashMap<>();
	private static String day_of_potioning = "";

	@Override
	protected void initHook() {
		initCommands();
        httpd.registerContext("/potions", new PotionHandler(), "Potions");
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(get_random);
		IRCBot.registerCommand(potion_stats);
		IRCBot.registerCommand(discovered);

		colorEntries.add(new ColorEntry("blue", "a"));
		colorEntries.add(new ColorEntry("red", "a"));
		colorEntries.add(new ColorEntry("röd", "a"));
		colorEntries.add(new ColorEntry("rød", "a"));
		colorEntries.add(new ColorEntry("yellow", "a"));
		colorEntries.add(new ColorEntry("purple", "a"));
		colorEntries.add(new ColorEntry("green", "a"));
		colorEntries.add(new ColorEntry("cyan", "a"));
		colorEntries.add(new ColorEntry("tan", "a"));
		colorEntries.add(new ColorEntry("black", "a"));
		colorEntries.add(new ColorEntry("white", "a"));
		colorEntries.add(new ColorEntry("pink", "a"));
		colorEntries.add(new ColorEntry("gold", "a", "{color} colored {item}"));
		colorEntries.add(new ColorEntry("silver", "a", "{color} colored {item}"));
		colorEntries.add(new ColorEntry("tomato", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("lime", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("citrus", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("strawberry", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("chocolate", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("orange", "an"));
		colorEntries.add(new ColorEntry("tuna", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("salmon", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("rainbow", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("void", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("ocean", "an", "{color} colored {item}", "the color of the {color}"));
		colorEntries.add(new ColorEntry("grass", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("sky", "a", "{color} colored {item}", "the color of the {color}"));
		colorEntries.add(new ColorEntry("rock", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("metal", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("copper", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("aqua", "an", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("dirt", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("quicksilver", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("rust", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("coral", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("transparent", "a", "{color} {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("water", "a", "{color} colored {item}", "the color of {color}"));
		colorEntries.add(new ColorEntry("weather", "a"));

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
		consistencies.add("prickly");
		consistencies.add("sour");
		consistencies.add("salty");
		consistencies.add("sweet");
		consistencies.add("runny");
		consistencies.add("boiling");
		consistencies.add("freezing");
		consistencies.add("sedimented");
		consistencies.add("warpy");
		consistencies.add("basic");
		consistencies.add("stirring");
		consistencies.add("bubbly");
		consistencies.add("gloopy");
		consistencies.add("goopy");
		consistencies.add("slimy");
		consistencies.add("solid");
		consistencies.add("molten");
		consistencies.add("fiery");
		consistencies.add("dull");
		consistencies.add("resonating");
		consistencies.add("shining");
		consistencies.add("seeping");
		consistencies.add("smooth");
		consistencies.add("soft");

		//Valid tags: {user},{color},{turn_color},{color:<item>:p},{consistency},{transformation},{transformation2},{transformations},{transformations2}
		effects.add("{user} looks confused as nothing happens.");
		effects.add("{user} turns into a {transformation} girl.");
		effects.add("{user} turns into a {transformation} boy.");
		effects.add("{user} turns into a {transformation}.");
		effects.add("{user} turns into a {transformation} {transformation2}.");
		effects.add("{user} turns into a {transformation} {transformation2} girl.");
		effects.add("{user} turns into a {transformation} {transformation2} boy.");
		effects.add("{user}'s hair turn {turn_color}.");
		effects.add("{user}'s skin turn {turn_color}.");
		effects.add("{user}'s nails turn {turn_color}.");
		effects.add("{user}'s bones turn {turn_color}.");
		effects.add("{user}'s clothes turn {turn_color}.");
		effects.add("{user}'s toes turn invisible.");
		effects.add("{user}'s hair grows three times longer.");
		effects.add("{user} gains the proportional strength of a {transformation}.");
		effects.add("{user} now knows how not to be seen.");
		effects.add("{user} gains knowledge about a random useless subject.");
		effects.add("{user} gains an extra strand of hair on their face.");
		effects.add("{user} grows whiskers.");
		effects.add("{user} grows a tail from a {transformation}.");
		effects.add("{user} shrinks by a negligible amount.");
		effects.add("{user} grows slightly.");
		effects.add("{user} suddenly craves pie.");
		effects.add("{user} gains the ability to talk to bricks.");
		effects.add("{user} feels a strong urge to recycle the potion bottle.");
		effects.add("{user}'s bed is suddenly slightly less comfortable.");
		effects.add("{user} gains a negligible amount of luck.");
		effects.add("{user} realizes this was actually a {consistency} {color} potion.");
		effects.add("{user} remembers an important appointment.");
		effects.add("{user} grows a mustache.");
		effects.add("{user} has a sudden but short lived desire to run around in a circle.");
		effects.add("{user} temporarily gains the ability to summon safety pins.");
		effects.add("{user} feels slightly stronger."); // gain 1 point of strength
		effects.add("{user} feels slightly more agile."); // gain 1 point of agility
		effects.add("{user} feels slightly faster."); // gain 1 point of speed
		effects.add("{user} recovers some mana.");
		effects.add("{user} feels slightly weaker.");
		effects.add("{user} feels slightly less agile.");
		effects.add("{user} feels slightly slower.");
		effects.add("{user} gains an additional bone.");
		effects.add("{user} is suddenly more aware of cute things nearby.");
		effects.add("{user} loses exactly a handful of luck.");
		effects.add("{user}'s pockets suddenly contain a number of {color:marbles:}.");
		effects.add("{user}'s favourite hat is suddenly on fire.");
		effects.add("{user} has a single tear roll down their cheek for some reason.");
		effects.add("{user}'s nose vanish for one minute.");
		effects.add("{user} feels like a champion!");
		effects.add("{user} feels a sudden surge of static electricity.");
		effects.add("{user}'s shoes are now slightly too large.");
		effects.add("{user} is now Borg.");
		effects.add("{user} has no memory of drinking a potion.");
		effects.add("{user} knows the exact location of a particular molecule of oxygen for a second.");
		effects.add("{user} thinks the empty bottle is a snake.");
		effects.add("{user} gets an urge to have another potion.");
		effects.add("It tastes sweet.");
		effects.add("It tastes sour.");
		effects.add("It tastes bitter.");
		effects.add("It tastes salty.");
		effects.add("{user} feels like one particular wasp has it out for them suddenly.");
		effects.add("{user} zones out for a second.");
		effects.add("A warpzone opens up next to {user}.");
		effects.add("After the first sip the potion poofs away.");
		effects.add("{user} looks up and sees the moon smile at them for a second.");
		effects.add("The ghost of a plant haunts you for a while.");
		effects.add("Three nearby pebbles suddenly shift slightly in your direction.");
		effects.add("The next pie you eat tastes slightly less good.");
		effects.add("Sitting down suddenly sounds like a really terrible idea.");
		effects.add("The next fork you touch tells you it's most deeply guarded secret.");
		effects.add("A voice whispers into your ear \"Drink or be drunk\" as it fades away as you drink the potion.");
		effects.add("You briefly feel like you have just stepped out of a car.");
		effects.add("True enlightenment can be achieved by drinking another potion.");
		effects.add("For about 5 seconds you know the location of a great treasure.");
		effects.add("The potion was inside you all along.");
		effects.add("You are suddenly wearings gloves you don't remember putting on.");
		effects.add("A sudden craving for soup occupies your thoughts.");
		effects.add("{user} suddenly forgets a random piece of trivia.");
		effects.add("A {transformation} flies past that vaguely resembles someone you know.");
		effects.add("{user} reboots for an update.");
		effects.add("Dramatic music briefly plays in the distance.");
		effects.add("You have a feeling that your face just appeared on a random vegetable somewhere.");
		effects.add("The potion bottle is suddenly on fire!");
		effects.add("Once empty the potion bottle fills with a differently colored potion.");
		effects.add("{user} gains the ability to talk to {transformations}.");
		effects.add("You see the sky briefly flash solid dark blue then go back to normal.");
		effects.add("When you drink the last drop, a bucket of water materializes above your head and dumps it contents over you, then vanishes. The water does not.");
		effects.add("Suddenly there's a swarm of wasps behind you!");
		effects.add("When you bring the bottle down you see {color:plastic flamingo:p}. It stares into your soul.");
		effects.add("A bard starts playing a lute behind you. They don't stop.");
		effects.add("The bottle turns into a sword.");
		effects.add("The bottle turns into an axe.");
		effects.add("The bottle turns into a spear.");
		effects.add("The bottle turns into a dagger.");
		effects.add("The bottle turns into a bow.");
		effects.add("The bottle turns into a trident.");
		effects.add("The bottle turns into a sling.");
		effects.add("A genie appears out of the empty bottle, turns it into a pie, then vanishes.");
		effects.add("You think \"What if, like, *we* are the potions man?\", you then realize that makes no sense at all.");
		effects.add("After drinking the potion you notice a label that says \"Side effects may include giggle fits and excessive monologuing.\"");
		effects.add("You forget the location of a great treasure.");
		effects.add("Oh no, you got a potion, there's probably a boss fight coming!");
		effects.add("Who needs internal organs anyway? There's an acidic tinge to the potion...");
		effects.add("You feel much better!");
		effects.add("A tiny cloud appears with a ridiculous smile on it. It follows you.");
		effects.add("The potion contained a computer virus! But your anti-virus routines destroy it.");
		effects.add("The potion contained a computer virus! It just changed your background...");
		effects.add("The bottle splits into two revealing a smaller {consistency} {color} potion.");
		effects.add("A tiny genie appears, gives you a thumbs up and poofs away.");
		effects.add("You feel chill.");
		effects.add("You feel the need to smash sometimes for some reason.");
		effects.add("You feel the need to use the \"shell\" command.");
		effects.add("You feel the need to use the \"fling\" command.");
		effects.add("The bottle turns into a pie.");
		effects.add("The bottle turns into a piece of bacon.");
		effects.add("The bottle turns into an apple.");
		effects.add("A bard behind you suddenly stops playing. They were most likely eaten by a monster.");
		effects.add("Gravity reverses for {user] for a second.");
		effects.add("{user} now has a mullet for 30 minutes.");
		effects.add("The next remote you look for is extra hard to find.");
	}
    static String html;

    public DrinkPotion() throws IOException {
        InputStream htmlIn = getClass().getResourceAsStream("/html/potions.html");
        html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
    }

	private void initCommands() {
		local_command = new Command("drink", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
			    try {
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

                        EffectEntry effect = getPotionEffect(params, nick);

                        if (effect != null) {
                            Helper.sendMessage(target, effect.toString().replace("{user}", nick));
                        } else
                            Helper.sendMessage(target, "This doesn't seem to be a potion I recognize...");
                    } else
                        Helper.sendMessage(target, "Drink what?");
                } catch (Exception ex) {
			        ex.printStackTrace();
                }
			}
		};
		local_command.setHelpText("Drink a potion with a certain consistency and color and something might happen.");
		local_command.registerAlias("chug");
		local_command.registerAlias("toast");
		local_command.registerAlias("sip");
		local_command.registerAlias("ingest");
		local_command.registerAlias("consume");
		local_command.registerAlias("use");
		local_command.registerAlias("absorb");
		local_command.registerAlias("engross");
		local_command.registerAlias("quaff");

		get_random = new Command("randompotion", 10) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
			    String[] potion = getRandomPotion();
				Helper.sendMessage(target, "You get a " + potion[0] + " " + potion[1] + " potion" + (potion[2] == "new" ? " (New!)" : ""), nick);
			}
		};
		get_random.setHelpText("Get a random potion");
		get_random.registerAlias("potion");
		get_random.registerAlias("randpotion");
		get_random.registerAlias("gimmepotion");

		potion_stats = new Command("potionstats", 10) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Config.httpdEnable.equals("true")){
					Helper.sendMessage(target, "Potion shelf: " + httpd.getBaseDomain() + "/potions", nick);
				} else {
					int color_count = colorEntries.size();
					int consistencies_count = consistencies.size();
					int effect_count = effects.size();
					int combination_count = color_count * consistencies_count;
					Helper.sendMessage(target, "There are " + color_count + " colorEntries, " + consistencies_count + " consistencies! That's " + combination_count + " potion combinations! There are " + effect_count + " effects!");
				}
			}
		};

		discovered = new Command("discovered", 10) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Config.httpdEnable.equals("true")){
					Helper.sendMessage(target, "Potion shelf: " + httpd.getBaseDomain() + "/potions", nick);
				} else {
					int potions_count = potions.size();
					Helper.sendMessage(target, potions_count + " combination" + (potions_count == 1 ? " has" : "s have") + " been found today!");
				}
			}
		};
		potion_stats.registerAlias("potionsdiscovered");
		potion_stats.registerAlias("discoveredpotions");
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
		potion_stats.tryExecute(command, nick, target, event, copyOfRange);
		discovered.tryExecute(command, nick, target, event, copyOfRange);
	}

	public EffectEntry getPotionEffect(ArrayList<String> params, String user) {
		EffectEntry effectEntry;
		boolean is_potion = false;
		int color = -1;
		int consistency = -1;

		System.out.println("Params: " + params.toString());

		for (String param : params) {
			param = param.toLowerCase();
			if (color == -1 && ColorExists(param))
				color = GetColorIndexByName(param);
			if (consistency == -1 && consistencies.indexOf(param) != -1)
				consistency = consistencies.indexOf(param);
			if (param.replace(".", "").equals("potion"))
				is_potion = true;
		}

		System.out.println("ColorEntry: " + color);
		System.out.println("Consistency: " + consistency);
		System.out.println("Has_potion: " + is_potion);

		if (!is_potion || color == -1 || consistency == -1) {
			return null;
		}

		tryResetPotionList();

		if (combinationHasEffect(consistency, color)) {
			effectEntry = getCombinationEffect(consistency, color);
			System.out.println("Effect recorded for " + consistency + "," + color + ": " + effectEntry);
			return effectEntry;
		} else {
			int effect = Helper.getRandomInt(0, effects.size() - 1);
			System.out.println("No effect recorded for " + consistency + "," + color + ", Assign " + effect);

			String replace_color = getRandomColor().getName();
			String turn_color = getRandomColor().turnsTo();
			String replace_consistency = getRandomConsistency();

			String effectp = effects.get(effect)
                    .replace("{color}", replace_color)
                    .replace("{turn_color}", turn_color)
                    .replace("{consistency}", replace_consistency)
                    .replace("{transformation}", Helper.getRandomTransformation(true, false, false))
                    .replace("{transformation2}", Helper.getRandomTransformation(true, false, false))
                    .replace("{transformations}", Helper.getRandomTransformation(true, false, true))
                    .replace("{transformations2}", Helper.getRandomTransformation(true, false, true));
			try {
                Pattern pattern = Pattern.compile("\\{color:(.*):(p?)}");
                Matcher matcher = pattern.matcher(effectp);
                while (matcher.find()) {
                    String color_item = matcher.group(1);
                    boolean use_prefix = false;
                    if (matcher.group(2).equals("p"))
                        use_prefix = true;
                    effectp = effectp.replace(matcher.group(0), getRandomColor().colorItem(color_item, use_prefix));
                }
            } catch (Exception ex) {
			    ex.printStackTrace();
            }

			System.out.println("Effectp: " + effectp);
			effectEntry = new EffectEntry(effectp, user);
			setCombinationEffect(consistency, color, effectEntry);
			return effectEntry;
		}
	}

	private static void tryResetPotionList() {
		if (day_of_potioning.equals(DateTime.now().toString("yyyy-MM-dd"))) {
			resetPotionList();
		}
	}

	private static void resetPotionList() {
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

	private void setCombinationEffect(int consistency, int color, EffectEntry effect) {
		String key = consistency + "," + color;

		potions.put(key, effect);
	}

	private EffectEntry getCombinationEffect(int consistency, int color) {
		String key = consistency + "," + color;
		return potions.get(key);
	}

	public static String[] getRandomPotion() {
		int color = Helper.getRandomInt(0, colorEntries.size() - 1);
		int consistency = Helper.getRandomInt(0, consistencies.size() - 1);
		String col = colorEntries.get(color).getName();
		String con = consistencies.get(consistency);
		return new String[] { con, col, potions.containsKey(con + "," + col) ? "" : "new" };
	}

    static class PotionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

        	tryResetPotionList();

            String target = t.getRequestURI().toString();
            String response = "";

            int colorcount = colorEntries.size();
            int concount = consistencies.size();
            int combinations = colorcount * concount;
            int potioncount = potions.size();
            int effectcount = effects.size();
            float ratio = (float)effectcount / (float)combinations;
            DecimalFormat format = new DecimalFormat("#.###");
            String potionShelf = "<div>There are <b>" + colorcount + "</b> colorEntries and <b>" + concount + "</b> consistencies! That's <b>" + combinations + "</b> different potions! Out of these <b>" + potioncount + "</b> " + (potioncount == 1 ? "has" : "have") + " been discovered today.</div>" +
					"<div>There are <b>" + effectcount + "</b> effects. That's <b>" + format.format(ratio) + "</b> effect" + (ratio == 1 ? "" : "s") + " per potion.</div>" +
					"<table style='margin-top: 20px;'><tr><th>Potion</th><th>Effect</th><th>Discovered by</th></tr>";
            try {
                Iterator it = potions.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pair = (HashMap.Entry)it.next();
                    String[] potion = pair.getKey().toString().split(",");
                    String consistency = consistencies.get(Integer.parseInt(potion[0]));
                    String color = colorEntries.get(Integer.parseInt(potion[1])).getName();
					EffectEntry entry = (EffectEntry)pair.getValue();
					potionShelf += "<tr><td>" + consistency.substring(0,1).toUpperCase() + consistency.substring(1) + " " + color.substring(0, 1).toUpperCase() + color.substring(1) + " Potion</td><td>" + entry.Effect.replace("{user}", "User") + "</td><td>" + entry.Discoverer + "</td></tr>";
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            potionShelf += "</table>";
            List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");


            String navData = "";
            Iterator it = httpd.pages.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                navData += "<div class=\"innertube\"><h1><a href=\""+ pair.getValue() +"\">"+ pair.getKey() +"</a></h1></div>";
            }

            // convert String into InputStream
            InputStream is = new ByteArrayInputStream(html.getBytes());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    response = response + line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#POTIONS#", potionShelf)
                            .replace("#NAVIGATION#", navData)+"\n";
                }
            }
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static boolean ColorExists(String color) {
        for (ColorEntry c : colorEntries) {
            if (c != null && c.Name != null && c.Name.equals(color.toLowerCase()))
                return true;
        }
        return false;
    }

    private static ColorEntry FindColorByName(String color) {
	    for (ColorEntry c : colorEntries) {
	        if (c != null && c.Name != null && c.Name.equals(color.toLowerCase()))
	            return c;
        }
	    return null;
    }

    private static int GetColorIndexByName(String color) {
	    ColorEntry col = null;
        for (ColorEntry c : colorEntries) {
            if (c != null && c.Name != null && c.Name.equals(color.toLowerCase()))
                col = c;
        }
        return colorEntries.indexOf(col);
    }

    public static ColorEntry getRandomColor() {
	    int index = Helper.getRandomInt(0, colorEntries.size() - 1);
//	    System.out.println("Found color index " + index);
	    return colorEntries.get(index);
    }

    public static String getRandomConsistency() {
	    int index = Helper.getRandomInt(0, consistencies.size() - 1);
//	    System.out.println("Found consistency index " + index);
	    return consistencies.get(index);
    }
}

class EffectEntry {
	String Effect;
	String Discoverer;

	EffectEntry(String effect, String discoverer) {
		Effect = effect;
		Discoverer = discoverer;
	}

	@Override
	public String toString() {
		return Effect;
	}
}
