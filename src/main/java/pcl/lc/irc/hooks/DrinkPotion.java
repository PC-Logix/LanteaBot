package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Config;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.*;
import pcl.lc.utils.Exceptions.InvalidPotionException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

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
	private Command splash;
	public static ArrayList<AppearanceEntry> appearanceEntries = new ArrayList<>();
	public static ArrayList<AppearanceEntry> consistencies = new ArrayList<>();
	public static ArrayList<String> effects = new ArrayList<>();
	public static HashMap<String, EffectEntry> potions = new HashMap<>();
	public static ArrayList<String> limits = new ArrayList<>();
	public static String day_of_potioning = "";
	public static HashMap<String, ArrayList<EffectEntry>> specialFluids = new HashMap<>();

	public static int daysPotionsLast = 4;

	private static Boolean limitsEnabled = true;

	@Override
	protected void initHook() {
		initCommands();
        httpd.registerContext("/potions", new PotionHandler(), "Potions");
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(get_random);
		IRCBot.registerCommand(potion_stats);
		IRCBot.registerCommand(discovered);
		IRCBot.registerCommand(splash);

		appearanceEntries.add(new AppearanceEntry("blue", "a"));
		appearanceEntries.add(new AppearanceEntry("red", "a"));
		appearanceEntries.add(new AppearanceEntry("röd", "a"));
		appearanceEntries.add(new AppearanceEntry("rød", "a"));
		appearanceEntries.add(new AppearanceEntry("yellow", "a"));
		appearanceEntries.add(new AppearanceEntry("purple", "a"));
		appearanceEntries.add(new AppearanceEntry("green", "a"));
		appearanceEntries.add(new AppearanceEntry("cyan", "a"));
		appearanceEntries.add(new AppearanceEntry("tan", "a"));
		appearanceEntries.add(new AppearanceEntry("black", "a"));
		appearanceEntries.add(new AppearanceEntry("white", "a"));
		appearanceEntries.add(new AppearanceEntry("pink", "a"));
		appearanceEntries.add(new AppearanceEntry("metal", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("copper", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("iron", "an", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("stainless steel", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("aluminium", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("titanium", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("adamantium", "an", "{appearance} colored {item}", "the color of {appearance}")); // Marvel universe
		appearanceEntries.add(new AppearanceEntry("unobtanium", "an", "{appearance} colored {item}", "the color of {appearance}")); // Avatar
		appearanceEntries.add(new AppearanceEntry("tiberium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Command & Conquer
		appearanceEntries.add(new AppearanceEntry("caterium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Satisfactory
		appearanceEntries.add(new AppearanceEntry("aether", "an", "{appearance} colored {item}", "the color of {appearance}")); // Magic: The Gathering
		appearanceEntries.add(new AppearanceEntry("bavarium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Just Cause 3
		appearanceEntries.add(new AppearanceEntry("bombastium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Disney
		appearanceEntries.add(new AppearanceEntry("dalekanium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Doctor Who
		appearanceEntries.add(new AppearanceEntry("dilithium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Star Trek
		appearanceEntries.add(new AppearanceEntry("jumbonium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Futurama
		appearanceEntries.add(new AppearanceEntry("naqahdah", "a", "{appearance} colored {item}", "the color of {appearance}")); // Stargate
		appearanceEntries.add(new AppearanceEntry("octiron", "an", "{appearance} colored {item}", "the color of {appearance}")); // Discworld
		appearanceEntries.add(new AppearanceEntry("redstone", "a", "{appearance} colored {item}", "the color of {appearance}")); // Minecraft
		appearanceEntries.add(new AppearanceEntry("ruby", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("emerald", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("sapphire", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("amethyst", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("diamond", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("spice", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("radiation", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("gold", "a", "{appearance} colored {item}"));
		appearanceEntries.add(new AppearanceEntry("silver", "a", "{appearance} colored {item}"));
		appearanceEntries.add(new AppearanceEntry("tomato", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("lime", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("citrus", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("strawberry", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("chocolate", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("orange", "an"));
		appearanceEntries.add(new AppearanceEntry("tuna", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("salmon", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("rainbow", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("void", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("ocean", "an", "{appearance} colored {item}", "the color of the {appearance}"));
		appearanceEntries.add(new AppearanceEntry("grass", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("sky", "a", "{appearance} colored {item}", "the color of the {appearance}"));
		appearanceEntries.add(new AppearanceEntry("rock", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("aqua", "an", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("dirt", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("quicksilver", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("coral", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("transparent", "a", "{appearance} {item}", "{appearance}"));
		appearanceEntries.add(new AppearanceEntry("water", "a", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("weather", "a"));
		appearanceEntries.add(new AppearanceEntry("aegisalt", "an", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("cerulium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("ferozium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("moonstone", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("rubium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("solarium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("violium", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("automato", "an", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("avesmingo", "an", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("boneboo", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("coralcreep", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("currentcorn", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("oculemon", "an", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("pearlpeas", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("pussplum", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("toxictop", "a", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("violet", "a"));
		appearanceEntries.add(new AppearanceEntry("crimson", "a"));
		appearanceEntries.add(new AppearanceEntry("grathnode ", "a", "{appearance} colored {item}", "the color of {appearance}")); // Ar Tonelico

		consistencies.add(new AppearanceEntry("viscous", "a"));
		consistencies.add(new AppearanceEntry("cloudy", "a"));
		consistencies.add(new AppearanceEntry("fluffy", "a"));
		consistencies.add(new AppearanceEntry("thick", "a"));
		consistencies.add(new AppearanceEntry("smelly", "a"));
		consistencies.add(new AppearanceEntry("fragrant", "a"));
		consistencies.add(new AppearanceEntry("light", "a"));
		consistencies.add(new AppearanceEntry("shiny", "a"));
		consistencies.add(new AppearanceEntry("porous", "a"));
		consistencies.add(new AppearanceEntry("ripe", "a"));
		consistencies.add(new AppearanceEntry("muddy", "a"));
		consistencies.add(new AppearanceEntry("shimmering", "a"));
		consistencies.add(new AppearanceEntry("gloomy", "a"));
		consistencies.add(new AppearanceEntry("prickly", "a"));
		consistencies.add(new AppearanceEntry("sour", "a"));
		consistencies.add(new AppearanceEntry("salty", "a"));
		consistencies.add(new AppearanceEntry("sweet", "a"));
		consistencies.add(new AppearanceEntry("runny", "a"));
		consistencies.add(new AppearanceEntry("boiling", "a"));
		consistencies.add(new AppearanceEntry("freezing", "a"));
		consistencies.add(new AppearanceEntry("sedimented", "a"));
		consistencies.add(new AppearanceEntry("warpy", "a"));
		consistencies.add(new AppearanceEntry("basic", "a"));
		consistencies.add(new AppearanceEntry("stirring", "a"));
		consistencies.add(new AppearanceEntry("bubbly", "a"));
		consistencies.add(new AppearanceEntry("gloopy", "a"));
		consistencies.add(new AppearanceEntry("goopy", "a"));
		consistencies.add(new AppearanceEntry("slimy", "a"));
		consistencies.add(new AppearanceEntry("solid", "a"));
		consistencies.add(new AppearanceEntry("molten", "a"));
		consistencies.add(new AppearanceEntry("fiery", "a"));
		consistencies.add(new AppearanceEntry("dull", "a"));
		consistencies.add(new AppearanceEntry("resonating", "a"));
		consistencies.add(new AppearanceEntry("shining", "a"));
		consistencies.add(new AppearanceEntry("seeping", "a"));
		consistencies.add(new AppearanceEntry("smooth", "a"));
		consistencies.add(new AppearanceEntry("soft", "a"));
		consistencies.add(new AppearanceEntry("oxidised", "an"));
		consistencies.add(new AppearanceEntry("mutable", "a"));
		consistencies.add(new AppearanceEntry("liquid", "a"));
		consistencies.add(new AppearanceEntry("smelly", "a"));

		//Valid tags: {user},{appearance},{appearance_p},{turn_appearance},{appearance:<item>:p},{consistency},{consistency_p},{transformation},{transformation2},{transformations},{transformations2},{limit}
		// {r:[min]-{max]:[unit]} - Produces a random int within the range specified suffixed by the specified unit
		effects.add("{user} looks confused as nothing happens.");
		effects.add("{user} turns into {transformation_p} girl{limit}.");
		effects.add("{user} turns into {transformation_p} boy{limit}.");
		effects.add("{user} turns into {transformation_p}{limit}.");
		effects.add("{user} turns into {appearance_p} {transformation} girl{limit}.");
		effects.add("{user} turns into {appearance_p} {transformation} boy{limit}.");
		effects.add("{user} turns into {appearance_p} {transformation}{limit}.");
		effects.add("{user} turns into {transformation_p} {transformation2}{limit}.");
		effects.add("{user} turns into {transformation_p} {transformation2} girl{limit}.");
		effects.add("{user} turns into {transformation_p} {transformation2} boy{limit}.");
		effects.add("{user} turns into {appearance_p} {transformation} {transformation2}{limit}.");
		effects.add("{user} turns into {appearance_p} {transformation} {transformation2} girl{limit}.");
		effects.add("{user} turns into {appearance_p} {transformation} {transformation2} boy{limit}.");
		effects.add("{user}'s hair turn {turn_appearance}{limit}.");
		effects.add("{user}'s hair glows {turn_appearance}{limit}.");
		effects.add("{user}'s skin turn {turn_appearance}{limit}.");
		effects.add("{user}'s eyes turn {turn_appearance}{limit}.");
		effects.add("{user}'s nails turn {turn_appearance}{limit}.");
		effects.add("{user}'s bones turn {turn_appearance}{limit}.");
		effects.add("{user}'s clothes turn {turn_appearance}{limit}.");
		effects.add("{user}'s eyes glow {turn_appearance}{limit}.");
		effects.add("{user}'s skin turn {turn_appearance} but with a {appearance} glow{limit}.");
		effects.add("{user}'s toes turn invisible{limit}.");
		effects.add("{user}'s hair grows {r:2-4:time} longer{limit}.");
		effects.add("{user} gains the proportional strength of a {transformation}{limit}.");
		effects.add("{user} now knows how not to be seen.");
		effects.add("{user} gains knowledge about a random useless subject.");
		effects.add("{user} gains an extra strand of hair on their face{limit}.");
		effects.add("{user} grows whiskers{limit}.");
		effects.add("{user} grows a tail from a {transformation}{limit}.");
		effects.add("{user} shrinks by a negligible amount{limit}.");
		effects.add("{user} grows slightly{limit}.");
		effects.add("{user} suddenly craves pie.");
		effects.add("{user} gains the ability to talk to bricks{limit}.");
		effects.add("{user} feels a strong urge to recycle the potion bottle.");
		effects.add("{user}'s bed is suddenly slightly less comfortable{limit}.");
		effects.add("{user} gains a negligible amount of luck.");
		effects.add("{user} realizes this was actually a {consistency} {appearance} potion.");
		effects.add("{user} remembers an important appointment.");
		effects.add("An incredibly fake looking mustache is stuck to {user}'s face{limit}.");
		effects.add("{user} has a sudden desire to run around in a circle{limit}.");
		effects.add("{user} gains the ability to summon safety pins{limit}.");
		effects.add("{user} feels slightly stronger."); // gain 1 point of strength
		effects.add("{user} feels slightly more agile."); // gain 1 point of agility
		effects.add("{user} feels slightly faster."); // gain 1 point of speed
		effects.add("{user} recovers some mana.");
		effects.add("{user} feels slightly weaker.");
		effects.add("{user} feels slightly less agile.");
		effects.add("{user} feels slightly slower.");
		effects.add("{user} gains an additional bone.");
		effects.add("{user} is suddenly more aware of cute things nearby{limit}.");
		effects.add("{user} loses exactly a handful of luck.");
		effects.add("{user}'s pockets suddenly contain a number of {appearance:marbles:}.");
		effects.add("{user}'s favourite hat is suddenly on fire.");
		effects.add("{user} has a single tear roll down their cheek for some reason.");
		effects.add("{user}'s nose vanish{limit}.");
		effects.add("{user} feels like a champion!");
		effects.add("{user} feels a sudden surge of static electricity.");
		effects.add("{user}'s shoes are now slightly too large{limit}.");
		effects.add("{user} is now Borg{limit}.");
		effects.add("{user} has no memory of drinking a potion.");
		effects.add("{user} knows the exact location of a particular molecule of oxygen{limit}.");
		effects.add("{user} thinks the empty bottle is a snake{limit}.");
		effects.add("{user} gets an urge to have another potion.");
		effects.add("It tastes sweet.");
		effects.add("It tastes sour.");
		effects.add("It tastes bitter.");
		effects.add("It tastes salty.");
		effects.add("{user} feels like one particular wasp has it out for them suddenly.");
		effects.add("{user} zones out for {r:1-10:minute}.");
		effects.add("A warpzone opens up next to {user}. (Use " + Config.commandprefix + "warp)");
		effects.add("After the first sip the potion poofs away.");
		effects.add("{user} looks up and sees the moon smile at them for a second.");
		effects.add("The ghost of a plant haunts you{limit}.");
		effects.add("{r:2-5:} nearby pebbles suddenly shift slightly in {user}'s direction.");
		effects.add("The next pie {user} eats tastes slightly less good.");
		effects.add("Sitting down suddenly seems like a really terrible idea.");
		effects.add("The next fork {user} touches tells them it's most deeply guarded secret.");
		effects.add("A voice whispers into {user}'s ear \"Drink or be drunk\" as it fades away as they drink the potion.");
		effects.add("{user} briefly feel like they have just stepped out of a car.");
		effects.add("True enlightenment can be achieved by drinking another potion.");
		effects.add("For about a second {user} knows the location of a great treasure.");
		effects.add("The potion was inside you all along.");
		effects.add("{user} is suddenly wearings gloves they don't remember putting on.");
		effects.add("A sudden craving for soup occupies {user}'s thoughts{limit}.");
		effects.add("{user} suddenly forgets a random piece of trivia.");
		effects.add("A {transformation} flies past that vaguely resembles someone {user} knows.");
		effects.add("{user} reboots for an update for {r:1-10:minute}.");
		effects.add("Dramatic music briefly plays in the distance.");
		effects.add("{user} has a feeling that their face just appeared on a random vegetable somewhere.");
		effects.add("The potion bottle is suddenly on fire!");
		effects.add("Once empty the potion bottle fills with a different potion.");
		effects.add("{user} gains the ability to talk to {transformations}{limit}.");
		effects.add("{user} sees the sky briefly flash solid dark blue then go back to normal.");
		effects.add("When {user} drinks the last drop, a bucket of water materializes above their head and dumps it contents over them, then vanishes. The water does not.");
		effects.add("Suddenly there's a swarm of wasps behind {user} that chase them for {r:30-60:second}!");
		effects.add("When {user} brings the bottle down they see {appearance:plastic flamingo:p}. It stares into their soul.");
		effects.add("A bard starts playing a lute behind {user}. They don't stop.");
		effects.add("A bard starts playing a lute behind {user}{limit}.");
		effects.add("The bottle turns into a sword.");
		effects.add("The bottle turns into an axe.");
		effects.add("The bottle turns into a spear.");
		effects.add("The bottle turns into a dagger.");
		effects.add("The bottle turns into a bow.");
		effects.add("The bottle turns into a trident.");
		effects.add("The bottle turns into a sling.");
		effects.add("A genie appears out of the empty bottle, turns it into a pie, then vanishes.");
		effects.add("{user} thinks \"What if, like, *we* are the potions man?\". This makes no sense whatsoever.");
		effects.add("After drinking the potion {user} notices a label that says \"Side effects may include giggle fits and excessive monologuing.\"");
		effects.add("{user} forgets the location of a great treasure.");
		effects.add("Oh no, {user} got a health potion, there's probably a boss fight coming!");
		effects.add("There's an acidic tinge to the potion... A label on the bottle reads \"Who needs internal organs anyway?\"");
		effects.add("{user} feels much better!");
		effects.add("A tiny cloud appears with a ridiculous smile on it. It follows {user}{limit}.");
		effects.add("The potion contained a computer virus! But {user}'s anti-virus routines destroy it.");
		effects.add("The potion contained a computer virus! It just changed {user}'s background...");
		effects.add("The bottle splits into two revealing a smaller {consistency} {appearance} potion.");
		effects.add("A tiny genie appears, gives {user} a thumbs up, and poofs away.");
		effects.add("{user} feels chill.");
		effects.add("{user} feels the need to smash.");
		effects.add("{user} feels the need to use \"" + Config.commandprefix + "shell\".");
		effects.add("{user} feels the need to use \"" + Config.commandprefix + "fling\".");
		effects.add("The bottle turns into a pie.");
		effects.add("The bottle turns into a piece of bacon.");
		effects.add("The bottle turns into an apple.");
		effects.add("A bard behind {user} suddenly stops playing. They were most likely eaten by a monster.");
		effects.add("Gravity reverses for {user}{limit}.");
		effects.add("{user} now has a mullet{limit}.");
		effects.add("The next remote {user} looks for is extra hard to find.");
		effects.add("{user} gets a sudden Spice infusion. {user} can see the universe. [Spice Addiction +1]");
		effects.add("{user}'s radiation level goes up by 2{limit}.");
		effects.add("{user} smells something burning.");
		effects.add("The sun turns into a giant baby face for a second. It's horrific.");
		effects.add("Everything {user} says is now in Comic Sans{limit}.");
		effects.add("Everything {user} says is now in Wingdings{limit}.");
		effects.add("{user}'s favourite cup is now upside down.");
		effects.add("The potion bottle insults {user}'s haircut.");
		effects.add("After drinking the potion you realize the bottle has your face on it.");
		effects.add("Wheels are briefly square.");
		effects.add("{user} hears a train whistle in the distance.");
		effects.add("The next glass of water {user} has tastes like water.");

		//Never end with punctuation
		limits.add(" for {r:1-60:second}");
		limits.add(" for {r:2-60:minute}");
		limits.add(" for {r:2-5:hour}");
		limits.add(" until their next sip of water");
		limits.add(" until the next time they hug someone");
		limits.add(" until they say the word \"Blatherskite\"");
		limits.add(" until they leave the computer");
		limits.add(" until they see a bird");
		limits.add(" for {r:1-10:moon}");
		limits.add(" until Sozin's Comet returns");
		limits.add(" until they see a unicorn");
		limits.add(" until they find true love");
		limits.add(" until they see a star fall");
		limits.add(" until they eat a pie");

		//Valid tags: {user},{appearance},{appearance_p},{turn_appearance},{appearance:<item>:p},{consistency},{consistency_p},{transformation},{transformation2},{transformations},{transformations2},{limit}
		specialFluids.put("water", new ArrayList<>(Arrays.asList(
				new EffectEntry("You drink some water. Wait... this isn't water... it's {consistency_p} {appearance} potion!"),
				new EffectEntry("You splash {target} with some water. Wait... this isn't water... it's {consistency_p} {appearance} potion!"))));
		specialFluids.put("soda", new ArrayList<>(Arrays.asList(
				new EffectEntry("You have some soda. It's fizzy and sweet."),
				new EffectEntry("You splash {target} with some soda. It's fizzy and sticky."))));
		specialFluids.put("coffee", new ArrayList<>(Arrays.asList(
				new EffectEntry("You have some coffee. It's hot and bitter."),
				new EffectEntry("You splash {target} with coffee. It's scalding hot! {target} takes 1d6 damage!"))));
		specialFluids.put("everything", new ArrayList<>(Arrays.asList(
				new EffectEntry("{user} explodes!"),
				new EffectEntry("You fail to lift the container containing all the potions. It's too heavy."))));
		specialFluids.put("antidote", new ArrayList<>(Arrays.asList(
				new EffectEntry("{user} reverts to their original state before drinking any potions."),
				new EffectEntry("You splash {target} with some antidote. {target} reverts to their original state before drinking any potions."))));
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
				PotionEntry potion;
			    try {
			    	if (params.size() == 0 || params.get(0).equals("random")) {
						potion = PotionHelper.getRandomPotion();
						Helper.sendMessage(target, "You drink " + potion.consistency.getName(true) + " " + potion.appearance.getName() + " potion" + (potion.isNew ? " (New!)" : "") + ". " + potion.getEffect(nick).toString().replace("{user}", nick));
						return;
					} else if (params.get(0).equals("^")) {
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
					} else {
			    		if (specialFluids.containsKey(params.get(0))) {
			    			Helper.sendMessage(target, PotionHelper.replaceParamsInEffectString(specialFluids.get(params.get(0)).get(0).toString(), nick, ""));
			    			return;
						}
					}

			    	try {
			    		potion = new PotionEntry();
			    		potion.setFromCommandParameters(params);

                    	EffectEntry effect = potion.getEffect(nick);

                    	if (effect != null)
							Helper.sendMessage(target, (potion.isNew ? "(New!) " : "") + effect.toString().replace("{user}", nick));
                    	else
                    		Helper.sendMessage(target, "Due to some series of events I couldn't find any effect for this potion.");
			    	} catch (InvalidPotionException ex) {
						Helper.sendMessage(target, "This doesn't seem to be a potion I recognize... Make sure it has an appearance and consistency keyword, and the word \"potion\" in it.");
					}
                } catch (Exception ex) {
			        ex.printStackTrace();
                }
			}
		};
		local_command.setHelpText("Drink a potion with a certain consistency and appearance and something might happen.");
		local_command.registerAlias("chug");
		local_command.registerAlias("toast");
		local_command.registerAlias("sip");
		local_command.registerAlias("ingest");
		local_command.registerAlias("consume");
		local_command.registerAlias("use");
		local_command.registerAlias("absorb");
		local_command.registerAlias("engross");
		local_command.registerAlias("quaff");
		local_command.registerAlias("skull");
		local_command.registerAlias("down");
		local_command.registerAlias("slurp");

		splash = new Command("splash", 10) {
            @Override
            public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
                try {
                    if (params.length() == 0) {
                        Helper.sendMessage(target, "Try " + Config.commandprefix + this.getCommand() + " <target>[ with <potion>]");
                    } else {
                    	String[] split = params.split("with");

                    	String splashTarget;
                    	String potionString = null;
                    	if (split.length == 1) {
							splashTarget = params;
						} else {
                    		splashTarget = split[0].trim();
                    		potionString = split[1].trim();
						}

                        PotionEntry potion;
                        if (potionString == null || potionString.equals("random")) {
                            potion = PotionHelper.getRandomPotion();
                            EffectEntry effect = potion.getEffect(nick);
                            Helper.sendMessage(target, "You fling " + potion.consistency.getName(true) + " " + potion.appearance.getName() + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + effect.toString().replace("{user}", splashTarget));
                            return;
						} else if (potionString.equals("^")) {
							List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
							for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
								if (entry.getValue().get(0).equals(target)) {
									System.out.println(entry.getValue().get(2));
									if (entry.getValue().get(2).toLowerCase().contains("potion")) {
										potionString = entry.getValue().get(2);
										break;
									}
								}
							}
						}

						if (specialFluids.containsKey(potionString)) {
							Helper.sendMessage(target, PotionHelper.replaceParamsInEffectString(specialFluids.get(potionString).get(1).toString(), nick, splashTarget));
							return;
						}

                        try {
							potion = new PotionEntry();
							potion.setFromCommandParameters(potionString);

							EffectEntry effect = potion.getEffect(nick);

							if (effect != null)
								Helper.sendMessage(target, "You fling " + potion.consistency.getName(true) + " " + potion.appearance.getName() + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + effect.toString().replace("{user}", splashTarget));
						} catch (InvalidPotionException ex) {
                        	Helper.sendMessage(target, "This doesn't seem to be a potion I recognize... Make sure it has an appearance and consistency keyword, and the word \"potion\" in it.");
						}
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

		get_random = new Command("randompotion", 10) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
			    PotionEntry potion = PotionHelper.getRandomPotion();
				Helper.sendMessage(target, "You get " + potion.consistency.getName(true) + " " + potion.appearance.getName() + " potion" + (potion.isNew ? " (New!)" : ""), nick);
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
					int apperance_count = appearanceEntries.size();
					int consistencies_count = consistencies.size();
					int effect_count = effects.size();
					int combination_count = apperance_count * consistencies_count;
					Helper.sendMessage(target, "There are " + apperance_count + " appearanceEntries, " + consistencies_count + " consistencies! That's " + combination_count + " potion combinations! There are " + effect_count + " effects!");
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
		potion_stats.registerAlias("potions");
		potion_stats.registerAlias("potionshelf");
		potion_stats.registerAlias("potionlist");
		potion_stats.registerAlias("listpotions");
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
		splash.tryExecute(command, nick, target, event, copyOfRange);
	}

    static class PotionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

        	PotionHelper.tryResetPotionList();

            String target = t.getRequestURI().toString();
            String response = "";

            int appearancecount = appearanceEntries.size();
            int concount = consistencies.size();
            int combinations = appearancecount * concount;
            int potioncount = potions.size();
            int effectcount = effects.size();
            float ratio = (float)effectcount / (float)combinations;
            DecimalFormat format = new DecimalFormat("#.###");
            ArrayList<String> unique_effects_discovered = new ArrayList<>();

			for (Map.Entry<String, EffectEntry> stringEffectEntryEntry : potions.entrySet()) {
				String[] potion = stringEffectEntryEntry.getKey().split(",");
				if (!unique_effects_discovered.contains(potion[1]))
					unique_effects_discovered.add(potion[1]);
			}
            int unique_effect_count = unique_effects_discovered.size();
            String potionShelf = "<div>There are <b>" + appearancecount + "</b> appearances and <b>" + concount + "</b> consistencies! That's <b>" + combinations + "</b> different potions! Out of these <b>" + potioncount + "</b> " + (potioncount == 1 ? "has" : "have") + " been discovered today.</div>" +
					"<div>There are <b>" + effectcount + "</b> effects. That's <b>" + format.format(ratio) + "</b> effect" + (ratio == 1 ? "" : "s") + " per potion. <b>" + unique_effect_count + "</b> unique effect" + (unique_effect_count == 1 ? "" : "s") + " " + (unique_effect_count == 1 ? "has" : "have") + " been discovered today.</div>" +
					"<div style='margin-top: 6px;'>A valid potion string (for use with <b>" + Config.commandprefix + "drink</b>) needs an appearance keyword, consistency keyword and the word \"<b>potion</b>\" in it.</div>" +
					"<table style='margin-top: 20px;'><tr><th>Potion</th><th>Effect</th><th>Discovered by</th></tr>";
            try {
				for (Map.Entry<String, EffectEntry> stringEffectEntryEntry : potions.entrySet()) {
					String[] potion = stringEffectEntryEntry.getKey().split(",");
					String consistency = consistencies.get(Integer.parseInt(potion[0])).getName();
					String appearance = appearanceEntries.get(Integer.parseInt(potion[1])).getName();
					EffectEntry entry = stringEffectEntryEntry.getValue();
					potionShelf += "<tr><td>" + consistency.substring(0, 1).toUpperCase() + consistency.substring(1) + " " + appearance.substring(0, 1).toUpperCase() + appearance.substring(1) + " Potion</td><td>" + entry.Effect.replace("{user}", "User") + "</td><td>" + entry.Discoverer + "</td></tr>";
				}
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            potionShelf += "</table>";
            List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(),"utf-8");

			String entries = "";
			for (AppearanceEntry appearanceEntry : appearanceEntries) {
				String name = appearanceEntry.getName();
				entries += "<div>" + name.substring(0, 1).toUpperCase() + name.substring(1) + "</div>";
			}
			potionShelf += "<div style='margin-top: 10px;'>" + MakeJavascriptContainer("Show/hide appearances (" + appearanceEntries.size() + ")", entries) + "</div>";

			entries = "";
			for (AppearanceEntry consistancy : consistencies) {
				entries += "<div>" + consistancy.getName().substring(0, 1).toUpperCase() + consistancy.getName().substring(1) + "</div>";
			}
			potionShelf += "<div style='margin-top: 10px;'>" + MakeJavascriptContainer("Show/hide consistencies (" + consistencies.size() + ")", entries) + "</div>";

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

    private static boolean AppearanceExists(String appearance) {
        for (AppearanceEntry c : appearanceEntries) {
            if (c != null && c.Name != null && c.Name.equals(appearance.toLowerCase()))
                return true;
        }
        return false;
    }

    private static AppearanceEntry FindAppearanceByName(String appearance) {
	    for (AppearanceEntry c : appearanceEntries) {
	        if (c != null && c.Name != null && c.Name.equals(appearance.toLowerCase()))
	            return c;
        }
	    return null;
    }

    private static int GetAppearanceIndexByName(String appearance) {
	    AppearanceEntry col = null;
        for (AppearanceEntry c : appearanceEntries) {
            if (c != null && c.Name != null && c.Name.equals(appearance.toLowerCase()))
                col = c;
        }
        return appearanceEntries.indexOf(col);
    }

	private static String MakeJavascriptContainer(String label, String contents) {
		return "<div><span style='cursor: pointer;' onclick='if (this.parentElement.children[1].style.display != \"none\") { this.parentElement.children[1].style.display = \"none\"; } else { this.parentElement.children[1].style.display = null; }'>" + label + "</span><div style='display: none;'>" + contents + "</div></div>";
	}
}

