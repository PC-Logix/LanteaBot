package pcl.lc.irc.hooks;

import pcl.lc.irc.AbstractListener;
import pcl.lc.utils.Database;

public class RPG extends AbstractListener {

	@Override
	protected void initHook() {
		Database.addPreparedStatement("newCharacter", "INSERT INTO RPGUsers (account, health, xp, level, defense, accuracy, numAttacked, numAttacks, deaths, revives) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		Database.addPreparedStatement("updateCharacter", "UPDATE RPGUsers SET health = '?', xp = '?', level = '?', defense = '?', accuracy = '?', numAttacked = '?', numAttacks = '?', deaths = '?', revives = '?' WHERE account = '?'");
		Database.addPreparedStatement("getCharacter", "SELECT * FROM RPGUsers WHERE account = ?");
		//Don't want to add this to the DB yet.
		//Database.addStatement("CREATE TABLE IF NOT EXISTS RPGUsers(account STRING UNIQUE PRIMARY KEY, userName STRING, health, xp, level, defense, accuracy, numAttacked, numAttacks, deaths, revives)");
	}

}
