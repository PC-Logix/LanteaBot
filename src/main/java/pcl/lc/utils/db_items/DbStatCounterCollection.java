package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;
import pcl.lc.utils.DatabaseEntryCollection;

import java.util.ArrayList;

public class DbStatCounterCollection extends DatabaseEntryCollection {
	public ArrayList<DbStatCounter> items;

	public DbStatCounterCollection() {
		items = new ArrayList<>();
	}

	@Override
	public DbStatCounter FirstOrNull() {
		try {
			return this.items.get(0);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public void AddItem(DatabaseEntry item) {
		items.add((DbStatCounter) item);
	}
}
