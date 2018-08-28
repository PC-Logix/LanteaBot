package pcl.lc.utils.db_items;

import pcl.lc.utils.DatabaseEntry;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by Forecaster on 02/06/2018 for the LanteaBot project.
 */
public class InventoryItem extends DatabaseEntry {
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

	public InventoryItem() {}

	public InventoryItem(String name, int uses, boolean is_favourite, String added_by, String owner) {
		this.id = Integer.MIN_VALUE;
		this.item_name = name;
		this.uses_left = uses;
		this.is_favourite = is_favourite;
		this.added_by = added_by;
		this.added = (int) new Timestamp(System.currentTimeMillis()).getTime();
		this.owner = owner;
		this.cursed = false;
	}

	public InventoryItem(String name, int uses, boolean is_favourite, String added_by) {
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

	public static ArrayList<InventoryItem> GetAll(boolean can_be_favourite) {
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
		ArrayList<DatabaseEntry> entries = GetManyByField(InventoryItem::new, table, fields, values, null, null);
		ArrayList<InventoryItem> items = new ArrayList<>();
		for (DatabaseEntry entry : entries)
			items.add((InventoryItem) entry);
		return items;
	}

	public static InventoryItem GetByID(int ID) {
		InventoryItem item = new InventoryItem();
		item = (InventoryItem) GetByField(item, table,"id", ID);
		return item;
	}

	public static InventoryItem GetByName(String name) {
		InventoryItem item = new InventoryItem();
		item = (InventoryItem) GetByField(item, table, "item_name", name);
		return item;
	}

	public static ArrayList<InventoryItem> GetRandomItems(int amount) {
		ArrayList<DatabaseEntry> entries = GetManyByField(InventoryItem::new, table, new String[] {"owner"}, new String[] {null}, "RANDOM()", String.valueOf(amount));
		ArrayList<InventoryItem> items = new ArrayList<>();
		for (DatabaseEntry entry : entries)
			items.add((InventoryItem) entry);
		return items;
	}
}
