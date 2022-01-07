package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;
import pcl.lc.utils.DatabaseEntryCollection;

import java.util.ArrayList;

public class DbInventoryItemCollection extends DatabaseEntryCollection {
	public ArrayList<DbInventoryItem> items;

	public DbInventoryItemCollection() {
		items = new ArrayList<>();
	}

	@Override
	public DbInventoryItem FirstOrNull() {
		try {
			return this.items.get(0);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public void AddItem(DatabaseEntry item) {
		items.add((DbInventoryItem) item);
	}
}
