/**
 *
 */
package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class Drama extends AbstractListener {
	private Command local_command;
	String[] people = {"Michiyo", "Player", "jadedcat", "Alblaka", "Greg", "Eloraam", "AUTOMATIC_MAIDEN", "Alz454", "RS485", "Shukaro", "Toops", "CovertJaguar", "Pahimar", "Sengir", "Azanor", "jeb", "Greymerk", "Dinnerbone", "Grum", "dan200", "Cloudy", "KingLemming", "Zeldo", "AlgorithmX2", "Mikee", "Eyamaz", "kakermix", "cpw", "LexManos", "Vswe", "Direwolf20", "Calclavia", "Reika", "Sangar", "skyboy", "FlowerChild", "SpaceToad", "ChickenBones", "Notch", "Pokefenn", "Shadowclaimer", "Vazkii", "pixlepix", "nekosune", "copygirl", "immibis", "RichardG", "JAKJ", "mDiyo", "pillbox", "progwml6", "PowerCrystals", "GUIpsp", "nallar", "Soaryn", "Soaryn", "AbrarSyed", "Sunstrike", "BevoLJ", "asie", "tterrag", "CrazyPants", "Aidan", "Binnie", "Mojang", "ProfMobius", "peterix", "RWTema", "Slowpoke", "Curse", "bspkrs", "Mr_okushama", "Searge", "iChun", "Krapht", "Erasmus_Crowley", "MysteriousAges", "Drullkus", "Micdoodle8","GenPage","Hunterz","tgame14", "Velotican", "kirindave", "MachineMuse", "M3gaFr3ak", "Lunatrius", "wha-ha-ha", "Katie"};
	String[] sites = {"FTB Forums", "MCF", "Reddit", "4chan", "Technic Forums", "IC2 Forums", "GitHub", "BitBucket", "IRC", "ForgeCraft", "Patreon", "BTW Forums", "GregTech thread", "Google+", "Twitch"};
	String[] things = {"OpenPrinter", "OpenFM", "OpenSecurity", "LanteaCraft", "ForgeCraft", "Simply Jetpacks", "RedPower 2", "ModLoader", "RedLogic", "Forge MultiPart", "Project: Red", "BuildCraft", "Tinkers' Steelworks", "Artifice", "Roguelike Dungeons", "IndustrialCraft 2", "Equivalent Exchange", "Forestry", "RailCraft", "Simple Jetpacks", "Compact Solars", "ComputerCraft", "Wireless Redstone", "OpenComputers", "GregTech", "Ars Magica", "Thaumcraft", "FTB", "Technic", "Resonant Rise", "MineFactory Reloaded", "Magic Farm 2", "Tekkit", "MCPC+", "ATLauncher", "Metallurgy", "Logistics Pipes", "MCUpdater", "MultiMC", "Curse", "Mojang", "Test Pack Please Ignore", "Agrarian Skies", "Steve's Carts", "Steve's Factory Manager", "BiblioCraft",  "Minecraft", "XyCraft", "Forge", "GregTech", "OpenBlocks", "OpenPeripheral", "OpenComputers", "MFFS", "RotaryCraft", "Big Reactors", "Thermal Expansion 3", "Extra Utilities", "Universal Electricity", "Not Enough Items", "Portal Gun mod", "the Mojang launcher", "Too Many Items", "OptiFine", "Extra Cells", "ExtraBiomesXL", "Biomes O' Plenty", "Better than Wolves", "Schematica", "Tinker's Construct", "Natura", "Hexxit", "Iron Chests", "open-source mods", "closed-source mods", "Not Enough Mods", "Ender IO", "Mekanism", "Minecraft 1.7", "Pixelmon", "Pixelmon", "JABBA", "WAILA", "Opis", "CraftGuide", "Iguana Tweaks", "Tinkers Mechworks", "the Minecraft Drama Generator", "MineChem", "LittleMaidMob", "MCP", "Immibis' Microblocks", "Carpenter's Blocks", "Chisel", "Applied Energistics", "Applied Energistics 2", "Rotatable Blocks", "EnhancedPortals 3", "Ex Nihilo", "Ex Aliquo", "Magic Bees", "BetterStorage", "Backpacks", "Aether II", "Highlands", "Alternate Terrain Generation", "InfiCraft", "Bukkit", "Spigot", "SpoutCraft", "MortTech", "ICBM", "Galacticraft", "Modular Power Suits", "Team CoFH", "Extra Bees", "Extra Trees", "Mo' Creatures", "Grimoire of Gaia", "Atum", "Agriculture", "Sync", "Hats", "Nether Ores"};
	String[] packs = {"Feed The Beast", "the ForgeCraft pack", "FTB Monster", "FTB Unstable", "Agrarian Skies", "Direwolf20 Pack", "Tekkit", "Hexxit", "ATLauncher", "Resonant Rise", "MCUpdater", "Attack of the B-Team", "Mindcrack", "Magic Maiden", "ForgeCraft", "Technic"};
	String[] function = {"MJ support", "RF support", "EU support", "FMP compatibility", "quarries", "automatic mining", "GregTech balance", "ComputerCraft APIs", "OpenComputers APIs", "Bukkit plugin compatibility", "MCPC+ support", "ID allocation", "ore processing", "smelting", "crafting", "balance", "bees", "ThaumCraft integration", "realism", "decorative blocks", "new mobs", "TCon tool parts", "new wood types", "bundled cable support", "new player capes", "more drama", "less drama", "microblocks", "drama generation commands", "Blutricity support", "overpowered items", "underpowered items", "new ores", "better SMP support", "achievements", "quests", "more annoying worldgen"};
	String[] adj = {"bad", "wrong", "illegal", "horrible", "nasty", "not in ForgeCraft", "noncompliant with Mojang's EULA", "a serious problem", "incompatible", "a waste of time", "wonderful", "amazing", "toxic", "too vanilla", "shameful", "disappointing", "bloated", "outdated", "incorrect", "full of drama", "too realistic"};
	String[] badsoft = {"malware", "spyware", "adware", "DRM", "viruses", "trojans", "keyloggers", "stolen code", "easter eggs", "potential login stealers", "adf.ly links", "bad code", "stolen assets", "malicious code", "secret backdoors"};
	String[] drama = {"bugs", "crashes", "drama", "lots of drama", "imbalance", "pain and suffering", "piracy", "bees", "adf.ly"};
	String[] crash = {"crash", "explode", "break", "lag", "blow up", "corrupt chunks", "corrupt worlds", "rain hellfish", "spawn bees"};
	String[] ban = {"ban", "kick", "put a pumpkin of shame on", "add items mocking", "blacklist", "whitelist", "give admin rights to", "shame", "destroy"};
	String[] code = {"code", "assets", "ideas", "concepts", "a single function", "5 lines of code", "a class", "a few files", "a ZIP file", "Gradle buildscripts", "a GitHub repository"};
	String[] worse = {"worse", "better", "faster", "slower", "more stable", "less buggy"};
	String[] ac1 = {"sue", "destroy the life of", "flame", "cause drama about", "complain about", "kick"};
	String[] price = {"200$", "250$", "300$", "350$", "400$", "450$", "500$", "600$"};
	String[] activates = {"activates", "works", "functions", "breaks"};
	String[] says = {"says", "tweets", "claims", "confirms", "denies"};
	String[] enormous = {"big", "large", "huge", "gigantic", "enormous"};
	String[] sentences = {	
			"[people] launched a DoS attack on the website of [things]",
			"[sites] urges everyone to stop using [things]",
			"After a [enormous] amount of requests, [packs] removes [things]",
			"After a [enormous] amount of requests, [packs] adds [things]",
			"After a [enormous] amount of requests, [packs] adds [function] to [things]",
			"[people] plays [things] on Twitch",
			"[people] fixes [function] in [things] to be unlike [things]",
			"[things] makes [things] [crash], [sites] users complain",
			"[people] complained about being in [things] on [sites]",
			"[people] releases [code] of [things] for [price]",
			"[sites] considers [things] worse than [things]",
			"[people] made [things] depend on [things]",
			"[people] bans [people] from using [things] in [packs]",
			"[people] complains that [things] discussion doesn't belong on [sites]",
			"[people] has a Patreon goal to add [function] to [things] for [price] a month",
			"[people] has a Patreon goal to add [things] compatibility to [things] for [price] a month",
			"[people] complains that [people] replaced [things] by [things]",
			"[people] complains that [people] replaced [things] by [things] in [packs]",
			"[people] complains that [people] removed [function] in [packs]",
			"[people] decided that [things] is too [adj] and replaced it with [things]",
			"[people] [says] [things] is [adj].",
			"[people] [says] [things] is literally [adj].",
			"[things] is not updated for the latest version of Minecraft.",
			"[people] removes [things] from [packs].",
			"[people] adds [things] to [packs].",
			"[people] quits modding. Fans of [things] rage.",
			"[people] is found to secretly like [things]",
			"[people] openly hates [function] in [things]",
			"[people] threatens to [ac1] [people] until they remove [things] from [packs]",
			"[people] threatens to [ac1] [people] until they remove [function] from [things]",
			"[people] threatens to [ac1] [people] until they add [function] to [things]",
			"[people] came out in support of [things]",
			"[people] came out in support of [drama]",
			"[people] and [people] came out in support of [drama]",
			"[people] came out against [drama], [sites] rages",
			"[people] and [people] came out against [drama], [sites] rages",
			"[people] forks [things] causing [drama]",
			"[people] [says] to replace [things] with [things]",
			"[people] [says] [people] causes drama",
			"[things] fans claim that [things] should be more like [things]",
			"[things] fans claim that [things] should have better [function]",
			"[people] [says] that [things] should be more like [things]",
			"[people] [says] that [things] should be less like [things]",
			"[people] rebalances [things] for [packs]",
			"[people] adds [function] to [things] by request of [people]",
			"[people] removes [function] from [things] by request of [people]",
			"[people] removes compatibility between [things] and [things] by request of [people]",
			"[people] [says] [people]'s attitude is [adj]",
			"[people] [says] [sites]'s attitude is [adj]",
			"[people] quits the development team of [things]",
			"[people] [says] [things] is too much like [things]",
			"[people] [says] [things] is a ripoff of [things]",
			"[people] [says] [people] stole [code] from [people]",
			"[people] [says] [people] did not steal [code] from [people]",
			"[people] decides to [ban] [people] from [packs]",
			"[things] doesn't work with [things] since the latest update",
			"[people] sues [things]",
			"[people] [says] [things] is [adj] on [sites]",
			"[people] [says] [things] is full of [badsoft]",
			"[people] [says] [things] causes [drama]",
			"[people] [says] [things] causes [drama] when used with [things]",
			"[people] [says] using [things] and [things] together is [adj]",
			"[people] rants about [things] on [sites]",
			"[people] rants about [function] in mods on [sites]",
			"[people] steals code from [things]",
			"[things] breaks [function]",
			"[people] sues [things] developers",
			"[people] reminds you that [things] is [adj]",
			"[people] and [people] get into a drama fight on [sites]",
			"Fans of [things] and [things] argue on [sites]",
			"[people] and [people] argue about [things]",
			"[people] puts [badsoft] in [things]",
			"[people] complains about [things] breaking [things]",
			"[people] complains about [things] breaking [function]",
			"[people] complains about [things] including [function]",
			"[things] breaks [function] in [things]",
			"[things] breaks [things] support in [things]",
			"[things] adds code to [ban] [people] automatically",
			"[things] adds code to [ban] people using [things]",
			"[things] removes compatibility with [things]",
			"[people] [says] not to use [things]",
			"[people] [says] not to use [things] with [things]",
			"[people] finds [badsoft] in [things]",
			"[people] drew a nasty graffiti about [people]",
			"[people] drew a nasty graffiti about [things]",
			"[things] makes [things] [crash] when used with [things]",
			"[things] makes [things] [crash] when used by [people]",
			"[things] makes [things] crash [things] when used by [people]",
			"[things] adds [badsoft] that only [activates] in [packs]",
			"[things] adds [badsoft] that only [activates] alongside [things]",
			"[things] makes [people] invincible from [things] in [packs]",
			"[people] decides to base their entire modpack on [things]",
			"[people] tweaks balance in [things] too much, annoying [sites]",
			"[people] tweaks balance in [things] too much, annoying [people]",
			"[people] [says] [people] is worse than [people]",
			"[people] [says] [things] is [worse] than [things]",
			"[people] bans [people] from [sites]"};
	@Override
	protected void initHook() {
		local_command = new Command("drama", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (!IRCBot.isIgnored(event.getUser().getNick())) {
					Random random = new Random();
					int index = random.nextInt(sentences.length);
					Helper.sendMessage(target, dramaParse(sentences[index]), nick);
				}
			}
		};
		local_command.setHelpText("Generates random drama using Mod Developers, Projects, and other semi random data.");
		IRCBot.registerCommand(local_command);
	}

	public String dramaParse(String dramaString) {
		//TODO: DO THE DRAMA!
		String drama = "llama";
		
		return drama;
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
