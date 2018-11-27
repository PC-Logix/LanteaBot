package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.Permissions;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import java.util.Date;

/**
 * A module for Tonking
 * Created by Forecaster on 30/03/2017 for the LanteaBot project.
 */
public class Tonk extends AbstractListener {
	private Command local_command;
	private Command reset_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command);
		IRCBot.registerCommand(reset_command);
	}

	private void initCommands() {
		local_command = new Command("tonk", 60) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String tonkin = Database.getJsonData("lasttonk");
				String tonk_record = Database.getJsonData("tonkrecord");
				long now = new Date().getTime();

				if (tonkin == "" || tonk_record == "") {
					Helper.sendMessage(target, "You got the first Tonk " + nick + ", but this is only the beginning.");
					Database.storeJsonData("tonkrecord", "0;" + nick);
				} else {
					long lasttonk = 0;
					try {
						lasttonk = Long.parseLong(tonkin);
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}

					long diff = now - lasttonk;

					try {
						String tonk[] = tonk_record.split(";");
						long tonk_record_long = Long.parseLong(tonk[0]);
						String recorder = tonk[1].trim();

						if (nick.equals(recorder)) {
							Helper.sendMessage(target, "You still hold the record " + nick + ", for now... " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)));
						} else if (tonk_record_long < diff) {
							System.out.println("New record");
							System.out.println("'" + recorder + "' == '" + nick + "' => " + (nick.equals(recorder) ? "true" : "false"));

							Helper.sendMessage(target, nick + "! You beat " + (nick.equals(recorder) ? "your own" : recorder + "'s") + " previous record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + "! I hope you're happy!");
							Helper.sendMessage(target, nick + "'s new record is " + Helper.timeString(Helper.parseMilliseconds(diff)));
							Database.storeJsonData("tonkrecord", diff + ";" + nick);
							Database.storeJsonData("lasttonk", String.valueOf(now));
						} else {
							System.out.println("No record");
							Helper.sendMessage(target, "I'm sorry " + nick + ", you were not able to beat " + (nick.equals(recorder) ? "your own" : recorder + "'s") + " record of " + Helper.timeString(Helper.parseMilliseconds(tonk_record_long)) + " this time.");
							Helper.sendMessage(target, Helper.timeString(Helper.parseMilliseconds(diff)) + " were wasted!");
							Database.storeJsonData("lasttonk", String.valueOf(now));
						}
					} catch (Exception ex) {
						System.out.println(ex.getClass() + ": " + ex.getMessage());
					}
				}
			}
		};
		local_command.setHelpText("What is tonk? Tonk is life.");
		
		reset_command = new Command("resettonk", 60, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				long now = new Date().getTime();
				Helper.sendMessage(target, "Tonk reset " + nick + ", you are the record holder!");
				Database.storeJsonData("tonkrecord", "0;" + nick);
				Database.storeJsonData("lasttonk", String.valueOf(now));
			}
		};
		reset_command.setHelpText("What is tonk? Tonk is life.");
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
		reset_command.tryExecute(command, nick, target, event, copyOfRange);
	}
}
