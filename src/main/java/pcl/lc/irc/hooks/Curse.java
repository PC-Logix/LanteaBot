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
public class Curse extends AbstractListener {
	private Command local_command;
	private ArrayList<String> curses;

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

	private void initCommands() {
		local_command = new Command("curse", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				if (curses.size() == 0)
					Helper.sendMessage(target, "I don't know any curses...");
				else
					Helper.sendMessage(target, curses.get(Helper.getRandomInt(0, curses.size() - 1)) + Helper.getRandomExlamations(true, false), nick);
			}
		};
		local_command.registerAlias("curses");
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
