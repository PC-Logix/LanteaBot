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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Caitlyn
 */
@SuppressWarnings("rawtypes")
public class SWSpoilerGen extends AbstractListener {
	private Command local_command;
	private static Map<String, String[]> words;
	public static String[] sentences = {
			"In this Star Wars movie, our heroes return to take on the First Order and new villain [1] with help from their new friend [2].  Rey builds a new Light Saber with a [3] blade, and they head out to confront The First Order's new superweapon The [4], a space station capable of [5].  They unexpectedly join forces with their old enemy [6] and destroy the superwapon in a battle featuring [7] P.S. Rey's parents are [8] and [9].",
	};
	@Override
	protected void initHook() {
		words = new HashMap<>();
		words.put("1", new String[]{"Kyle Ren", "Malloc", "Darth Sebelius", "Theranos", "Lord Juul"});
		words.put("2", new String[]{"Kim Spacemeasurer", "Teen Yoda", "Dab Tweetdeck", "Yaz Progestin", "TI-83"});
		words.put("3", new String[]{"beige", "ochre", "mauve", "aquamarine", "taupe"});
		words.put("4", new String[]{"Sun Obliterator","Moonsquisher","World Eater","Planet Zester","Superconducting Supercollider"});
		words.put("5", new String[]{"blowing up a planet with a bunch of beams of energy that combine into one","blowing up a bunch of planets with one beam of energy that splits into many","cutting a planet in half and smashing the halves together like two cymbals","increasing the CO2 levels in a planets atmosphere, causing rapid heating","triggering the end credits before the movie is done"});
		words.put("6", new String[]{"Boba Fett","Slacious Crumb","The Space Slug","The Bottom Half of Darth Maul","YouTube Commenters"});
		words.put("7", new String[]{"a bow that shoots little Lightsaber-headed arrows","X-Wings and TIE Fighters dodging the giant letters of the opening crawl","a Sith educational display that uses force lightning to demonstate the dielectric breakdown of air","Kylo Ren putting on another helmet over his smaller one","a Sith car wash where the bristles on the brushes are little Lightsabers"});
		words.put("8", new String[]{"Luke","Leia","Han","Obi-Wan","A Random Junk Trader"});
		words.put("9", new String[]{"Poe","BB-8","Amilyn Holdo","Laura Dern","A Random Junk Trader","That One Droid from the Jawa Sandcrawler that says Gronk"});
		local_command = new Command("swspoiler") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.AntiPings = Helper.getNamesFromTarget(target);
				Helper.sendMessage(target, spoilerParse(), nick, true);
			}
		};
		local_command.setHelpText("Generates a random spoiler for Star Wars");
		IRCBot.registerCommand(local_command);
	}

	public static String spoilerParse() {
		Random random = new Random();
		int index = random.nextInt(sentences.length);
		String spoilerString = sentences[index];
		for (Map.Entry<String, String[]> entry : words.entrySet()) {
			String[] demWords = entry.getValue();
			String key = entry.getKey();
			Pattern pattern = Pattern.compile("\\[" + key + "\\]");
			Matcher matcher = pattern.matcher(spoilerString);
			while(matcher.find()) {
				spoilerString = matcher.replaceFirst(demWords[Helper.getRandomInt(0, demWords.length - 1)]);
				matcher = pattern.matcher(spoilerString);
			}
		}
		return spoilerString;
	}
}
