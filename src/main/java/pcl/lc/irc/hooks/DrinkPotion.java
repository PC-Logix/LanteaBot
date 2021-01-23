package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gcardone.junidecode.App;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.joda.time.DateTime;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.httpd.httpd;
import pcl.lc.irc.*;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.utils.*;
import pcl.lc.utils.Exceptions.InvalidPotionException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

/**
 * @author Forecaster
 */
@SuppressWarnings("rawtypes")
public class DrinkPotion extends AbstractListener {
	private Command local_command;
	private Command get_random;
	private Command potion_stats;
	private Command discovered;
	private Command splash;
	private Command potion_lookup;
	public static HashMap<String, AppearanceEntry> appearanceEntries = new HashMap<>();
	public static HashMap<String, AppearanceEntry> consistencyEntries = new HashMap<>();
	public static ArrayList<EffectEntry> effects = new ArrayList<>();
	public static HashMap<String, EffectEntry> potions = new HashMap<>(); //consistency,appearance
	public static ArrayList<String> limits = new ArrayList<>();
	public static HashMap<String, EffectEntry> specialFluids = new HashMap<>();

	public static HashMap<String, Integer> curseMap = new HashMap<>();
	public static HashMap<String, Integer> researchPointsMap = new HashMap<>();
	public static HashMap<String, Integer> baconMap = new HashMap<>();
	public static HashMap<String, Integer> radiationMap = new HashMap<>();

	public CommandRateLimit rateLimit;

	public static int daysPotionsLast = 4;
	public static String day_of_potioning = DateTime.now().plusDays(DrinkPotion.daysPotionsLast).toString("yyyy-MM-dd");

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
		IRCBot.registerCommand(potion_lookup);

