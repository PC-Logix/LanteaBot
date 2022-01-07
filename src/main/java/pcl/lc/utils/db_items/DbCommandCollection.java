package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;
import pcl.lc.utils.DatabaseEntryCollection;

import java.util.ArrayList;

public class DbCommandCollection extends DatabaseEntryCollection {
	public ArrayList<DbCommand> items;

	public DbCommandCollection() {
		items = new ArrayList<>();
	}

	@Override
	public DbCommand FirstOrNull() {
		try {
			return this.items.get(0);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public void AddItem(DatabaseEntry item) {
		items.add((DbCommand) item);
	}
}
