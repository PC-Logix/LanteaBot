package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;

import javax.annotation.Nullable;

public class DbStatCounter extends DatabaseEntry {
	public static String table = "Statistics";
	public static String primary_key = "id";

	public int id;
	public String group;
	public String key;
	public double count = 0;

	public boolean Save() {
		return super.Save(table);
	}

	public boolean Delete() {
		return super.Delete(table, primary_key);
	}

	public DbStatCounter() {}

	public DbStatCounter(String group, String key) {
		this.group = group;
		this.key = key;
	}

	public static DbStatCounterCollection GetByKey(String key) {
		return (DbStatCounterCollection) GetManyByField(new DbStatCounterCollection(), DbStatCounter::new, table, new String[] { "key" }, new String[] { key });
	}

	public static DbStatCounterCollection GetByGroup(String group) {
		return (DbStatCounterCollection) GetManyByField(new DbStatCounterCollection(), DbStatCounter::new, table, new String[] { "group" }, new String[] { group });
	}

	public static DbStatCounterCollection GetByGroupAndKey(String group, String key) {
		return (DbStatCounterCollection) GetManyByField(new DbStatCounterCollection(), DbStatCounter::new, table, new String[] { "group", "key" }, new String[] { group, key });
	}

	public static int Count(String group) {
		DbStatCounterCollection items = (DbStatCounterCollection) GetManyByField(new DbStatCounterCollection(), DbStatCounter::new, table, new String[] { "group" }, new String[] { group });
		int count = 0;
		for (DbStatCounter item : items.items) {
			count += item.count;
		}
		return count;
	}

	public static int Count(String group, String key) {
		DbStatCounterCollection items = (DbStatCounterCollection) GetManyByField(new DbStatCounterCollection(), DbStatCounter::new, table, new String[] { "group", "key" }, new String[] { group, key });
		int count = 0;
		for (DbStatCounter item : items.items) {
			count += item.count;
		}
		return count;
	}

	public static boolean Increment(String group, String key) {
		return Increment(group, key, 1);
	}

	public static boolean Increment(String group, String key, int amount) {
		DbStatCounter item = GetByGroupAndKey(group, key).FirstOrNull();
		System.out.println("Stat item:");
		System.out.println(item);
		if (item == null)
			item = new DbStatCounter(group, key);
		item.count += amount;
		return item.Save();
	}
}
