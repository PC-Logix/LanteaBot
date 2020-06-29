package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import java.util.ArrayList;

/**
 * @author Forecaster
 *
 */
@SuppressWarnings("rawtypes")
public class CurseWord extends AbstractListener {
	private Command local_command;
	private static ArrayList<String> curses;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Holy manbats Batman!");
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
		curses.add("Voldemort");
		curses.add("Fudge");
		curses.add("Jiminy Cricket");
		curses.add("Dad-Sizzle");
		curses.add("Bejabbers");
		curses.add("Sard");
		curses.add("Waesucks");
	}

	public static String getRandomCurse() {
		return curses.get(Helper.getRandomInt(0, curses.size() - 1));
	}

	private void initCommands() {
		local_command = new Command("curseword") {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (curses.size() == 0)
					Helper.sendMessage(target, "I don't know any curses...");
				else
					Helper.sendMessage(target, getRandomCurse() + Helper.getRandomExclamations(true, false), nick);
			}
		};
		local_command.registerAlias("curses");
		local_command.registerAlias("cursewd");
		local_command.registerAlias("cursew");
		local_command.registerAlias("swear");
	}
}
