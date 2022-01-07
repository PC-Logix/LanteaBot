package pcl.lc.utils;

import java.util.ArrayList;

public abstract class DatabaseEntryCollection {
	public ArrayList<DatabaseEntry> items;

	public DatabaseEntryCollection() {
		items = new ArrayList<>();
	}

	public DatabaseEntry FirstOrNull() {
		if (this.items.size() > 0)
			return this.items.get(0);
		return null;
	}

	public void AddItem(DatabaseEntry item) {
		items.add(item);
	}

	public boolean SaveAll() {
		boolean result = true;
		for (DatabaseEntry item : items) {
			if (!item.Save(DatabaseEntry.table))
				result = false;
		}
		return result;
	}

	public boolean DeleteAll() {
		boolean result = true;
		for (DatabaseEntry item : items) {
			if (!item.Delete(DatabaseEntry.table, DatabaseEntry.primary_key))
				result = false;
		}
		return result;
	}
}
