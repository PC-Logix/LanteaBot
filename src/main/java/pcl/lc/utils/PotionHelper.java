package pcl.lc.utils;

import gcardone.junidecode.App;
import org.joda.time.DateTime;
import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.irc.hooks.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PotionHelper {
	/**
	 * @return String[] Returns three values: consistency, appearance and "" or "new" (whether potion has been generated already today)
	 */
	public static PotionEntry getRandomPotion() {
		int coli = getRandomAppearanceIndex();
		int coni = getRandomConsistencyIndex();
		AppearanceEntry app = getAppearance(coli);
		AppearanceEntry con = getConsistency(coni);
		return new PotionEntry(con, app, !PotionHelper.combinationHasEffect(con, app));
	}

	public static void tryResetPotionList() {
		if (DrinkPotion.day_of_potioning == null || DrinkPotion.day_of_potioning.equals("") || DateTime.parse(DrinkPotion.day_of_potioning).isBefore(DateTime.now())) {
			resetPotionList();
		}
	}

	public static void resetPotionList() {
		System.out.println("Resetting potion list!");
		DrinkPotion.potions = new HashMap<>();
		DrinkPotion.day_of_potioning = DateTime.now().plusDays(DrinkPotion.daysPotionsLast).toString("yyyy-MM-dd");
	}

	public static boolean combinationHasEffect(AppearanceEntry consistency, AppearanceEntry appearance) {
		tryResetPotionList();
		String key = getConsistencyIndexByName(consistency.getName()) + "," + getAppearanceIndexByName(appearance.getName());
		if (DrinkPotion.potions.containsKey(key))
			return true;
		return false;
	}

	public static void setCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance, EffectEntry effect) {
		String key = getConsistencyIndexByName(consistency.getName()) + "," + getAppearanceIndexByName(appearance.getName());
		DrinkPotion.potions.put(key, effect);
	}

	public static EffectEntry getCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance) {
		String key = getConsistencyIndexByName(consistency.getName()) + "," + getAppearanceIndexByName(appearance.getName());
		return DrinkPotion.potions.get(key);
	}

	public static AppearanceEntry findAppearanceInString(ArrayList<String> string) {
		return findAppearanceInString(String.join(" ", string));
	}

	public static AppearanceEntry findAppearanceInString(String string) {
		string = Helper.reverseString(string).toLowerCase();
		System.out.println(string);
		for (AppearanceEntry c : DrinkPotion.appearanceEntries) {
			if (string.contains(c.getName(false, true)))
				return c;
		}
		return null;
	}

	public static AppearanceEntry findConsistencyInString(ArrayList<String> string) {
		return findConsistencyInString(String.join(" ", string));
	}

	public static AppearanceEntry findConsistencyInString(String string) {
		string = Helper.reverseString(string).toLowerCase();
		System.out.println(string);
		for (AppearanceEntry c : DrinkPotion.consistencies) {
			if (string.contains(c.getName(false, true)))
				return c;
		}
		return null;
	}

	public static int getRandomAppearanceIndex() {
		return Helper.getRandomInt(0, DrinkPotion.appearanceEntries.size() - 1);
	}

	public static AppearanceEntry getAppearance() {
		return getAppearance(getRandomAppearanceIndex());
	}

	public static AppearanceEntry getAppearance(int index) {
		return DrinkPotion.appearanceEntries.get(index);
	}

	public static int getRandomConsistencyIndex() {
		return Helper.getRandomInt(0, DrinkPotion.consistencies.size() - 1);
	}

	public static AppearanceEntry getConsistency() {
		return getConsistency(getRandomConsistencyIndex());
	}

	public static AppearanceEntry getConsistency(int index) {
		return DrinkPotion.consistencies.get(index);
	}

	public static int getRandomLimitIndex() {
		return Helper.getRandomInt(0, DrinkPotion.limits.size() - 1);
	}

	public static String getLimit() {
		return getLimit(getRandomLimitIndex());
	}

	public static String getLimit(int index) {
		return DrinkPotion.limits.get(index);
	}

	public static int getAppearanceIndexByName(String name) {
		for (int i = 0; i < DrinkPotion.appearanceEntries.size(); i++) {
			AppearanceEntry e = DrinkPotion.appearanceEntries.get(i);
			if (e.getName().toLowerCase().equals(name.toLowerCase()))
				return i;
		}
		return -1;
	}

	public static int getConsistencyIndexByName(String name) {
		for (int i = 0; i < DrinkPotion.consistencies.size(); i++) {
			AppearanceEntry e = DrinkPotion.consistencies.get(i);
			if (e.getName().toLowerCase().equals(name.toLowerCase()))
				return i;
		}
		return -1;
	}

	public static String replaceParamsInEffectString(String effect) {
		return replaceParamsInEffectString(effect, null);
	}

	/**
	 * @param effect The effect string to replace {user} and {trigger} tags within
	 * @param targetName The name of the target of the effect
	 * @return Returns the effect string with name inserted
	 */
	public static String replaceParamsInEffectString(String effect, String targetName) {
		return replaceParamsInEffectString(effect, targetName, null);
	}

	public static String replaceParamsInEffectString(String effect, String targetName, String triggererName) {
		Item item = Inventory.getRandomItem();
		String itemName;
		if (item == null)
			itemName = Helper.getRandomGarbageItem();
		else
			itemName = item.getNameRaw();

		Pattern evadePattern = Pattern.compile("\\{evade:(\\d+):(\\d+d\\d+)}");
		Matcher evadeMatcher = evadePattern.matcher(effect);
		if (evadeMatcher.find()) {
			DiceTest test = new DiceTest(Integer.parseInt(evadeMatcher.group(1)), "They successfully evaded it with a {result} vs DC {DC}!", "They fail to evade it with a {result} vs DC {DC} and takes {damage} damage.");
			String damage = evadeMatcher.group(2);
			test.doCheck();
			effect = Helper.replaceSubstring(effect, test.getLine().replace("{damage}", damage), evadeMatcher.start(), evadeMatcher.end());
		}

		effect = DiceRoll.rollDiceInString(effect, true);

		if (targetName != null)
			effect = effect.replaceAll("\\{user}", targetName);

		if (triggererName != null)
			effect = effect.replaceAll("\\{trigger}", triggererName);

		String junkoritem = Inventory.getRandomItem().getNameWithoutPrefix();
		if (Helper.getRandomInt(0, 1) == 1)
			junkoritem = Helper.getRandomGarbageItem(false, true);

		return effect
        .replace("{item}", itemName)
        .replace("{junk_or_item}", junkoritem)
        .replace("{appearance}", PotionHelper.getAppearance().getName(false, false))
        .replace("{appearance_lc}", PotionHelper.getAppearance().getName(false, true))
				.replace("{appearance_p}", PotionHelper.getAppearance().getName(true, false))
				.replace("{appearance_p_lc}", PotionHelper.getAppearance().getName(true, true))
				.replace("{turn_appearance}", PotionHelper.getAppearance().turnsTo())
				.replace("{turn_appearance_lc}", PotionHelper.getAppearance().turnsTo(true))
				.replace("{consistency}", PotionHelper.getConsistency().getName(false, false))
				.replace("{consistency_lc}", PotionHelper.getConsistency().getName(false, true))
				.replace("{consistency_p}", PotionHelper.getConsistency().getName(true, false))
				.replace("{consistency_p_lc}", PotionHelper.getConsistency().getName(true, true))
				.replace("{transformation}", Helper.getRandomTransformation(true, false, false, true))
				.replace("{transformation_p}", Helper.getRandomTransformation(true, true, false, true))
				.replace("{transformation_pc}", Helper.getRandomTransformation(true, true, false, false))
				.replace("{transformation2}", Helper.getRandomTransformation(true, false, false, true))
				.replace("{transformations}", Helper.getRandomTransformation(true, true, true, true))
				.replace("{transformations_p}", Helper.getRandomTransformation(true, false, true, true))
				.replace("{transformations2}", Helper.getRandomTransformation(true, false, true, true))
				.replace("{junk}", Helper.getRandomGarbageItem(false, true))
				.replace("{junk_p}", Helper.getRandomGarbageItem(true, true))
				.replace("{limit}", PotionHelper.getLimit())
				.replace("{codeword}", Helper.getRandomCodeWord())
				.replace("{codeword2}", Helper.getRandomCodeWord());
	}
}
