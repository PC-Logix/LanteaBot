package pcl.lc.irc.hooks;

import pcl.lc.irc.AbstractListener;
import pcl.lc.utils.Database;

public class RPG extends AbstractListener {

	@Override
	protected void initHook() {
		//Don't want to add this to the DB yet.
		//Database.addStatement("CREATE TABLE IF NOT EXISTS RPGUsers(account STRING UNIQUE PRIMARY KEY, health, xp, defense, accuracy, numattacked, numattacks, deaths, revives)");
	}

}
