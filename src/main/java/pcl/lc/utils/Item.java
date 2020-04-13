package pcl.lc.utils;

import com.google.api.client.util.DateTime;
import pcl.lc.irc.IRCBot;
import pcl.lc.irc.hooks.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Item class
 * Created by Forecaster on 12/03/2017 for the LanteaBot project.
 */
public class Item {
	public static int maxItemNameLength = 70;

	private int id;
	private String name;
	private int uses_left;
	private boolean is_favourite;
	private String added_by;
	private int added;
	private String owner;
	private boolean cursed;

	public Item(String name) throws Exception {
		this(name, true);
	}

	public Item(String name, boolean lookup) throws Exception {
		if (lookup) {
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
				this.owner = resultSet.getString(7);
				this.cursed = resultSet.getBoolean(8);
			} else {
				throw new Exception("No item '" + name + "' found");
			}
		} else {
			this.name = name;
			this.uses_left = Inventory.getUsesFromName(name);
			this.is_favourite = false;
			this.added_by = "";
			this.added = (int) new Timestamp(System.currentTimeMillis()).getTime();
			this.owner = IRCBot.getOurNick();
			this.cursed = false;
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
				this.owner = resultSet.getString(7);
				this.cursed = resultSet.getBoolean(8);
			} else {
				throw new Exception("No item '" + name + "' found");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Item(int id, String name, int uses_left, boolean is_favourite, String added_by, int added, String owner, boolean cursed) {
		this.id = id;
		this.name = name;
		this.uses_left = uses_left;
		this.is_favourite = is_favourite;
		this.added_by = added_by;
		this.added = added;
		this.owner = owner;
		this.cursed = cursed;
	}

	public void Save()
	{

	}

	/**
	 * Applies massive damage to item, almost guaranteed to destroy it.
	 * Calls destroy with includeLeadingComma = true, capitalizeFirstWord = false, includeEndPunctuation = true
	 * @return The dust string from the destroyed item
	 */
	public String destroy() {
		return destroy(true, false, true);
	}

	/**
	 * Applies massive damage to item, almost guaranteed to destroy it.
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return The dust string from the destroyed item
	 */
	public String destroy(boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		return damage(999, includeLeadingComma, capitalizeFirstWord, includeEndPunctuation);
	}

	/**
	 * Applies one (1) damage to the item, defaults to includeLeadingComma:true, capitalizeFirstWord:false, includeEndPunctuation:false
	 * @return String
	 */
	public String decrementUses() {
		return damage(1);
	}

	/**
	 * Applies one (1) damage to the item
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return String
	 */
	public String decrementUses(boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		return damage(1, includeLeadingComma, capitalizeFirstWord, includeEndPunctuation);
	}

	/**
	 * Applies one (1) damage to the item
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return String
	 */
	public String damage(boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		return damage(1, includeLeadingComma, capitalizeFirstWord, includeEndPunctuation);
	}

	/**
	 * Applies damage to the item, defaults to includeLeadingComma:true, capitalizeFirstWord:false, includeEndPunctuation:false
	 * @param damage Amount of damage to apply
	 * @return String
	 */
	public String damage(int damage) {
		return damage(damage, true, false, false);
	}

	/**
	 * Applies damage to the item. If result is 0 or less item is destroyed unless it's preserved
	 * Returns the 'dust' string to append if the item was destroyed, empty string otherwise. 'Dust' string should be appended at the end of the message to the channel/user
	 * @param damage Amount of damage to apply
	 * @param includeLeadingComma Whether to begin the sentence with a comma and space
	 * @param capitalizeFirstWord Whether the first word should be capitalized if it isn't already
	 * @param includeEndPunctuation If false, any punctuation at the end of the sentence will be cleared
	 * @return String
	 */
	public String damage(int damage, boolean includeLeadingComma, boolean capitalizeFirstWord, boolean includeEndPunctuation) {
		if (this.uses_left == -1)
			return "";
		this.uses_left -= damage;
		int inventory_size_penalty = (int) Math.floor(Inventory.getInventorySize() / 15d);
		System.out.println("inventory_size_penalty: " + inventory_size_penalty);
		if ((this.uses_left - inventory_size_penalty) <= 0) {
			int result = Inventory.removeItem(this.id);
			if (result == 0) {
				String sentence = Inventory.getItemBreakString(Inventory.fixItemName(this.name, true), includeEndPunctuation);
				if (capitalizeFirstWord)
					sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
				return (includeLeadingComma ? ", " : "") + sentence;
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

	public void addUses(int uses) {
		this.uses_left += uses;
		try {
			PreparedStatement statement = Database.getPreparedStatement("setUses");
			statement.setInt(1, this.uses_left);
			statement.setInt(2, this.id);
			statement.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void incrementUses() {
		addUses(1);
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

	public Item setName(String new_name) {
		this.name = new_name;
		return this;
	}

	public String getNameWithoutPrefix() {
		if (this.name == null)
			return "null";
		try {
			return Inventory.fixItemName(this.name, true, true);
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

	public DiceRollResult getGenericRoll()
	{
		return getGenericRoll(new DiceRollBonusCollection());
	}

	public DiceRollResult getGenericRoll(DiceRollBonusCollection bonus)
	{
		return getGenericRoll(1, getDiceSizeFromItemName(), bonus);
	}

	public static DiceRollResult getGenericRoll(int diceAmount, int diceSize, DiceRollBonusCollection bonus)
	{
		return getGenericRoll(diceAmount, diceSize, bonus, 0);
	}

	public static DiceRollResult getGenericRoll(int diceAmount, int diceSize, DiceRollBonusCollection bonus, int minValue)
	{
		DiceRollResult result = new DiceRollResult();
		int diceRoll = Helper.rollDice(diceAmount + "d" + diceSize).getSum();
		result.rollResult = diceRoll;
		result.bonus = bonus;
		result.minValue = minValue; //Minimum Value
		result.diceAmount = diceAmount;
		result.diceSize = diceSize;
		return result;
	}

	/**
	 * Default dice size 4
	 * @return int
	 */
	public DiceRollResult getDamage()
	{
		return getDamage(1, getDiceSizeFromItemName());
	}

	public DiceRollResult getDamage(int diceAmount, int diceSize)
	{
		return getDamage(diceAmount, diceSize, 0);
	}

	public DiceRollResult getDamage(int diceAmount, int diceSize, int minDamage)
	{
		return getGenericRoll(diceAmount, diceSize, DiceRollBonusCollection.getOffensiveItemBonus(this), minDamage);
	}

	/**
	 * Default dice size 4
	 * @return int
	 */
	public DiceRollResult getDamageRecution()
	{
		return getDamageReduction(1, getDiceSizeFromItemName());
	}

	public DiceRollResult getDamageReduction(int diceAmount, int diceSize)
	{
		return getDamageReduction(diceAmount, diceSize, 0);
	}

	public DiceRollResult getDamageReduction(int diceAmount, int diceSize, Integer minDamageReduction)
	{
		return getGenericRoll(diceAmount, diceSize, DiceRollBonusCollection.getDefensiveItemBonus(this), minDamageReduction);
	}

	/**
	 * Default dice size 4
	 * @return int
	 */
	public DiceRollResult getHealing()
	{
		return getHealing(1, getDiceSizeFromItemName());
	}

	public DiceRollResult getHealing(int diceAmount, int diceSize)
	{
		return getHealing(diceAmount, diceSize, 0);
	}

	public DiceRollResult getHealing(int diceAmount, int diceSize, int minHealing)
	{
		return getGenericRoll(diceAmount, diceSize, DiceRollBonusCollection.getHealingItemBonus(this), minHealing);
	}

	/**
	 * "{damage} damage (Minimum|{diceRoll}+|-{bonus})"
	 * @param input DiceRollResult The result from one of the get____Roll methods
	 * @return String
	 */
	public static String stringifyDamageResult(DiceRollResult input)
	{
		if (input.rollResult == 0)
			return "no damage" + getParenthesis(input);
		return input.rollResult + " damage" + getParenthesis(input);
	}

	/**
	 * "damage reduced by {reduction} (Minimum|{diceRoll}+-{bonus}"
	 * or
	 * "no damage reduction (Incapable|{diceRoll}+-{bonus})"
	 * @param input int[] The result array from one of the get____Roll methods
	 * @return String
	 */
	public static String stringifyDamageReductionResult(DiceRollResult input)
	{
		if (input.rollResult == 0)
			return "no damage reduction" + getParenthesis(input);
		return "damage reduced by " + input.rollResult + getParenthesis(input);
	}

	/**
	 * "gained {health} health (Minimum|{diceRoll}+-{bonus})"
	 * or
	 * "no health gained (Incapable|{diceRoll}+-{bonus})
	 * @param input int[] The result array from one of the get____Roll methods
	 * @return String
	 */
	public static String stringifyHealingResult(DiceRollResult input)
	{
		if (input.rollResult == 0)
			return "no health gained" + getParenthesis(input);
		return input.rollResult + " health gained" + getParenthesis(input);
	}

	public static String getParenthesis(DiceRollResult input)
	{
		if (input.bonus.size() == 0)
			return "";
		if (input.bonus.incapable)
			return " (Incapable)";
		if (input.rollResult == input.minValue)
			return " (min)";
		return " (" + input.rollResult + (input.bonus.size() < 0 ? "" : "+") + input.bonus + ")";
	}

	public static int getDiceSizeFromItemName(String itemName) {
		double diceSize = Math.floor(((double)itemName.length() / (double)maxItemNameLength) * 6) * 2;
		System.out.println("DiceSize: " + diceSize);
		return Math.max(4, (int)diceSize);
	}

	public int getDiceSizeFromItemName() {
		return getDiceSizeFromItemName(this.name);
	}
}
