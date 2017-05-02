package pcl.lc.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Collection of Item instances
 * Created by Forecaster on 12/03/2017 for the LanteaBot project.
 */
public class ItemCollection {
	ArrayList<Item> items;
	int count;

	public ItemCollection() {
		this.items = new ArrayList<>();
		this.count = 0;
	}

	public ItemCollection(ArrayList<Item> items) {
		this.items = items;
		this.count = items.size();
	}

	public boolean fillWithUniqueItems(int max_amount) {
		return fillWithUniqueItems(max_amount, false);
	}

	public boolean fillWithUniqueItems(int max_amount, boolean can_be_favourite) {
		boolean found_items = false;
		try {
			PreparedStatement statement;
			if (!can_be_favourite)
				statement = Database.getPreparedStatement("getRandomItemsNonFavourite");
			else
				statement = Database.getPreparedStatement("getRandomItems");
			statement.setInt(1, max_amount);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				found_items = true;
				items.add(new Item(result.getInt(1), result.getString(2), result.getInt(3), result.getBoolean(4), result.getString(5), result.getInt(6)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return found_items;
	}

	public Item getRandomFromCollection() {
		return items.get(Helper.getRandomInt(0, items.size() - 1));
	}

	public String getItemNames() {
		String items = "";
		for (int i = 0; i < this.items.size(); i++) {
			if (i == 0)
				items += this.items.get(i).getName();
			else if (i == (this.items.size() - 1))
				items += " & " + this.items.get(i).getName();
			else
				items += ", " + this.items.get(i).getName();
		}
		return items;
	}

	public int count() {
		return this.items.size();
	}

	public ArrayList<Item> getItems() {
		return this.items;
	}

	public Item pop() {
		this.count--;
		return this.items.remove(items.size() - 1);
	}

	public Item shift() {
		this.count--;
		return this.items.remove(0);
	}
}
