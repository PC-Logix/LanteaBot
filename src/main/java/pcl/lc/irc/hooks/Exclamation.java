package pcl.lc.irc.hooks;

import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.entryClasses.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.entryClasses.CommandRateLimit;
import pcl.lc.utils.CommandChainState;
import pcl.lc.utils.CommandChainStateObject;
import pcl.lc.utils.Helper;
import pcl.lc.utils.TablesOfRandomThings;

import java.util.ArrayList;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class Exclamation extends AbstractListener {
	private Command command_curse_word;
	private Command command_exclamation;
	private static ArrayList<String> curses;
	private static ArrayList<String> positive;
	private static ArrayList<String> surprised;
	private static ArrayList<String> negative;

	public enum TypeFilter {
		ALL,
		CURSE_WORD,
		POSITIVE,
		SURPRISED,
		NEGATIVE
	}

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(command_curse_word);
		IRCBot.registerCommand(command_exclamation);
		curses = new ArrayList<>();
		curses.add("Heckgosh"); //xkcd #1975
		curses.add("Jeepers"); //xkcd #1975
		curses.add("By my throth"); //xkcd #1975
		curses.add("Goshhawk"); //xkcd #1975
		curses.add("Willikers"); //xkcd #1975
		curses.add("Dogast"); //http://mentalfloss.com/article/88747/10-old-words-curses-and-cursing
		curses.add("Dagnabbit");
		curses.add("Consarn it"); //https://www.littlethings.com/old-bad-words/
		curses.add("Fopdoodle"); //https://www.littlethings.com/old-bad-words/
		curses.add("Gadsbudlikins"); //https://www.littlethings.com/old-bad-words/
		curses.add("Potzblitz"); //https://www.littlethings.com/old-bad-words/
		curses.add("Zounderkite"); //https://matadornetwork.com/life/21-amazing-forgotten-curse-words-need-bring-back/
		curses.add("Aw jeez");
		curses.add("Dagnammit");
		curses.add("Fudge");
		curses.add("Jiminy Cricket");
		curses.add("Dad-Sizzle");
		curses.add("Bejabbers");
		curses.add("Sard");
		curses.add("Waesucks");
		curses.add("Crud");
		curses.add("Fiddlesticks");

		positive = new ArrayList<>();
		positive.add("Woooo");
		positive.add("Yay");
		positive.add("Yay");
		positive.add("Boo-yah");
		positive.add("Huzzah");
		positive.add("Hooray");
		positive.add("Yippee");
		positive.add("Yippee");
		positive.add("Kapow");
		positive.add("Boom");
		positive.add("Swell");
		positive.add("Awesome");
		positive.add("Bingo");
		positive.add("Eureka");
		positive.add("Yeah");
		positive.add("Wild");
		positive.add("Awesome");

		surprised = new ArrayList<>();
		surprised.add("Woah");
		surprised.add("Wah");
		surprised.add("Zoinks");
		surprised.add("Wut");
		surprised.add("Golly");
		surprised.add("Geez");
		surprised.add("Uh-oh");
		surprised.add("Wow");
		surprised.add("Yow");

		negative = new ArrayList<>();
		negative.add("Darn it");
		negative.add("Darn");
		negative.add("Blast");
		negative.add("Shoot");
		negative.add("Yikes");
		negative.add("Eh");
	}

	public static String getRandomExpression() {
		return getRandomExpression(TypeFilter.ALL);
	}

	public static String getRandomExpression(TypeFilter filter) {
		return getRandomExpression(filter, false);
	}

	public static String getRandomExpression(TypeFilter filter, boolean lowerCase) {
		ArrayList<String> words = new ArrayList<>();
		if (filter.equals(TypeFilter.ALL)) {
			words.addAll(curses);
			words.addAll(positive);
			words.addAll(surprised);
			words.add("Holy " + TablesOfRandomThings.getRandomGarbageItem(false, true) + " Batman");
			words.add("Holy " + TablesOfRandomThings.getRandomGarbageItem(false, true) + " Batman");
			words.add("Holy " + TablesOfRandomThings.getRandomGarbageItem(false, true) + " Batman");
			words.addAll(negative);
		} else if (filter.equals(TypeFilter.CURSE_WORD))
			words.addAll(curses);
		else if (filter.equals(TypeFilter.POSITIVE))
			words.addAll(positive);
		else if (filter.equals(TypeFilter.SURPRISED)) {
			words.addAll(surprised);
			words.add("Holy " + TablesOfRandomThings.getRandomGarbageItem(false, true) + " Batman");
			words.add("Holy " + TablesOfRandomThings.getRandomGarbageItem(false, true) + " Batman");
			words.add("Holy " + TablesOfRandomThings.getRandomGarbageItem(false, true) + " Batman");
		} else if (filter.equals(TypeFilter.NEGATIVE))
			words.addAll(negative);
		if (words.size() == 0)
			return "...";
		if (lowerCase)
			return words.get(Helper.getRandomInt(0, words.size() - 1)).toLowerCase();
		return words.get(Helper.getRandomInt(0, words.size() - 1));
	}

	private void initCommands() {
		CommandRateLimit limit = new CommandRateLimit(0, 5, 0, true, false);
		command_curse_word = new Command("curseword", limit) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, getRandomExpression(TypeFilter.CURSE_WORD) + TablesOfRandomThings.getRandomExclamations(true, false), nick);
				return new CommandChainStateObject();
			}
		};
		command_curse_word.registerAlias("curses");
		command_curse_word.registerAlias("cursewd");
		command_curse_word.registerAlias("cursew");
		command_curse_word.registerAlias("swear");
		command_curse_word.registerAlias("swearword");
		command_curse_word.setHelpText("Holy manbats Batman!");

		command_exclamation = new Command("exclamation", limit) {
			@Override
			public CommandChainStateObject onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				Helper.sendMessage(target, getRandomExpression(TypeFilter.ALL) + TablesOfRandomThings.getRandomExclamations(true, false), nick);
				return new CommandChainStateObject();
			}
		};
		command_exclamation.registerAlias("excl");
		command_exclamation.setHelpText("Wot in tarnation?!");
	}
}
