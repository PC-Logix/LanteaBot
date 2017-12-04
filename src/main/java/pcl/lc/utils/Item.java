package pcl.lc.utils;

import com.google.api.client.util.DateTime;
import pcl.lc.irc.hooks.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Item class
 * Created by Forecaster on 12/03/2017 for the LanteaBot project.
 */
public class Item {
	private int id;
	private String name;
	private int uses_left;
	private boolean is_favourite;
	private String added_by;
	private int added;

	public Item(String name) throws Exception {
		PreparedStatement statement = Database.getPreparedStatement("getItemByName");
		statement.setString(1, name);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			this.id = resultSet.getInt(1);
			this.name = resultSet.getString(2);
			this.uses_left = resultSet.getInt(3);
			this.is_favourite = resultSet.getBoolean(4);
			this.added_by = resultSet.getString(5);
			this.added = resultSet.getInt(6);
		} else {
			throw new Exception("No item '" + name + "' found");
		}
	}

	public Item(int id) {
		try {
			PreparedStatement statement = Database.getPreparedStatement("getItem");
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				this.id = resultSet.getInt(1);
				this.name = resultSet.getString(2);
				this.uses_left = resultSet.getInt(3);
				this.is_favourite = resultSet.getBoolean(4);
				this.added_by = resultSet.getString(5);
				this.added = resultSet.getInt(6);
			} else {
				throw new Exception("No item '" + name + "' found");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Item(int id, String name, int uses_left, boolean is_favourite, String added_by, int added) {
		this.id = id;
		this.name = name;
		this.uses_left = uses_left;
		this.is_favourite = is_favourite;
		this.added_by = added_by;
		this.added = added;
	}

	public String decrementUses() {
		return damage(1);
	}

	public String decrementUses(boolean includeLeadingComma, boolean capitalizeFirstWord) {
		return damage(1, includeLeadingComma, capitalizeFirstWord);
	}

	public String damage() {
		return damage(1);
	}

	public String damage(boolean includeLeadingComma, boolean capitalizeFirstWord) {
		return damage(1, includeLeadingComma, capitalizeFirstWord);
	}

	public String damage(int damage) {
		return damage(damage, true, false);
	}

	/**
	 * Applies damage to the item. If result is 0 or less item is destroyed unless it's preserved
	 * Returns the 'dust' string to append if the item was destroyed, empty string otherwise. 'Dust' string should be appended at the end of the message to the channel/user
	 * @return String
	 */
	public String damage(int damage, boolean includeLeadingComma, boolean capitalizeFirstWord) {
		if (this.uses_left == -1)
			return "";
		this.uses_left -= damage;
		if (this.uses_left <= 0) {
			int result = Inventory.removeItem(this.id);
			if (result == 0) {
				String sentence = Inventory.getItemBreakString(Inventory.fixItemName(this.name, true));
				if (capitalizeFirstWord)
					sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
				return (includeLeadingComma ? ", " : "") + sentence + ".";
			}
			else
				System.out.println("Error removing item (" + result + ")");
		} else {
			try {
				PreparedStatement statement = Database.getPreparedStatement("setUses");
				statement.setInt(1, this.uses_left);
				statement.setInt(2, this.id);
				statement.executeUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return "";
	}

	public int removeItem() {
		return Inventory.removeItem(this.id);
	}

	public boolean preserve() {
		try {
			PreparedStatement preserveItem = Database.getPreparedStatement("preserveItem");
			preserveItem.setString(1, this.name);
			preserveItem.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean unPpreserve() {
		try {
			PreparedStatement unPreserveItem = Database.getPreparedStatement("unPreserveItem");
			unPreserveItem.setString(1, this.name);
			unPreserveItem.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return getName(false);
	}

	public String getName(boolean sort_out_prefixes) {
		if (this.name == null)
			return "null";
		try {
			return Inventory.fixItemName(this.name, sort_out_prefixes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.name;
	}

	public String getNameRaw() {
		return this.name;
	}

	public String getAdded_by() {
		return this.added_by;
	}

	public int getAddedRaw() {
		return this.added;
	}

	public DateTime getAdded() {
		return new DateTime(this.added);
	}

	public int getUsesLeft() {
		return this.uses_left;
	}

	public String getUsesLeftVague() {
		return Inventory.getUsesIndicator(this.uses_left);
	}

	public boolean isFavourite() {
		return this.is_favourite;
	}
}