		PotionHelper.addAppearanceEntry(new AppearanceEntry("Blue", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Red", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Röd", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Rød", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Yellow", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Purple", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Green", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Cyan", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Tan", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Black", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("White", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Pink", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Gold", "A", "{appearance} colored {item}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Silver", "A", "{appearance} colored {item}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Copper", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Iron", "An", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Stainless steel", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Aluminium", "An", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Titanium", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Platinum", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Electrum", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Mithril", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Adamantium", "An", "{appearance} colored {item}", "the color of {appearance}")); // Marvel universe
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Unobtanium", "An", "{appearance} colored {item}", "the color of {appearance}")); // Avatar
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Tiberium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Command & Conquer
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Caterium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Satisfactory
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Aether", "An", "{appearance} colored {item}", "the color of {appearance}")); // Magic: The Gathering
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Bavarium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Just Cause 3
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Bombastium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Disney
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Dalekanium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Doctor Who
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Dilithium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Star Trek
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Jumbonium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Futurama
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Naqahdah", "A", "{appearance} colored {item}", "the color of {appearance}")); // Stargate
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Octiron", "An", "{appearance} colored {item}", "the color of {appearance}")); // Discworld
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Redstone", "A", "{appearance} colored {item}", "the color of {appearance}")); // Minecraft
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Ruby", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Emerald", "An", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Sapphire", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Amethyst", "An", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Diamond", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Spice", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Radiation", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Tomato", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Lime", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Citrus", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Strawberry", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Chocolate", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Orange", "An"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Tuna", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Salmon", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Rainbow", "A", "{appearance} colored {item}", "the color of the {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Void", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Ocean", "An", "{appearance} colored {item}", "the color of the {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Grass", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Sky", "A", "{appearance} colored {item}", "the color of the {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Rock", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Aqua", "An", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Dirt", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Quicksilver", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Coral", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Transparent", "A", "{appearance} {item}", "{appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Water", "A", "{appearance} colored {item}", "the color of {appearance}"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Weather", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Aegisalt", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Cerulium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Ferozium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Moonstone", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Rubium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Solarium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Violium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Automato", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Avesmingo", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Boneboo", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Coralcreep", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Currentcorn", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Oculemon", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Pearlpeas", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Pussplum", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Toxictop", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Violet", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Crimson", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Grathnode", "A", "{appearance} colored {item}", "the color of {appearance}")); // Ar Tonelico
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Nectar", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Honey", "A"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Apple", "An"));
		PotionHelper.addAppearanceEntry(new AppearanceEntry("Pear", "A"));
		System.out.println("Registered " + appearanceEntries.size() + " appearance entries!");

		PotionHelper.addConsistencyEntry(new AppearanceEntry("Viscous", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Cloudy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Fluffy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Thick", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Smelly", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Fragrant", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Light", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Shiny", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Porous", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Ripe", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Muddy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Shimmering", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Gloomy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Prickly", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Sour", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Salty", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Sweet", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Runny", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Boiling", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Freezing", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Sedimented", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Warpy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Basic", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Stirring", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Bubbly", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Gloopy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Goopy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Slimy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Solid", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Molten", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Fiery", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Dull", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Resonating", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Shining", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Seeping", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Smooth", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Soft", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Oxidised", "An"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Mutable", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Liquid", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Smelly", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Powdery", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Dusty", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Diluted", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Concentrated", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Invisible", "An"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Simulated", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Forked", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Spooned", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Wild", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Still", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Silent", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Woolly", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Rather", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Hairy", "A"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Eroded", "An"));
		PotionHelper.addConsistencyEntry(new AppearanceEntry("Tiny", "A"));
		System.out.println("Registered " + consistencyEntries.size() + "consistencies!");

		//See `PotionHelper.DynaParam` for tag list and descriptions.
		effects.add(new EffectEntry("{user} looks confused as nothing happens."));
		effects.add(new EffectEntry("{user} turns into {transformation_pc} girl{limit}."));
		effects.add(new EffectEntry("{user} turns into {transformation_pc} boy{limit}."));
		effects.add(new EffectEntry("{user} turns into {transformation_p}{limit}."));
		effects.add(new EffectEntry("{user} turns into {appearance_p_lc} {transformation} girl{limit}."));
		effects.add(new EffectEntry("{user} turns into {appearance_p_lc} {transformation} boy{limit}."));
		effects.add(new EffectEntry("{user} turns into {appearance_p_lc} {transformation}{limit}."));
		effects.add(new EffectEntry("{user} turns into {transformation_p} {transformation2}{limit}."));
		effects.add(new EffectEntry("{user} turns into {transformation_p} {transformation2} girl{limit}."));
		effects.add(new EffectEntry("{user} turns into {transformation_p} {transformation2} boy{limit}."));
		effects.add(new EffectEntry("{user} turns into {appearance_p_lc} {transformation} {transformation2}{limit}."));
		effects.add(new EffectEntry("{user} turns into {appearance_p_lc} {transformation} {transformation2} girl{limit}."));
		effects.add(new EffectEntry("{user} turns into {appearance_p_lc} {transformation} {transformation2} boy{limit}."));
		effects.add(new EffectEntry("{user}'s hair turn {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s hair glows {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s skin turn {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s eyes turn {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s nails turn {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s bones turn {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s clothes turn {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s eyes glow {turn_appearance_lc}{limit}."));
		effects.add(new EffectEntry("{user}'s skin turn {turn_appearance_lc} but with a {appearance_lc} glow{limit}."));
		effects.add(new EffectEntry("{user}'s toes turn invisible{limit}."));
		effects.add(new EffectEntry("{user}'s hair grows {r:2-4:time} longer{limit}."));
		effects.add(new EffectEntry("{user} gains the proportional strength of a {transformation}{limit}."));
		effects.add(new EffectEntry("{user} now knows how not to be seen."));
		effects.add(new EffectEntry("{user} gains knowledge about a random useless subject."));
		effects.add(new EffectEntry("{user} grows whiskers{limit}."));
		effects.add(new EffectEntry("{user} grows a tail from a {transformation}{limit}."));
		effects.add(new EffectEntry("{user} shrinks by a negligible amount{limit}."));
		effects.add(new EffectEntry("{user} grows slightly{limit}."));
		effects.add(new EffectEntry("{user} suddenly craves pie."));
		effects.add(new EffectEntry("{user} gains the ability to talk to bricks{limit}."));
		effects.add(new EffectEntry("{user} feels a strong urge to recycle the potion bottle.",
				"{user} feels like they should clean up the broken bottle."));
		effects.add(new EffectEntry("{user}'s bed is suddenly slightly less comfortable{limit}."));
		effects.add(new EffectEntry("{user} gains a negligible amount of luck."));
		effects.add(new EffectEntry("{user} realizes this was actually {consistency_p} {appearance} potion.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				rateLimit.reset(parameters.triggererName);
				return null;
			}
		}));
		effects.add(new EffectEntry("{user} remembers an important appointment."));
		effects.add(new EffectEntry("An incredibly fake looking mustache is stuck to {user}'s face{limit}."));
		effects.add(new EffectEntry("{user} has a sudden desire to run around in a circle{limit}."));
		effects.add(new EffectEntry("{user} gains the ability to summon safety pins{limit}."));
		effects.add(new EffectEntry("{user} feels slightly stronger.")); // gain 1 point of strength
		effects.add(new EffectEntry("{user} feels slightly more agile.")); // gain 1 point of agility
		effects.add(new EffectEntry("{user} feels slightly faster.")); // gain 1 point of speed
		effects.add(new EffectEntry("{user} recovers some mana."));
		effects.add(new EffectEntry("{user} feels slightly weaker.")); // lose 1 point of strength
		effects.add(new EffectEntry("{user} feels slightly less agile.")); // lose 1 point of agility
		effects.add(new EffectEntry("{user} feels slightly slower.")); // lose 1 point of speed
		effects.add(new EffectEntry("{user} gains an additional bone."));
		effects.add(new EffectEntry("{user} is suddenly more aware of cute things nearby{limit}."));
		effects.add(new EffectEntry("{user} loses exactly a handful of luck."));
		effects.add(new EffectEntry("{user}'s pockets suddenly contain 1d10 {appearance:marbles:}."));
		effects.add(new EffectEntry("{user}'s favourite hat is suddenly on fire."));
		effects.add(new EffectEntry("{user}'s favourite hat is suddenly fire."));
		effects.add(new EffectEntry("{user}'s favourite shirt is suddenly on fire."));
		effects.add(new EffectEntry("{user}'s favourite shirt is suddenly fire."));
		effects.add(new EffectEntry("{user}'s favourite skin is suddenly on fire."));
		effects.add(new EffectEntry("{user}'s favourite skin is suddenly fire."));
		effects.add(new EffectEntry("{user}'s favourite pants are suddenly on fire."));
		effects.add(new EffectEntry("{user}'s favourite pants are suddenly fire."));
		effects.add(new EffectEntry("{user} has a single tear roll down their cheek for some reason."));
		effects.add(new EffectEntry("{user}'s nose vanish{limit}."));
		effects.add(new EffectEntry("{user} feels like a champion!"));
		effects.add(new EffectEntry("{user} feels a sudden surge of static electricity."));
		effects.add(new EffectEntry("{user}'s shoes are now slightly too large{limit}."));
		effects.add(new EffectEntry("{user} is now Borg{limit}."));
		effects.add(new EffectEntry("{user} has no memory of drinking a potion.",
				"{user} doesn't remember being splashed."));
		effects.add(new EffectEntry("{user} knows the exact location of a particular molecule of oxygen{limit}."));
		effects.add(new EffectEntry("{user} thinks the empty bottle is a snake{limit}.",
				"{user} thinks they're being chased by an imaginary snake{limit}"));
		effects.add(new EffectEntry("{user} gets an urge to have another potion.",
				"{user} gets the urge to drink a potion.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				rateLimit.reset(parameters.triggererName);
				return null;
			}
		}));
		effects.add(new EffectEntry("It tastes sweet.",
				"It smells like sugar as it sticks to {user}."));
		effects.add(new EffectEntry("It tastes sour.",
				"It smells sour and stings a little where it touches skin."));
		effects.add(new EffectEntry("It tastes bitter.",
				"Nothing in particular happens."));
		effects.add(new EffectEntry("It tastes salty.",
				"Nothing in particular happens."));
		effects.add(new EffectEntry("{user} feels like one particular wasp has it out for them suddenly."));
		effects.add(new EffectEntry("{user} zones out for {r:1-10:minute}."));
		effects.add(new EffectEntry("A warp zone opens up next to {user}. (Use " + Config.commandprefix + "warp to jump in)"));
		effects.add(new EffectEntry("After the first sip the potion poofs away.",
				"{user} flinches as the potion is thrown but nothing arrives to hit them..."));
		effects.add(new EffectEntry("{user} looks up and sees the moon smile at them for a second."));
		effects.add(new EffectEntry("The ghost of a plant haunts {user}{limit}."));
		effects.add(new EffectEntry("{r:2-5:} nearby pebbles suddenly shift slightly in {user}'s direction."));
		effects.add(new EffectEntry("The next pie {user} eats tastes slightly less good."));
		effects.add(new EffectEntry("Sitting down suddenly seems like a really terrible idea."));
		effects.add(new EffectEntry("The next fork {user} touches tells them it's most deeply guarded secret."));
		effects.add(new EffectEntry("A voice whispers a secret into {user}'s ear only they can hear."));
		effects.add(new EffectEntry("{user} briefly feel like they have just stepped out of a car."));
		effects.add(new EffectEntry("True enlightenment can be achieved by drinking another potion.",
				"{user} feels as if they should drink a potion for some reason."));
		effects.add(new EffectEntry("For about a second {user} knows the location of a great treasure."));
		effects.add(new EffectEntry("The potion was inside {user} all along."));
		effects.add(new EffectEntry("{user} is suddenly wearing gloves they don't remember putting on."));
		effects.add(new EffectEntry("{user} is suddenly wearing a tiny glove on each finger."));
		effects.add(new EffectEntry("A sudden craving for soup occupies {user}'s thoughts{limit}."));
		effects.add(new EffectEntry("{user} suddenly forgets a random piece of trivia."));
		effects.add(new EffectEntry("A {transformation} flies past that vaguely resembles someone {user} knows."));
		effects.add(new EffectEntry("{user} reboots for an update for {r:1-10:minute}."));
		effects.add(new EffectEntry("Dramatic music briefly plays in the distance."));
		effects.add(new EffectEntry("{user} has a feeling that their face just appeared on a random vegetable somewhere."));
		effects.add(new EffectEntry("The potion bottle is suddenly on fire! {user} takes 1d4 fire damage before letting go of it!",
				"As the potion strikes {user} it bursts into flames! {user} takes 1d4 fire damage."));
		effects.add(new EffectEntry("Once empty the potion bottle fills with a different potion.",
				"{user} looks confused as nothing seems to happen..."));
		effects.add(new EffectEntry("{user} gains the ability to talk to {transformations}{limit}."));
		effects.add(new EffectEntry("{user} sees the sky briefly flash solid dark blue then go back to normal."));
		effects.add(new EffectEntry("When {user} drinks the last drop, a bucket of water materializes above their head and dumps its contents over them. {evade_qc:8:{user} avoids the bucket and it's content!:{user} is drenched in water, followed by a bucket hitting them on the head.}",
				"A bucket of water materializes above {user}'s head and dumps its contents over them. {evade_qc:8:{user} avoids the bucket and it's content!:{user} is drenched in water, followed by a bucket hitting them on the head.}"));
		effects.add(new EffectEntry("Suddenly there's a swarm of wasps behind {user} that chase them for {r:30-60:second}!"));
		effects.add(new EffectEntry("When {user} brings the bottle down they see {appearance:plastic flamingo:p}. It stares into their soul.",
				"When {user} turns around they see {appearance:plastic flamingo:p}. It stares into their soul."));
		effects.add(new EffectEntry("A bard starts playing a lute behind {user}. They don't stop."));
		effects.add(new EffectEntry("A bard starts playing a lute behind {user}{limit}."));
		effects.add(new EffectEntry("The bottle turns into a sword.",
				"{appearance_p} sword appears next to {user}."));
		effects.add(new EffectEntry("The bottle turns into {appearance_p_lc} axe.",
				"{appearance_p} axe appears next to {user}."));
		effects.add(new EffectEntry("The bottle turns into {appearance_p_lc} spear.",
				"{appearance_p} spear appears next to {user}."));
		effects.add(new EffectEntry("The bottle turns into {appearance_p_lc} dagger.",
				"{appearance_p} dagger appears next to {user}."));
		effects.add(new EffectEntry("The bottle turns into {appearance_p_lc} bow.",
				"{appearance_p} bow appears next to {user}."));
		effects.add(new EffectEntry("The bottle turns into {appearance_p_lc} trident.",
				"{appearance_p} trident appears next to {user}."));
		effects.add(new EffectEntry("The bottle turns into {appearance_p_lc} sling.",
				"{appearance_p} sling appears next to {user}."));
		effects.add(new EffectEntry("A genie appears out of the empty bottle, turns it into a pie, then vanishes.",
				"A genie appears out of the smashed bottle, turns it into a pie, then vanishes."));
		effects.add(new EffectEntry("{user} thinks \"What if, like, *we* are the potions man?\". This makes no sense whatsoever."));
		effects.add(new EffectEntry("After drinking the potion {user} notices a label that says \"Side effects may include giggle fits and excessive monologuing.\"",
				"{user} gets a sudden urge to monologue after being hit by the potion."));
		effects.add(new EffectEntry("{user} forgets the location of a great treasure."));
		effects.add(new EffectEntry("Oh no, {user} got a health potion, there's probably a boss fight coming!",
				"{user} regains 1d6 hit points!"));
		effects.add(new EffectEntry("There's an acidic tinge to the potion... A label on the bottle reads \"Who needs internal organs anyway?\". {user} takes  3d6 acid damage.",
				"The fluid burns as it splashes onto {user} who takes 2d6 acid damage."));
		effects.add(new EffectEntry("{user} feels much better!"));
		effects.add(new EffectEntry("A tiny cloud appears with a ridiculous smile on it. It follows {user}{limit}."));
		effects.add(new EffectEntry("The potion contained a computer virus! But {user}'s anti-virus routines destroy it."));
		effects.add(new EffectEntry("The potion contained a computer virus! It just changed {user}'s background..."));
		effects.add(new EffectEntry("The potion contained a computer virus! It sent a message to all of {user}'s friends telling them that they love them!"));
		effects.add(new EffectEntry("The potion contained a computer virus! It changed {user}'s theme to one they don't like!"));
		effects.add(new EffectEntry("The potion contained a computer virus! {user} hears a maniacal laugh as their cursor flips upside down!"));
		effects.add(new EffectEntry("The bottle splits into two revealing a smaller {consistency} {appearance} potion."));
		effects.add(new EffectEntry("A tiny genie appears, gives {user} a thumbs up, and poofs away."));
		effects.add(new EffectEntry("{user} feels chill."));
		effects.add(new EffectEntry("{user} feels the need to smash."));
		effects.add(new EffectEntry("{user} feels the need to use \"" + Config.commandprefix + "shell\"."));
		effects.add(new EffectEntry("{user} feels the need to use \"" + Config.commandprefix + "fling\"."));
		effects.add(new EffectEntry("The bottle turns into a pie.",
				"A pie appears in front of {user}."));
		effects.add(new EffectEntry("The fluid smells faintly of bacon...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				if (parameters.isNew) {
					if (!baconMap.containsKey(parameters.targetName))
						baconMap.put(parameters.targetName, 0);
					int bacon = baconMap.get(parameters.targetName) + 1;
					baconMap.put(parameters.targetName, bacon);
					String prem = "The bottle turns into a piece of bacon. " + parameters.targetName + " has found ";
					if (parameters.isSplash)
						prem = "A piece of bacon appears in front of {user}. " + parameters.targetName + " has found ";
					return prem + bacon + " piece" + (bacon == 1 ? "" : "s") + " of bacon so far.";
				}
				return null;
			}
		}));
		effects.add(new EffectEntry("The bottle turns into an apple.",
				"An apple appears in front of {user}."));
		effects.add(new EffectEntry("A bard behind {user} suddenly stops playing. They were most likely eaten by a monster."));
		effects.add(new EffectEntry("Gravity reverses for {user}{limit}."));
		effects.add(new EffectEntry("{user} now has a mullet{limit}."));
		effects.add(new EffectEntry("The next remote {user} looks for is extra hard to find."));
		effects.add(new EffectEntry("{user} gets a sudden Spice infusion. {user} can see the universe. [Spice Addiction +1]"));
		effects.add(new EffectEntry("{user} feels ever so slightly warmer...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				int rads = Helper.getRandomInt(1, 4);
				if (!radiationMap.containsKey(parameters.targetName))
					radiationMap.put(parameters.targetName, 0);
				int rabs = radiationMap.get(parameters.targetName) + rads;
				radiationMap.put(parameters.targetName, rabs);
				return parameters.targetName + "'s radiation level goes up by " + rads + ". " + parameters.targetName + "'s radiation level is " + rabs + ".";
			}
		}, 2));
		effects.add(new EffectEntry("{user} smells something burning."));
		effects.add(new EffectEntry("The sun turns into a giant baby face for a second. It's horrific."));
		effects.add(new EffectEntry("Everything {user} says is now in Comic Sans{limit}."));
		effects.add(new EffectEntry("Everything {user} says is now in Wingdings{limit}."));
		effects.add(new EffectEntry("{user}'s favourite cup is now upside down."));
		effects.add(new EffectEntry("The potion bottle insults {user}'s haircut.",
				"A disembodied voice insults {user}'s haircut coming from the direction of {trigger}"));
		effects.add(new EffectEntry("After drinking the potion {user} realizes the bottle has their face on it.",
				"{user} sees their face etched into one of the shards of the broken bottle."));
		effects.add(new EffectEntry("Wheels are briefly square."));
		effects.add(new EffectEntry("{user} hears a train whistle in the distance."));
		effects.add(new EffectEntry("The next glass of water {user} has tastes like {appearance}."));
		effects.add(new EffectEntry("As {user} drinks the potion they become the target of a wad of llama spit! {evade:12:1d4}",
				"As the potion hits {user} they become the target of a wad of llama spit! {evade:12:1d4}"));
		effects.add(new EffectEntry("As {user} drinks the potion they seem to have become magnetic and {junk_p} flies towards them! {evade:14:1d6}",
				"As the potion hits {user} they seem to have become magnetic and {junk_p} flies towards them! {evade:14:1d6}"));
		effects.add(new EffectEntry("A swinging blade comes flying towards {user} from nowhere! {evade:16:1d8}"));
		effects.add(new EffectEntry("A trapdoor suddenly opens up under {user}! There are spikes at the bottom. {evade:15:1d6}"));
		effects.add(new EffectEntry("A giant boulder is rolling towards {user}! {evade:15:1d6}"));
		effects.add(new EffectEntry("Time passes. As it does.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				int hours = Helper.getRandomInt(1, 4);
				Tonk.tonkTimeAdd(hours + "h");
				return "Tonk moved forward " + hours + " hour" + (hours == 1 ? "" : "s") + ".";
			}
		}, 1));
		effects.add(new EffectEntry("{user} feels as if time tried to go in reverse for a bit, but couldn't...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				int hours = Helper.getRandomInt(1, 4);
				Tonk.tonkTimeRemove(hours + "h");
				return "Tonk moved back " + hours + " hour" + (hours == 1 ? "" : "s") + ".";
			}
		}, 1));
		effects.add(new EffectEntry("Some tonk points fly by. {user} fails to catch them.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				int points = Helper.getRandomInt(1, 100);
				Tonk.tonkPointsAdd(parameters.targetName, points);
				return "Some tonk points fly by. " + parameters.targetName + " caught " + Tonk.displayTonkPoints(points) + " tonk points.";
			}
		}, 2));
		effects.add(new EffectEntry("Someone just had some of {user}'s favourite food and they didn't get any!"));
		effects.add(new EffectEntry("A bunch of people in white coats approach {user}. {evade_qc:12:{user} successfully evade the people!:{user} is caught and is given a nice jacket with long arms and put in a nice padded room{limit}.}"));
		effects.add(new EffectEntry("For a second it felt like {user} was going to have a huge epiphany...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				if (!researchPointsMap.containsKey(parameters.targetName))
					researchPointsMap.put(parameters.targetName, 0);
				int points = researchPointsMap.get(parameters.targetName) + 1;
				researchPointsMap.put(parameters.targetName, points);
				return parameters.targetName + " gains one research point. " + parameters.targetName + " now has " + points + " point" + (points == 1 ? "" : "s") + ".";
			}
		}, 2));
		effects.add(new EffectEntry("{user} hears a scream from nearby."));
		effects.add(new EffectEntry("{user} feels as if they managed to avoid a terrible curse...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				if (!curseMap.containsKey(parameters.targetName))
					curseMap.put(parameters.targetName, 0);
				int curse = curseMap.get(parameters.targetName) + 1;
				return parameters.targetName + " gains some curse. " + parameters.targetName + " has " + String.valueOf(curse) + " curse.";
			}
		}, 1));
		effects.add(new EffectEntry("{user}'s left sock is now cursed."));
		effects.add(new EffectEntry("{user}'s feet tingle briefly."));
		effects.add(new EffectEntry("{user} spots a shiny thing!"));
		effects.add(new EffectEntry("A green shell flies by! Just out of reach.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				TonkSnipe.refill(parameters.targetName, Tonk.TonkSnipeType.GREEN);
				return parameters.targetName + " barely manages to catch a green shell that appears in front of them!";
			}
		}, 3));
		effects.add(new EffectEntry("A red shell flies by! If only {user} could have reached it.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				TonkSnipe.refill(parameters.targetName, Tonk.TonkSnipeType.RED);
				return parameters.targetName + " barely manages to catch a red shell that appears in front of them!";
			}
		}, 2));
		effects.add(new EffectEntry("{user} feels like they need to drink {appearance_p_lc} potion.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				rateLimit.reset(parameters.triggererName);
				return null;
			}
		}));
		effects.add(new EffectEntry("{user} feels like they need to drink {consistency_p_lc} potion.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				rateLimit.reset(parameters.triggererName);
				return null;
			}
		}));
		effects.add(new EffectEntry("{user} falls into a shaft and drop {r:1-5:floor}!"));
		effects.add(new EffectEntry("{user} comes face to face with a basilisk! {evade_qc:14:{user} avoids it's gaze and gets away!:{user} is turned to stone{limit}}"));
		effects.add(new EffectEntry("A genie tries to turn {user} into {transformation_p}, {evade_qc:12:but {user} successfully dodge the beam!:{user} tries to evade but is caught in the beam and transformed{limit}.}", 3));
		effects.add(new EffectEntry("{user} hears a ringing in the distance....", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters effectActionParameters) {
				if (researchPointsMap.containsKey(effectActionParameters.targetName)) {
					int points = researchPointsMap.get(effectActionParameters.targetName);
					if (points > 0) {
						int tonkPoints = points * 45;
						Tonk.tonkPointsAdd(effectActionParameters.targetName, tonkPoints);
						researchPointsMap.replace(effectActionParameters.targetName, 0);
						return effectActionParameters.targetName + " has " + points + " research points. They figure out how to hack " + Tonk.displayTonkPoints(tonkPoints) + " tonk points for themselves!";
					}
				}
				return effectActionParameters.targetName + " doesn't seem to have any research points.";
			}
		}, 1));
		System.out.println("Registered " + effects.size() + " effects!");

		if (IRCBot.getDebug()) {
			effects = new ArrayList<>();
//			effects.add(new EffectEntry("A genie tries to turn {user} into {transformation_p}, {evade_qc:12:but {user} successfully dodge the beam!:{user} tries to evade but is caught in the beam and transformed{limit}.}", 3));
			/*effects.add(new EffectEntry("I done got drunk!", new Function<EffectActionParameters, String>() {
				@Override
				public String apply(EffectActionParameters effectActionParameters) {
					Helper.sendMessage(effectActionParameters.targetName, "Sup?");
					return null;
				}
			}, -1));*/
			/*effects.add(new EffectEntry("A green shell flies by! Just out of reach.", new Function<EffectActionParameters, String>() {
				@Override
				public String apply(EffectActionParameters parameters) {
					TonkSnipe.refill(parameters.targetName, Tonk.TonkSnipeType.GREEN);
					return parameters.targetName + " barely manages to catch a green shell that appears in front of them!";
				}
			}, 3));*/
			effects.add(new EffectEntry("For a second it felt like {user} was going to have a huge epiphany. It was just a fart...", new Function<EffectActionParameters, String>() {
				@Override
				public String apply(EffectActionParameters parameters) {
					if (!researchPointsMap.containsKey(parameters.targetName))
						researchPointsMap.put(parameters.targetName, 0);
					int points = researchPointsMap.get(parameters.targetName) + 1;
					researchPointsMap.put(parameters.targetName, points);
					return parameters.targetName + " gains one research point. " + parameters.targetName + " now has " + points + " point" + (points == 1 ? "" : "s") + ".";
				}
			}, 2));
			effects.add(new EffectEntry("{user} hears a ringing in the distance....", new Function<EffectActionParameters, String>() {
				@Override
				public String apply(EffectActionParameters effectActionParameters) {
					if (researchPointsMap.containsKey(effectActionParameters.targetName)) {
						int points = researchPointsMap.get(effectActionParameters.targetName);
						if (points > 0) {
							int tonkPoints = points * 45;
							Tonk.tonkPointsAdd(effectActionParameters.targetName, tonkPoints);
							int newPoints = 0;
							researchPointsMap.replace(effectActionParameters.targetName, newPoints);
							return effectActionParameters.targetName + " has " + points + " research points. They figure out how to hack " + Tonk.displayTonkPoints(tonkPoints) + " tonk points for themselves! They now have " + newPoints + " research points.";
						}
					}
					return effectActionParameters.targetName + " doesn't seem to have any research points.";
				}
			}, 1));
		}

		//Never end with punctuation and always start with a space
		//See above for valid tags
		limits.add(" for {r:1-60:second}");
		limits.add(" for {r:2-60:minute}");
		limits.add(" for {r:2-5:hour}");
		limits.add(" until their next sip of water");
		limits.add(" until the next time they hug someone");
		limits.add(" until they say the word \"{codeword}\"");
		limits.add(" until they say the phrase \"{codeword} {codeword2}\"");
		limits.add(" until they exit the room");
		limits.add(" until they see a bird");
		limits.add(" for {r:1-10:moon}");
		limits.add(" until Sozin's Comet returns");
		limits.add(" until they see a unicorn");
		limits.add(" until they see a star fall");
		limits.add(" until they eat a pie");
		limits.add(" until they have an apple");
		limits.add(" until they have a nap");
		limits.add(" until they sneeze");
		limits.add(" until someone stops looking at them");
		limits.add(" until someone looks at them");
		limits.add(" until someone stabs them");
		limits.add(" until someone baps them");
		limits.add(" until they tonk");
		limits.add(" until they have some bacon");
		limits.add(" until they stop thinking about it");
		limits.add(" until they recite the litany against fear");
		limits.add(" until they find a lamp");
		limits.add(" until they have {consistency_p} {appearance} potion");
		limits.add(" until they have {consistency_p} potion");
		limits.add(" until they have {appearance_p} potion");

		//Valid tags: {user},{appearance},{appearance_p},{turn_appearance},{appearance:<item>:p},{consistency},{consistency_p},{transformation},{transformation2},{transformations},{transformations2},{limit}
		specialFluids.put("water", new EffectEntry("{user} drinks some water. Wait... this isn't water... it's {consistency_p} {appearance} potion!", "You splash {user} with some water. Wait... this isn't water... it's {consistency_p} {appearance} potion!"));
		specialFluids.put("soda", new EffectEntry("{user} has some soda. It's fizzy and sweet.", "You splash {user} with some soda. It's fizzy and sticky."));
		specialFluids.put("coffee", new EffectEntry("{user} has some coffee. It's hot and bitter.", "You splash {user} with coffee. It's scalding hot! {user} takes 1d6 fire damage!"));
		specialFluids.put("everything", new EffectEntry("{user} explodes!", "You fail to lift the container containing all the potions. It's too heavy."));
		specialFluids.put("antidote", new EffectEntry("{user} reverts to their original state before any potions.", "You splash {user} with some antidote. {user} reverts to their original state before any potions."));
	}

	static String html;

	public DrinkPotion() throws IOException {
		InputStream htmlIn = getClass().getResourceAsStream("/html/potions.html");
		html = CharStreams.toString(new InputStreamReader(htmlIn, Charsets.UTF_8));
	}

	private void initCommands() {
		rateLimit = new CommandRateLimit(0, 10, 0, true, false, "Having another potion seems like a really bad idea right now...");
		local_command = new Command("drink", new CommandArgumentParser(0, new CommandArgument("Potion", ArgumentTypes.STRING)), rateLimit) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String pot = this.argumentParser.getArgument("Potion");
				PotionEntry potion = new PotionEntry();
				if (pot == null || pot.equals("random") || pot.equals("")) {
					potion = PotionHelper.getRandomPotion();
				} else {
					if (specialFluids.containsKey(pot)) {
						Helper.sendMessage(target, specialFluids.get(pot).getEffectString(nick), nick);
						return;
					} else {
						try {
							potion.setFromCommandParameters(pot);
						} catch (InvalidPotionException ex) {
							Helper.sendMessage(target, "This doesn't seem to be a potion I recognize... Make sure it has an appearance and consistency keyword, and the word \"potion\" in it.");
							return;
						}
					}
				}
				potion.getEffect(nick);
				String actionString = potion.effect.doAction(new EffectActionParameters(nick, null, false, potion.isNew));
				if (actionString == null)
					Helper.sendMessage(target, "You drink " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + ". " + potion.effect.getEffectString(nick));
				else
					Helper.sendMessage(target, "You drink " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + ". " + actionString);
			}
		};
		local_command.setHelpText("Drink a potion with a certain consistency and appearance and something might happen. Syntax: " + Config.commandprefix + local_command.getCommand() + " [potion] Potion needs to contain a valid consistency, appearance and the word 'potion', See " + Config.commandprefix + "potionstats command for a list.");
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

		splash = new Command("splash", new CommandArgumentParser(1, new CommandArgument("Target", ArgumentTypes.STRING), new CommandArgument("Potion", ArgumentTypes.STRING)), new CommandRateLimit(10)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String splashTarget = this.argumentParser.getArgument("Target");
				String potionString = this.argumentParser.getArgument("Potion");

				PotionEntry potion = null;
				if (potionString == null || potionString.equals("random") || potionString.equals("")) {
					potion = PotionHelper.getRandomPotion();
					potion.getEffectSplash(splashTarget, nick);
//					String result = nick + " flings " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + PotionHelper.replaceParamsInEffectString(potion.getEffectString(true), splashTarget, nick);
//					Helper.sendMessage(target, result);
//							Defend.addEvent(nick, splashTarget, target, potion.consistency.getName(false, true) + " " + potion.appearance.getName(false, true) + " potion", Defend.EventTypes.POTION, result);
//							Helper.sendMessage(target, nick + " is trying to splash " + splashTarget + " with a " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion! They have " + Defend.getReactionTimeString() + " if they want to attempt to " + Config.commandprefix + "defend against it!");
//					return;
				} else {
					potion = new PotionEntry();

					try {
						potion.setFromCommandParameters(potionString);
					} catch (InvalidPotionException ex) {
						Helper.sendMessage(target, "This doesn't seem to be a potion I recognize... Make sure it has an appearance and consistency keyword, and the word \"potion\" in it.");
						return;
					}

					potion.getEffectSplash(splashTarget, nick);
				}

				if (specialFluids.containsKey(potionString)) {
					EffectEntry eff = specialFluids.get(potionString);
					PotionHelper.replaceParamsInEffectString(eff, splashTarget, nick, false);
					Helper.sendMessage(target, eff.effectSplashDiscovered);
					return;
				}

				if (potion != null) {
					String actionString = potion.effect.doAction(new EffectActionParameters(splashTarget, nick, true, potion.isNew));
					if (actionString == null)
						Helper.sendMessage(target, "You fling " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + potion.effect.getEffectString(splashTarget, nick, true));
					else
						Helper.sendMessage(target, "You fling " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + actionString);
				} else
					Helper.sendMessage(target, "Potion wasn't set...");
			}
		};
		splash.setHelpText("Splash some unfortunate bystander with a potion! Syntax: " + Config.commandprefix + local_command.getCommand() + " <target> [with <potion>] If [with <potion>] is omitted a random potion is used.");

		get_random = new Command("randompotion", new CommandRateLimit(10)) {
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

		potion_stats = new Command("potionstats", new CommandRateLimit(10)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Config.httpdEnable.equals("true")) {
					Helper.sendMessage(target, "Potion shelf: " + httpd.getBaseDomain() + "/potions", nick);
				} else {
					int apperance_count = appearanceEntries.size();
					int consistencies_count = consistencyEntries.size();
					int effect_count = effects.size();
					int combination_count = apperance_count * consistencies_count;
					Helper.sendMessage(target, "There are " + apperance_count + " appearanceEntries, " + consistencies_count + " consistencies! That's " + combination_count + " potion combinations! There are " + effect_count + " effects!");
				}
			}
		};

		discovered = new Command("discovered", new CommandArgumentParser(0, new CommandArgument("ListAll", ArgumentTypes.BOOLEAN)), new CommandRateLimit(10)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Config.httpdEnable.equals("true")) {
					Helper.sendMessage(target, "Potion shelf: " + httpd.getBaseDomain() + "/potions", nick);
				} else {
					if (this.argumentParser.getBool("ListAll")) {
						for (Map.Entry<String, EffectEntry> entry : potions.entrySet()) {
							String key = entry.getKey();
							String[] keys = key.split(",");
							EffectEntry potion = entry.getValue();
							AppearanceEntry consistency = DrinkPotion.consistencyEntries.get(keys[0]);
							AppearanceEntry appearance = DrinkPotion.appearanceEntries.get(keys[1]);
							Helper.sendMessage(nick, key + ": Potion: " + consistency.Name + " " + appearance.Name + " EffectDrink: '" + potion.effectDrink + ", DiscoveredDrink: " + potion.effectDrinkDiscovered + ", EffectSplash: " + potion.effectSplash + ", DiscoveredSplash: " + potion.effectSplashDiscovered + ", ´Discovered by: " + potion.discoverer + ", UsesRemaining: " + potion.usesRemaining, null, true);
						}
					} else {
						int potions_count = potions.size();
						Helper.sendMessage(target, potions_count + " combination" + (potions_count == 1 ? " has" : "s have") + " been found today!");
					}
				}
			}
		};
		potion_stats.registerAlias("potionsdiscovered");
		potion_stats.registerAlias("discoveredpotions");
		potion_stats.registerAlias("potions");
		potion_stats.registerAlias("potionshelf");
		potion_stats.registerAlias("potionlist");
		potion_stats.registerAlias("listpotions");

		potion_lookup = new Command("potion_lookup", new CommandRateLimit(10)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				AppearanceEntry app = PotionHelper.findAppearanceInString(params);
				AppearanceEntry con = PotionHelper.findConsistencyInString(params);
				String key = PotionHelper.getCombinationKey(con, app);
				String ret = "Potion combination key: " + key;
				if (DrinkPotion.potions.containsKey(key))
					ret += ", Potion registered: Yes";
				else
					ret += ", Potion registered: No";
				Helper.sendMessage(target, ret);
			}
		};
	}

	static class PotionHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			PotionHelper.tryResetPotionList();

			String target = t.getRequestURI().toString();
			StringBuilder response = new StringBuilder();

			int appearanceCount = appearanceEntries.size();
			int consistencyCount = consistencyEntries.size();
			int combinations = appearanceCount * consistencyCount;
			int potionCount = potions.size();
			int effectCount = 0;
			for (EffectEntry effect : effects) {
				effectCount += PotionHelper.countEffectVariations(effect.effectDrink);
			}
			float ratio = (float) effectCount / (float) combinations;
			DecimalFormat format = new DecimalFormat("#.###");
			ArrayList<String> unique_effects_discovered = new ArrayList<>();

			for (Map.Entry<String, EffectEntry> stringEffectEntryEntry : potions.entrySet()) {
				String[] potion = stringEffectEntryEntry.getKey().split(",");
				if (!unique_effects_discovered.contains(potion[1]))
					unique_effects_discovered.add(potion[1]);
			}
			int unique_effect_count = unique_effects_discovered.size();
			StringBuilder potionShelf = new StringBuilder("<div>There are <b>" + appearanceCount + "</b> appearances and <b>" + consistencyCount + "</b> consistencies! That's <b>" + combinations + "</b> different potions! Out of these <b>" + potionCount + "</b> " + (potionCount == 1 ? "has" : "have") + " been discovered today.</div>" +
					"<div>There are <b>" + effectCount + "</b> effects. That's <b>" + format.format(ratio) + "</b> effect" + (ratio == 1 ? "" : "s") + " per potion. <b>" + unique_effect_count + "</b> unique effect" + (unique_effect_count == 1 ? "" : "s") + " " + (unique_effect_count == 1 ? "has" : "have") + " been discovered today.</div>" +
					"<div style='margin-top: 6px;'>A valid potion string (for use with <b>" + Config.commandprefix + "drink</b>) needs an appearance keyword, consistency keyword and the word \"<b>potion</b>\" in it.</div>" +
					"<table style='margin-top: 20px;'><tr><th>Potion</th><th>Effect</th><th>Discovered by</th></tr>");
			for (Map.Entry<String, EffectEntry> stringEffectEntryEntry : potions.entrySet()) {
				String[] potion = stringEffectEntryEntry.getKey().split(",");
				String consistency = consistencyEntries.get(potion[0]).getName();
				String appearance = appearanceEntries.get(potion[1]).getName();
				EffectEntry entry = stringEffectEntryEntry.getValue();
				potionShelf.append("<tr><td>").append(consistency.substring(0, 1).toUpperCase()).append(consistency.substring(1)).append(" ").append(appearance.substring(0, 1).toUpperCase()).append(appearance.substring(1)).append(" Potion</td><td>").append(PotionHelper.concealPlaceholdersForDisplay(entry.effectDrinkDiscovered)).append("</td><td>").append(entry.discoverer).append("</td></tr>");
			}
			potionShelf.append("</table>");
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(), "utf-8");

			StringBuilder entries = new StringBuilder();
			for (Map.Entry<String, AppearanceEntry> entry : appearanceEntries.entrySet()) {
				AppearanceEntry appearanceEntry = entry.getValue();
				String name = appearanceEntry.getName();
				entries.append("<div>").append(name.substring(0, 1).toUpperCase()).append(name.substring(1)).append("</div>");
			}
			potionShelf.append("<div style='margin-top: 10px;'>").append(MakeJavascriptContainer("Show/hide appearances (" + appearanceEntries.size() + ")", entries.toString())).append("</div>");

			entries = new StringBuilder();
			for (Map.Entry<String, AppearanceEntry> entry : consistencyEntries.entrySet()) {
				AppearanceEntry consistency = entry.getValue();
				entries.append("<div>").append(consistency.getName().substring(0, 1).toUpperCase()).append(consistency.getName().substring(1)).append("</div>");
			}
			potionShelf.append("<div style='margin-top: 10px;'>").append(MakeJavascriptContainer("Show/hide consistencies (" + consistencyEntries.size() + ")", entries.toString())).append("</div>");

			StringBuilder navData = new StringBuilder();
			Iterator it = httpd.pages.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				navData.append("<div class=\"innertube\"><h1><a href=\"").append(pair.getValue()).append("\">").append(pair.getKey()).append("</a></h1></div>");
			}

			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(html.getBytes());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					response.append(line.replace("#BODY#", target).replace("#BOTNICK#", IRCBot.getOurNick()).replace("#POTIONS#", potionShelf.toString())
							.replace("#NAVIGATION#", navData.toString())).append("\n");
				}
			}
			t.sendResponseHeaders(200, response.toString().getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}

	private static String MakeJavascriptContainer(String label, String contents) {
		return "<div><span style='cursor: pointer;' onclick='if (this.parentElement.children[1].style.display != \"none\") { this.parentElement.children[1].style.display = \"none\"; } else { this.parentElement.children[1].style.display = null; }'>" + label + "</span><div style='display: none;'>" + contents + "</div></div>";
	}
}

