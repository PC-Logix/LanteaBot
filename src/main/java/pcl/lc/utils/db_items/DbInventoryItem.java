package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Forecaster on 02/06/2018 for the LanteaBot project.
 */
public class DbInventoryItem extends DatabaseEntry {
	public static String table = "Inventory";
	public static String primary_key = "id";

	public int id;
	public String item_name;
	public int uses_left;
	public boolean is_favourite;
	public String added_by;
	public int added;
	public String owner;
	public boolean cursed;

	public DbInventoryItem() {}

	public DbInventoryItem(String name, int uses, boolean is_favourite, String added_by, String owner) {
		this.id = Integer.MIN_VALUE;
		this.item_name = name;
		this.uses_left = uses;
		this.is_favourite = is_favourite;
		this.added_by = added_by;
		this.added = (int) new Timestamp(System.currentTimeMillis()).getTime();
		this.owner = owner;
		this.cursed = false;
	}

	public DbInventoryItem(String name, int uses, boolean is_favourite, String added_by) {
		this.id = Integer.MIN_VALUE;
		this.item_name = name;
		this.uses_left = uses;
		this.is_favourite = is_favourite;
		this.added_by = added_by;
		this.added = (int) new Timestamp(System.currentTimeMillis()).getTime();
		this.owner = null;
		this.cursed = false;
	}

	@Override
	public String toString() {
		return this.item_name;
	}

	public boolean Save() {
		return super.Save(table);
	}

	public boolean Delete() {
		return super.Delete(table, primary_key);
	}

	public static DbInventoryItemCollection GetAll(boolean can_be_favourite) {
		String[] fields;
		Object[] values;
		if (can_be_favourite) {
			fields = new String[]{
				"owner"
			};
			values = new Object[]{
				null
			};
		} else {
			fields = new String[] {
				"owner",
				"is_favourite"
			};
			values = new Object[] {
				null,
				false
			};
		}
		DbInventoryItemCollection collection = new DbInventoryItemCollection();
		GetManyByField(collection, DbInventoryItem::new, table, fields, values);
		return collection;
	}

	public static DbInventoryItem GetByID(int ID) {
		DbInventoryItem item = new DbInventoryItem();
		item = (DbInventoryItem) GetByField(item, table,"id", ID);
		return item;
	}

	public static DbInventoryItem GetByName(String name) {
		DbInventoryItem item = new DbInventoryItem();
		item = (DbInventoryItem) GetByField(item, table, "item_name", name);
		return item;
	}

	public static DbInventoryItemCollection GetRandomItems(int amount) {
		DbInventoryItemCollection collection = new DbInventoryItemCollection();
		GetManyByField(collection, DbInventoryItem::new, table, new String[] {"owner"}, new String[] {null}, "RANDOM()", String.valueOf(amount));
		return collection;
	}
}
