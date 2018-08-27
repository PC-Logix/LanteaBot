package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;

/**
 * Created by Forecaster on 06/06/2018 for the LanteaBot project.
 */
public class CommandItem extends DatabaseEntry {
	public static String table = "Commands";
	public static String primary_key = "command";

	public String command;
	public String return_value;
	public String help;

	public CommandItem() {}

	public CommandItem(String command, String return_value, String help) {
		this.command = command;
		this.return_value = return_value;
		this.help = help;
	}

	public boolean Save() {
		return super.Save(table);
	}

	public boolean Delete() {
		return super.Delete(table, primary_key);
	}

	public static CommandItem GetByCommand(String command) {
		CommandItem item = new CommandItem();
		item = (CommandItem)GetByField(item, table, primary_key, command);
		return item;
	}
}
