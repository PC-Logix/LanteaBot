package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.IRCBot;
import pcl.lc.utils.Helper;

/**
 * @author Forecaster
 * Based on this xkcd
 * https://xkcd.com/1930/
 */
@SuppressWarnings("rawtypes")
public class CalendarFacts extends AbstractListener {
	private Command local_command;

	@Override
	protected void initHook() {
		initCommands();
		IRCBot.registerCommand(local_command, "Calendar facts based on xkcd comic 1930");
	}

	private String getEntryFromArray(String[] array) {
		int i = Helper.getRandomInt(0, array.length - 1);
		return array[i];
	}

	private void initCommands() {
		local_command = new Command("calendarfacts", 0) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String str = "Did you know that ";
				//<editor-fold desc="BLOCK ONE">
				int randomInt = Helper.getRandomInt(0, 5);
				switch (randomInt) {
					case 0:
						str += "the ";
						randomInt = Helper.getRandomInt(0, 3);
						switch (randomInt) {
							case 0:
								randomInt = Helper.getRandomInt(0, 1);
								switch (randomInt) {
									case 0:
										str += "fall ";
										break;
									case 1:
										str += "spring ";
										break;
								}
								str += "equinox ";
								break;
							case 1:
								randomInt = Helper.getRandomInt(0, 1);
								switch (randomInt) {
									case 0:
										str += "winter ";
										break;
									case 1:
										str += "summer ";
										break;
								}
								randomInt = Helper.getRandomInt(0, 1);
								switch (randomInt) {
									case 0:
										str += "solstice ";
										break;
									case 1:
										str += "olympics ";
										break;
								}
								break;
							case 2:
								randomInt = Helper.getRandomInt(0, 1);
								switch (randomInt) {
									case 0:
										str += "earliest ";
										break;
									case 1:
										str += "latest ";
										break;
								}
								randomInt = Helper.getRandomInt(0, 1);
								switch (randomInt) {
									case 0:
										str += "sunrise ";
										break;
									case 1:
										str += "sunset ";
										break;
								}
								break;
							case 3:
								randomInt = Helper.getRandomInt(0, 2);
								switch (randomInt) {
									case 0:
										str += "harvest ";
										break;
									case 1:
										str += "super ";
										break;
									case 2:
										str += "blood ";
										break;
								}
								str += "moon ";
								break;
						}
						break;
					case 1:
						str += "daylight ";
						randomInt = Helper.getRandomInt(0, 1);
						switch (randomInt) {
							case 0:
								str += "saving ";
								break;
							case 1:
								str += "savings ";
								break;
						}
						str += "time ";
						break;
					case 2:
						str += "leap ";
						randomInt = Helper.getRandomInt(0, 1);
						switch (randomInt) {
							case 0:
								str += "day ";
								break;
							case 1:
								str += "year ";
								break;
						}
						break;
					case 3:
						str += "Toyota Truck Month ";
						break;
					case 4:
						str += "Easter ";
						break;
					case 5:
						str += "Shark Week ";
						break;
				}
				//</editor-fold>

				//<editor-fold desc="BLOCK TWO">
				randomInt = Helper.getRandomInt(0, 2);
				switch (randomInt) {
					case 0:
						str += "happens ";
						randomInt = Helper.getRandomInt(0, 2);
						switch (randomInt) {
							case 0:
								str += "earlier ";
								break;
							case 1:
								str += "later ";
								break;
							case 2:
								str += "at the wrong time ";
								break;
						}
						str += "every year ";
						break;
					case 1:
						str += "drifts out of sync with the ";
						randomInt = Helper.getRandomInt(0, 2);
						switch (randomInt) {
							case 0:
								randomInt = Helper.getRandomInt(0, 2);
								switch (randomInt) {
									case 0:
										str += "sun ";
										break;
									case 1:
										str += "moon ";
										break;
									case 2:
										str += "zodiac ";
										break;
								}
								break;
							case 1:
								randomInt = Helper.getRandomInt(0, 3);
								switch (randomInt) {
									case 0:
										str += "gregorian ";
										break;
									case 1:
										str += "mayan ";
										break;
									case 2:
										str += "lunar ";
										break;
									case 3:
										str += "iPhone ";
										break;
								}
								str += "calendar ";
								break;
							case 2:
								str += "atomic clock in Colorado ";
								break;
						}
						break;
					case 2:
						str += "might ";
						randomInt = Helper.getRandomInt(0, 1);
						switch (randomInt) {
							case 0:
								str += "not happen ";
								break;
							case 1:
								str += "happen twice ";
								break;
						}
						str += "this year ";
						break;
				}
				//</editor-fold>

				//<editor-fold desc="BLOCK THREE">
				str += "because of ";
				randomInt = Helper.getRandomInt(0, 4);
				switch (randomInt) {
					case 0:
						str += "time zone legislation in ";
						randomInt = Helper.getRandomInt(0, 2);
						switch (randomInt) {
							case 0:
								str += "Indiana";
								break;
							case 1:
								str += "Arizona";
								break;
							case 2:
								str += "Russia";
								break;
						}
						break;
					case 1:
						str += "a decree by the pope in the 1500s";
						break;
					case 2:
						randomInt = Helper.getRandomInt(0, 5);
						switch (randomInt) {
							case 0:
								str += "precession ";
								break;
							case 1:
								str += "liberation ";
								break;
							case 2:
								str += "nutation ";
								break;
							case 3:
								str += "libation ";
								break;
							case 4:
								str += "eccentricity ";
								break;
							case 5:
								str += "obliquity ";
								break;
						}
						str += "of the ";
						randomInt = Helper.getRandomInt(0, 5);
						switch (randomInt) {
							case 0:
								str += "moon";
								break;
							case 1:
								str += "sun";
								break;
							case 2:
								str += "Earth's axis";
								break;
							case 3:
								str += "equator";
								break;
							case 4:
								str += "prime meridian";
								break;
							case 5:
								randomInt = Helper.getRandomInt(0, 1);
								switch (randomInt) {
									case 0:
										str += "international date ";
										break;
									case 1:
										str += "mason-dixon ";
										break;
								}
								str += "line";
						}
					case 3:
						str += "magnetic field reversal";
						break;
					case 4:
						str += "an arbitrary decition by ";
						randomInt = Helper.getRandomInt(0, 2);
						switch (randomInt) {
							case 0:
								str += "Benjamin Franklin";
								break;
							case 1:
								str += "Isaac Newton";
								break;
							case 2:
								str += "FDR";
								break;
						}
				}
				str += "? ";
				//</editor-fold>

				//<editor-fold desc="BLOCK FOUR">
				str += "Apparently ";
				randomInt = Helper.getRandomInt(0, 5);
				switch (randomInt) {
					case 0:
						str += "it causes a predictable increase in car accidents.";
						break;
					case 1:
						str += "that's why we have leap seconds.";
						break;
					case 2:
						str += "scientists are really worried.";
						break;
					case 3:
						str += "it was even more extreme during the ";
						randomInt = Helper.getRandomInt(0, 3);
						switch (randomInt) {
							case 0:
								str += "bronze age.";
								break;
							case 1:
								str += "ice age.";
								break;
							case 2:
								str += "createous.";
								break;
							case 3:
								str += "1990s.";
								break;
						}
						break;
					case 4:
						str += "there's a proposal to fix it, but it ";
						randomInt = Helper.getRandomInt(0, 3);
						switch (randomInt) {
							case 0:
								str += "will never happen.";
								break;
							case 1:
								str += "actually makes things worse.";
								break;
							case 2:
								str += "is stalled in congress.";
								break;
							case 3:
								str += "might be unconstitutional.";
								break;
						}
						break;
					case 5:
						str += "it's getting worse and no one knows why.";
						break;
				}
				//</editor-fold>
				Helper.sendMessage(target, str, null, false);
			}
		};
		local_command.registerAlias("calfacts");
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
