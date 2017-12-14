package pcl.lc.irc.hooks;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import pcl.lc.irc.AbstractListener;
import pcl.lc.irc.Command;
import pcl.lc.irc.Permissions;
import pcl.lc.irc.RPGCharacter;
import pcl.lc.utils.Account;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

public class RPG extends AbstractListener {
	private Command rpg;
	private Command stats;
	private Command xpup;
	private Command strength;
	private Command defense;
	private Command accuracy;
	private Command dodge;
	private Command status;

	@Override
	protected void initHook() {
		//Don't want to add this to the DB yet.
//		Database.addStatement("CREATE TABLE IF NOT EXISTS RPGUsers(account STRING UNIQUE PRIMARY KEY," +
//			"userName STRING," +
//			"health DOUBLE," +
//			"xp INT," +
//			"level INT," +
//			"strength INT," +
//			"defense INT," +
//			"accuracy INT," +
//			"dodge INT," +
//			"gainStrength INT," +
//			"gainDefense INT," +
//			"gainAccuracy INT," +
//			"gainDodge INT," +
//			"numAttacked INT," +
//			"numAttacks INT," +
//			"deaths INT," +
//			"revives INT)");
		Database.addPreparedStatement("newRPGCharacter", "INSERT INTO RPGUsers (account, userName, health, xp, level, strength, defense, accuracy, dodge, gainStrength, gainDefense, gainAccuracy, gainDodge, numAttacked, numAttacks, deaths, revives)" +
			" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		Database.addPreparedStatement("updateRPGCharacter", "UPDATE RPGUsers SET userName = ?, " +
			"health = ?, " +
			"xp = ?, " +
			"level = ?, " +
			"strength = ?, " +
			"defense = ?, " +
			"accuracy = ?, " +
			"dodge = ?, " +
			"gainStrength = ?, " +
			"gainDefense = ?, " +
			"gainAccuracy = ?, " +
			"gainDodge = ?, " +
			"numAttacked = ?, " +
			"numAttacks = ?, " +
			"deaths = ?, " +
			"revives = ? WHERE account = ?");
		Database.addPreparedStatement("getRPGCharacter", "SELECT * FROM RPGUsers WHERE account = ?");

		rpg = new Command("rpg", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				super.onExecuteSuccess(command, nick, target, event, params);
			}
		};

		stats = new Command("stats", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					Helper.sendMessage(target, character.toString());
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(stats);

		xpup = new Command("xpup", 0, Permissions.ADMIN) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					character.gainExperience(1);
					character.levelUp();
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(xpup);

		strength = new Command("strength", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					int gain = character.applyStrength();
					if (gain > 0)
						Helper.sendMessage(target, "You gained " + gain + " strength! You now have " + character.getStrength());
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(strength);

		defense = new Command("defense", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					int gain = character.applyDefense();
					if (gain > 0)
						Helper.sendMessage(target, "You gained " + gain + " defense! You now have " + character.getDefense());
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(defense);

		accuracy = new Command("accuracy", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					int gain = character.applyAccuracy();
					if (gain > 0)
						Helper.sendMessage(target, "You gained " + gain + " accuracy! You now have " + character.getAccuracy());
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(accuracy);

		dodge = new Command("dodge", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					int gain = character.applyDodge();
					if (gain > 0)
						Helper.sendMessage(target, "You gained " + gain + " dodge! You now have " + character.getDodge());
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(dodge);

		status = new Command("status", 0, Permissions.EVERYONE) {
			@Override
			public void onExecuteSuccess(Command command, String nick, String target, GenericMessageEvent event, String params) {
				String account = Account.getAccount(nick, event);
				try {
					RPGCharacter character = new RPGCharacter(account, nick, target);
					Helper.sendMessage(target, "Your status is \"" + character.getStatus(true) + "\"");
				} catch (Exception e) {
					e.printStackTrace();
					Helper.sendMessage(target, "I couldn't get the character.");
				}
			}
		};
		rpg.registerSubCommand(status);
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
		rpg.tryExecute(command, nick, target, event, copyOfRange);
	}
}
