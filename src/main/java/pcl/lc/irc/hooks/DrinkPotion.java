package pcl.lc.irc.hooks;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
	public static ArrayList<AppearanceEntry> appearanceEntries = new ArrayList<>();
	public static ArrayList<AppearanceEntry> consistencies = new ArrayList<>();
	public static ArrayList<EffectEntry> effects = new ArrayList<>();
	public static HashMap<String, EffectEntry> potions = new HashMap<>();
	public static ArrayList<String> limits = new ArrayList<>();
	public static HashMap<String, EffectEntry> specialFluids = new HashMap<>();

	public static HashMap<String, Integer> curseMap = new HashMap<>();
	public static HashMap<String, Integer> researchPointsMap = new HashMap<>();
	public static HashMap<String, Integer> baconMap = new HashMap<>();
	public static HashMap<String, Integer> radiationMap = new HashMap<>();

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

		appearanceEntries.add(new AppearanceEntry("Blue", "A"));
		appearanceEntries.add(new AppearanceEntry("Red", "A"));
		appearanceEntries.add(new AppearanceEntry("Röd", "A"));
		appearanceEntries.add(new AppearanceEntry("Rød", "A"));
		appearanceEntries.add(new AppearanceEntry("Yellow", "A"));
		appearanceEntries.add(new AppearanceEntry("Purple", "A"));
		appearanceEntries.add(new AppearanceEntry("Green", "A"));
		appearanceEntries.add(new AppearanceEntry("Cyan", "A"));
		appearanceEntries.add(new AppearanceEntry("Tan", "A"));
		appearanceEntries.add(new AppearanceEntry("Black", "A"));
		appearanceEntries.add(new AppearanceEntry("White", "A"));
		appearanceEntries.add(new AppearanceEntry("Pink", "A"));
		appearanceEntries.add(new AppearanceEntry("Gold", "A", "{appearance} colored {item}"));
		appearanceEntries.add(new AppearanceEntry("Silver", "A", "{appearance} colored {item}"));
		appearanceEntries.add(new AppearanceEntry("Copper", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Iron", "An", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Stainless steel", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Aluminium", "An", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Titanium", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Platinum", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Electrum", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Mithril", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Adamantium", "An", "{appearance} colored {item}", "the color of {appearance}")); // Marvel universe
		appearanceEntries.add(new AppearanceEntry("Unobtanium", "An", "{appearance} colored {item}", "the color of {appearance}")); // Avatar
		appearanceEntries.add(new AppearanceEntry("Tiberium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Command & Conquer
		appearanceEntries.add(new AppearanceEntry("Caterium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Satisfactory
		appearanceEntries.add(new AppearanceEntry("Aether", "An", "{appearance} colored {item}", "the color of {appearance}")); // Magic: The Gathering
		appearanceEntries.add(new AppearanceEntry("Bavarium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Just Cause 3
		appearanceEntries.add(new AppearanceEntry("Bombastium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Disney
		appearanceEntries.add(new AppearanceEntry("Dalekanium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Doctor Who
		appearanceEntries.add(new AppearanceEntry("Dilithium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Star Trek
		appearanceEntries.add(new AppearanceEntry("Jumbonium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Futurama
		appearanceEntries.add(new AppearanceEntry("Naqahdah", "A", "{appearance} colored {item}", "the color of {appearance}")); // Stargate
		appearanceEntries.add(new AppearanceEntry("Octiron", "An", "{appearance} colored {item}", "the color of {appearance}")); // Discworld
		appearanceEntries.add(new AppearanceEntry("Redstone", "A", "{appearance} colored {item}", "the color of {appearance}")); // Minecraft
		appearanceEntries.add(new AppearanceEntry("Ruby", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Emerald", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Sapphire", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Amethyst", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Diamond", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Spice", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Radiation", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Tomato", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Lime", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Citrus", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Strawberry", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Chocolate", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Orange", "An"));
		appearanceEntries.add(new AppearanceEntry("Tuna", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Salmon", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Rainbow", "A", "{appearance} colored {item}", "the color of the {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Void", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Ocean", "An", "{appearance} colored {item}", "the color of the {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Grass", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Sky", "A", "{appearance} colored {item}", "the color of the {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Rock", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Aqua", "An", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Dirt", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Quicksilver", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Coral", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Transparent", "A", "{appearance} {item}", "{appearance}"));
		appearanceEntries.add(new AppearanceEntry("Water", "A", "{appearance} colored {item}", "the color of {appearance}"));
		appearanceEntries.add(new AppearanceEntry("Weather", "A"));
		appearanceEntries.add(new AppearanceEntry("Aegisalt", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Cerulium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Ferozium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Moonstone", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Rubium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Solarium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Violium", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound
		appearanceEntries.add(new AppearanceEntry("Automato", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Avesmingo", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Boneboo", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Coralcreep", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Currentcorn", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Oculemon", "An", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Pearlpeas", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Pussplum", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Toxictop", "A", "{appearance} colored {item}", "the color of {appearance}")); // Starbound (food)
		appearanceEntries.add(new AppearanceEntry("Violet", "A"));
		appearanceEntries.add(new AppearanceEntry("Crimson", "A"));
		appearanceEntries.add(new AppearanceEntry("Grathnode", "A", "{appearance} colored {item}", "the color of {appearance}")); // Ar Tonelico
		appearanceEntries.add(new AppearanceEntry("Nectar", "A"));
		appearanceEntries.add(new AppearanceEntry("Honey", "A"));
		appearanceEntries.add(new AppearanceEntry("Apple", "An"));
		appearanceEntries.add(new AppearanceEntry("Pear", "A"));
		System.out.println("Registered " + appearanceEntries.size() + " appearance entries!");

		consistencies.add(new AppearanceEntry("Viscous", "A"));
		consistencies.add(new AppearanceEntry("Cloudy", "A"));
		consistencies.add(new AppearanceEntry("Fluffy", "A"));
		consistencies.add(new AppearanceEntry("Thick", "A"));
		consistencies.add(new AppearanceEntry("Smelly", "A"));
		consistencies.add(new AppearanceEntry("Fragrant", "A"));
		consistencies.add(new AppearanceEntry("Light", "A"));
		consistencies.add(new AppearanceEntry("Shiny", "A"));
		consistencies.add(new AppearanceEntry("Porous", "A"));
		consistencies.add(new AppearanceEntry("Ripe", "A"));
		consistencies.add(new AppearanceEntry("Muddy", "A"));
		consistencies.add(new AppearanceEntry("Shimmering", "A"));
		consistencies.add(new AppearanceEntry("Gloomy", "A"));
		consistencies.add(new AppearanceEntry("Prickly", "A"));
		consistencies.add(new AppearanceEntry("Sour", "A"));
		consistencies.add(new AppearanceEntry("Salty", "A"));
		consistencies.add(new AppearanceEntry("Sweet", "A"));
		consistencies.add(new AppearanceEntry("Runny", "A"));
		consistencies.add(new AppearanceEntry("Boiling", "A"));
		consistencies.add(new AppearanceEntry("Freezing", "A"));
		consistencies.add(new AppearanceEntry("Sedimented", "A"));
		consistencies.add(new AppearanceEntry("Warpy", "A"));
		consistencies.add(new AppearanceEntry("Basic", "A"));
		consistencies.add(new AppearanceEntry("Stirring", "A"));
		consistencies.add(new AppearanceEntry("Bubbly", "A"));
		consistencies.add(new AppearanceEntry("Gloopy", "A"));
		consistencies.add(new AppearanceEntry("Goopy", "A"));
		consistencies.add(new AppearanceEntry("Slimy", "A"));
		consistencies.add(new AppearanceEntry("Solid", "A"));
		consistencies.add(new AppearanceEntry("Molten", "A"));
		consistencies.add(new AppearanceEntry("Fiery", "A"));
		consistencies.add(new AppearanceEntry("Dull", "A"));
		consistencies.add(new AppearanceEntry("Resonating", "A"));
		consistencies.add(new AppearanceEntry("Shining", "A"));
		consistencies.add(new AppearanceEntry("Seeping", "A"));
		consistencies.add(new AppearanceEntry("Smooth", "A"));
		consistencies.add(new AppearanceEntry("Soft", "A"));
		consistencies.add(new AppearanceEntry("Oxidised", "An"));
		consistencies.add(new AppearanceEntry("Mutable", "A"));
		consistencies.add(new AppearanceEntry("Liquid", "A"));
		consistencies.add(new AppearanceEntry("Smelly", "A"));
		consistencies.add(new AppearanceEntry("Powdery", "A"));
		consistencies.add(new AppearanceEntry("Dusty", "A"));
		consistencies.add(new AppearanceEntry("Diluted", "A"));
		consistencies.add(new AppearanceEntry("Concentrated", "A"));
		consistencies.add(new AppearanceEntry("Invisible", "An"));
		consistencies.add(new AppearanceEntry("Simulated", "A"));
		consistencies.add(new AppearanceEntry("Forked", "A"));
		consistencies.add(new AppearanceEntry("Spooned", "A"));
		System.out.println("Registered " + consistencies.size() + "consistencies!");

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
		effects.add(new EffectEntry("{user} realizes this was actually a {consistency} {appearance} potion."));
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
				"{user} gets the urge to drink a potion."));
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
		effects.add(new EffectEntry("The potion bottle is suddenly on fire! {user} takes 1d4 damage before letting go of it!",
				"As the potion strikes {user} it bursts into flames!"));
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
		effects.add(new EffectEntry("There's an acidic tinge to the potion... A label on the bottle reads \"Who needs internal organs anyway?\"",
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
				if (!baconMap.containsKey(parameters.targetName))
					baconMap.put(parameters.targetName, 0);
				int bacon = baconMap.get(parameters.targetName) + 1;
				baconMap.put(parameters.targetName, bacon);
				String prem = "The bottle turns into a piece of bacon. " + parameters.targetName + " has found ";
				if (parameters.isSplash)
					prem = "A piece of bacon appears in front of {user}. " + parameters.targetName + " has found ";
				return prem + bacon + " piece" + (bacon == 1 ? "" : "s") + " of bacon so far.";
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
		}));
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
		}));
		effects.add(new EffectEntry("{user} feels as if time tried to go in reverse for a bit, but couldn't...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				int hours = Helper.getRandomInt(1, 4);
				Tonk.tonkTimeRemove(hours + "h");
				return "Tonk moved back " + hours + " hour" + (hours == 1 ? "" : "s") + ".";
			}
		}));
		effects.add(new EffectEntry("Some tonk points fly by. {user} fails to catch them.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				int points = Helper.getRandomInt(1, 100);
				Tonk.tonkPointsAdd(parameters.targetName, points);
				return parameters.targetName + " gained " + Tonk.displayTonkPoints(points) + " tonk points.";
			}
		}));
		effects.add(new EffectEntry("Someone just had some of {user}'s favourite food and they didn't get any!"));
		effects.add(new EffectEntry("A bunch of people in white coats approach {user}. {evade_qc:12:{user} successfully evade the people!:{user} is caught and is given a nice jacket with long arms and put in a nice padded room{limit}.}"));
		effects.add(new EffectEntry("For a second it felt like {user} was going to have a huge epiphany. It was just a fart...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				if (!researchPointsMap.containsKey(parameters.targetName))
					researchPointsMap.put(parameters.targetName, 0);
				int points = researchPointsMap.get(parameters.targetName) + 1;
				researchPointsMap.put(parameters.targetName, points);
				return parameters.targetName + " gains one research point. {user} now has " + points + " point" + (points == 1 ? "" : "s") + ".";
			}
		}));
		effects.add(new EffectEntry("{user} hears a scream from nearby."));
		effects.add(new EffectEntry("{user} feels as if they managed to avoid a terrible curse...", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				if (!curseMap.containsKey(parameters.targetName))
					curseMap.put(parameters.targetName, 0);
				int curse = curseMap.get(parameters.targetName) + 1;
				return parameters.targetName + " gains some curse. " + parameters.targetName + " has " + String.valueOf(curse) + " curse.";
			}
		}));
		effects.add(new EffectEntry("{user}'s left sock is now cursed."));
		effects.add(new EffectEntry("{user}'s feet tingle briefly."));
		effects.add(new EffectEntry("{user} spots a shiny thing!"));
		effects.add(new EffectEntry("A green shell flies by! Just out of reach.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				TonkSnipe.refill(parameters.targetName, Tonk.TonkSnipeType.GREEN);
				return parameters.targetName + " barely manages to catch a green shell that appears in front of them!";
			}
		}));
		effects.add(new EffectEntry("A red shell flies by! If only {user} could have reached it.", new Function<EffectActionParameters, String>() {
			@Override
			public String apply(EffectActionParameters parameters) {
				TonkSnipe.refill(parameters.targetName, Tonk.TonkSnipeType.RED);
				return parameters.targetName + " barely manages to catch a red shell that appears in front of them!";
			}
		}));
		effects.add(new EffectEntry("{user} feels like they need to drink {appearance_p_lc} potion."));
		effects.add(new EffectEntry("{user} feels like they need to drink {consistency_p_lc} potion."));
		effects.add(new EffectEntry("{user} falls into a shaft and drop {r:1-5:floor}!"));
		effects.add(new EffectEntry("{user} comes face to face with a basilisk! {evade_qc:14:{user} avoids being turned to stone!:{user} is turned to stone{limit}}"));
		effects.add(new EffectEntry("{user} "));
		System.out.println("Registered " + effects.size() + " effects!");

		if (IRCBot.getDebug()) {
			effects = new ArrayList<>();
			effects.add(new EffectEntry("A bunch of people in white coats approach {user}. {evade_qc:12:{user} successfully evade the people!:{user} is caught and is given a nice jacket with long arms and put in a nice padded room{limit}.}"));
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
		CommandRateLimit rateLimit = new CommandRateLimit(0, 10, 0, "Having another potion seems like a really bad idea right now...");
		local_command = new Command("drink", new CommandArgumentParser(0, new CommandArgument("Potion", "String")), rateLimit) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, ArrayList<String> params) {
				String pot = this.argumentParser.getArgument("Potion");
				PotionEntry potion;
				if (pot == null || pot.equals("random")) {
					potion = PotionHelper.getRandomPotion();
					potion.getEffect(nick);
					Helper.sendMessage(target, "You drink " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + ". " + PotionHelper.replaceParamsInEffectString(potion.getEffectString(), nick));
					return;
				} else if (pot.equals("^")) {
					List<Map.Entry<UUID, List<String>>> list = new ArrayList<>(IRCBot.messages.entrySet());
					for (Map.Entry<UUID, List<String>> entry : Lists.reverse(list)) {
						if (entry.getValue().get(0).equals(target)) {
							if (entry.getValue().get(2).toLowerCase().contains("potion")) {
								pot = entry.getValue().get(2);
							}
						}
					}
				} else {
					if (specialFluids.containsKey(pot)) {
						Helper.sendMessage(target, PotionHelper.replaceParamsInEffectString(specialFluids.get(pot).getEffectString(), nick));
						return;
					}
				}

				try {
					potion = new PotionEntry();
					potion.setFromCommandParameters(pot);

					potion.getEffect(nick);
					Helper.sendMessage(target, "You drink " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + ". " + PotionHelper.replaceParamsInEffectString(potion.getEffectString(), nick));
				} catch (InvalidPotionException ex) {
					Helper.sendMessage(target, "This doesn't seem to be a potion I recognize... Make sure it has an appearance and consistency keyword, and the word \"potion\" in it.");
				}
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

		splash = new Command("splash", new CommandRateLimit(10)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (params.length() == 0) {
					Helper.sendMessage(target, "Try " + Config.commandprefix + this.getCommand() + " <target>[ with <potion>]");
				} else {
					String[] split = params.split(" with ");

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
						potion.getEffectSplash(splashTarget, nick);
						String result = nick + " flings " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + PotionHelper.replaceParamsInEffectString(potion.getEffectString(true), splashTarget, nick);
						Helper.sendMessage(target, result);
//							Defend.addEvent(nick, splashTarget, target, potion.consistency.getName(false, true) + " " + potion.appearance.getName(false, true) + " potion", Defend.EventTypes.POTION, result);
//							Helper.sendMessage(target, nick + " is trying to splash " + splashTarget + " with a " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion! They have " + Defend.getReactionTimeString() + " if they want to attempt to " + Config.commandprefix + "defend against it!");
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
						Helper.sendMessage(target, PotionHelper.replaceParamsInEffectString(specialFluids.get(potionString).getEffectString(true), splashTarget, nick));
						return;
					}

					try {
						potion = new PotionEntry();
						potion.setFromCommandParameters(potionString);

						potion.getEffect(nick);
						Helper.sendMessage(target, "You fling " + potion.consistency.getName(true, true) + " " + potion.appearance.getName(false, true) + " potion" + (potion.isNew ? " (New!)" : "") + " that splashes onto " + splashTarget + ". " + PotionHelper.replaceParamsInEffectString(potion.getEffectString(true), splashTarget, nick));
					} catch (InvalidPotionException ex) {
						Helper.sendMessage(target, "This doesn't seem to be a potion I recognize... Make sure it has an appearance and consistency keyword, and the word \"potion\" in it.");
					}
				}
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
					int consistencies_count = consistencies.size();
					int effect_count = effects.size();
					int combination_count = apperance_count * consistencies_count;
					Helper.sendMessage(target, "There are " + apperance_count + " appearanceEntries, " + consistencies_count + " consistencies! That's " + combination_count + " potion combinations! There are " + effect_count + " effects!");
				}
			}
		};

		discovered = new Command("discovered", new CommandRateLimit(10)) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (Config.httpdEnable.equals("true")) {
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
			int consistencyCount = consistencies.size();
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
				String consistency = consistencies.get(Integer.parseInt(potion[0])).getName();
				String appearance = appearanceEntries.get(Integer.parseInt(potion[1])).getName();
				EffectEntry entry = stringEffectEntryEntry.getValue();
				potionShelf.append("<tr><td>").append(consistency.substring(0, 1).toUpperCase()).append(consistency.substring(1)).append(" ").append(appearance.substring(0, 1).toUpperCase()).append(appearance.substring(1)).append(" Potion</td><td>").append(entry.effectDrinkDiscovered.replace("{user}", "User")).append("</td><td>").append(entry.discoverer).append("</td></tr>");
			}
			potionShelf.append("</table>");
			List<NameValuePair> paramsList = URLEncodedUtils.parse(t.getRequestURI(), "utf-8");

			StringBuilder entries = new StringBuilder();
			for (AppearanceEntry appearanceEntry : appearanceEntries) {
				String name = appearanceEntry.getName();
				entries.append("<div>").append(name.substring(0, 1).toUpperCase()).append(name.substring(1)).append("</div>");
			}
			potionShelf.append("<div style='margin-top: 10px;'>").append(MakeJavascriptContainer("Show/hide appearances (" + appearanceEntries.size() + ")", entries.toString())).append("</div>");

			entries = new StringBuilder();
			for (AppearanceEntry consistency : consistencies) {
				entries.append("<div>").append(consistency.getName().substring(0, 1).toUpperCase()).append(consistency.getName().substring(1)).append("</div>");
			}
			potionShelf.append("<div style='margin-top: 10px;'>").append(MakeJavascriptContainer("Show/hide consistencies (" + consistencies.size() + ")", entries.toString())).append("</div>");

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

