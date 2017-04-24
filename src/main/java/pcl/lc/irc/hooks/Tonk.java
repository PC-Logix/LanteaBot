/**
 * 
 */
package pcl.lc.irc.hooks;

import com.google.api.client.util.DateTime;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Database;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Caitlyn
 *
 */
@SuppressWarnings("rawtypes")
public class Tonk extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, local_command.getHelpText());

		Database.addStatement("CREATE TABLE IF NOT EXISTS Tonks(user STRING UNIQUE PRIMARY KEY, tonk_time INT)");
		Database.addPreparedStatement("getUserTonk","SELECT tonk_time WHERE user = ?;");
		Database.addPreparedStatement("setUserTonk","INSERT OR REPLACE into Tonks(user, tonk_time) values (?, ?);");
	}

	private void initCommands() {
		local_command = new Command("tonk", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				try {
					long lastTonk = 0;
					String tonk = Database.getJsonData("lasttonk");
					if (tonk != "")
						lastTonk = Long.parseLong(tonk);

					PreparedStatement get = Database.getPreparedStatement("getUserTonk");
					get.setString(1, nick);
					ResultSet result = get.executeQuery();
					long now = System.currentTimeMillis();
					long tonkDif = now - lastTonk;
					if (result.next())
					{
						long myTonk = result.getLong(1);
						Helper.sendMessage(target, "Your last Tonk was " + Helper.timeString(Helper.parseMilliseconds(tonkDif), true, 6, true) + " ago!", nick);
					}
					else
					{
						Helper.sendMessage(target, "You're out of Tonk", nick);
					}

//					PreparedStatement set = Database.getPreparedStatement("setUserTonk");
//					set.setString(1, nick);
//					set.setLong(2, now);
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "Something went wrong", nick);
				}
			}
		};
		local_command.setHelpText("Tonk the bot");
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

	@Override
	public void handleMessage(String sender, MessageEvent event, String command, String[] args) {}

	@Override
	public void handleMessage(String nick, GenericMessageEvent event, String command, String[] copyOfRange) {}
}
