package pcl.lc.utils;

import gcardone.junidecode.App;
import org.joda.time.DateTime;
import org.jvnet.inflector.Noun;
import pcl.lc.irc.entryClasses.*;
import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.irc.hooks.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PotionHelper {
	/**
	 * @return String[] Returns three values: consistency, appearance and "" or "new" (whether potion has been generated already today)
	 */
	public static PotionEntry getRandomPotion() {
		AppearanceEntry app = getRandomAppearance();
		AppearanceEntry con = getRandomConsistency();
		System.out.println("Appearance: " + app.Name + ", Consistency: " + con.Name);
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

	public static String getCombinationKey(AppearanceEntry consistency, AppearanceEntry appearance) {
		return consistency.Name.toLowerCase() + "," + appearance.Name.toLowerCase();
	}

	public static boolean combinationHasEffect(AppearanceEntry consistency, AppearanceEntry appearance) {
		return combinationHasEffect(getCombinationKey(consistency, appearance));
	}

	public static boolean combinationHasEffect(String key) {
		tryResetPotionList();
		return DrinkPotion.potions.containsKey(key);
	}

	public static void setCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance, EffectEntry effect) {
		String key = getCombinationKey(consistency, appearance);
		System.out.println("Registering effect for combination '" + key + "'");
		DrinkPotion.potions.put(key, effect);
	}

	/**
	 *
	 * @param consistency An {@link AppearanceEntry} representing a consistency
	 * @param appearance An {@link AppearanceEntry} representing an appearance
	 * @return Return an {@link EffectEntry} if consistency and appearance combination exists or null otherwise
	 */
	public static EffectEntry getCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance) {
		return getCombinationEffect(getCombinationKey(consistency, appearance));
	}

	public static EffectEntry getCombinationEffect(String key) {
		System.out.println("Attempt to find registered effect for combination '" + key + "'");
		return DrinkPotion.potions.get(key);
	}

	public static AppearanceEntry findAppearanceInString(ArrayList<String> string) {
		return findAppearanceInString(String.join(" ", string));
	}

	public static AppearanceEntry findAppearanceInString(String string) {
		string = Helper.reverseString(string).toLowerCase();
		System.out.println(string);
		ArrayList<AppearanceEntry> appearanceEntries = new ArrayList<>(DrinkPotion.appearanceEntries.values());
		Collections.sort(appearanceEntries);
		for (AppearanceEntry c : appearanceEntries) {
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
		ArrayList<AppearanceEntry> consistencies = new ArrayList<>(DrinkPotion.consistencyEntries.values());
		Collections.sort(consistencies);
		for (AppearanceEntry c : consistencies) {
			if (string.contains(c.getName(false, true)))
				return c;
		}
		return null;
	}

	public static AppearanceEntry getRandomConsistency() {
		ArrayList<String> keys = new ArrayList<>(DrinkPotion.consistencyEntries.keySet());
		return DrinkPotion.consistencyEntries.get(keys.get(Helper.getRandomInt(0, keys.size() - 1)));
	}

	public static AppearanceEntry getRandomAppearance() {
		ArrayList<String> keys = new ArrayList<>(DrinkPotion.appearanceEntries.keySet());
		return DrinkPotion.appearanceEntries.get(keys.get(Helper.getRandomInt(0, keys.size() - 1)));
	}

	public static int getAppearanceCount() {
		return DrinkPotion.appearanceEntries.size();
	}

	public static int getConsistencyCount() {
		return DrinkPotion.consistencyEntries.size();
	}

	public static int getLimitCount() {
		return DrinkPotion.limits.size();
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

	public static String replaceParamsInEffectString(String effect) {
		return replaceParamsInEffectString(effect, null);
	}

	public static String replaceParamsInEffectString(String effect, String nick) {
		return replaceParamsInEffectString(effect, nick, null);
	}

	public enum DynaParam {
		ITEM("item", "{item}", "Replaced with a random item from the inventory, or a random junk item if nothing is found.", (input) -> {
			String tag = "{item}";
			Item item = Inventory.getRandomItem();
			if (item == null)
				return input.replace(tag, Helper.getRandomGarbageItem());
			return input.replace(tag, item.getNameRaw());
		}),
		ITEM_JUNK("item_junk", "{junk_or_item}", "Returns either a random item from the inventory or a junk item, without prefix, all lower case.", (input) -> {
			String junkoritem = "nothing";
			try {
				junkoritem = Inventory.getRandomItem().getNameWithoutPrefix();
				if (Helper.getRandomInt(0, 1) == 1)
					junkoritem = Helper.getRandomGarbageItem(false, true);
			} catch (Exception ex) {
				//Ignore no item found
			}
			return input.replace("{junk_or_item}", junkoritem);
		}),
		ITEM_JUNK_PREFIX("item_junk_prefix", "{junk_or_item_p}", "Returns either a random item from the inventory or a junk item, with prefix, all lower case.", (input) -> {
			String junkoritem = "nothing";
			try {
				junkoritem = Inventory.getRandomItem().getNameWithoutPrefix();
				if (Helper.getRandomInt(0, 1) == 1)
					junkoritem = Helper.getRandomGarbageItem(true, true);
			} catch (Exception ex) {
				//Ignore no item found
			}
			return input.replace("{junk_or_item_p}", junkoritem);
		}),
		JUNK("junk", "{junk}", "Returns a random junk item, capitalized, without prefix", (input) -> {
			return input.replace("{junk}", Helper.getRandomGarbageItem(false, false));
		}),
		JUNK_PREFIX("junk_p", "{junk_p}", "Returns a random junk item, capitalized, with prefix", (input) -> {
			return input.replace("{junk_p}", Helper.getRandomGarbageItem(true, false));
		}),
		JUNK_PREFIX_LOWER("junk_p_lc", "{junk_p_lc}", "Returns a random junk item, in lowercase, with prefix", (input) -> {
			return input.replace("{junk_p_lc}", Helper.getRandomGarbageItem(true, true));
		}),
		EVADE_DAMAGE("evade", "{evade:DC:damage}", "Allows triggering an evade event, resulting in damage on failure. A d20 roll is made and compared against the DC.", (input) -> {
			Pattern evadePattern = Pattern.compile("\\{evade:(\\d+):(.*)}");
			Matcher evadeMatcher = evadePattern.matcher(input);
			if (evadeMatcher.find()) {
				DiceTest test = new DiceTest(Integer.parseInt(evadeMatcher.group(1)), "They successfully evaded it with a {result} vs DC {DC}!", "They fail to evade it with a {result} vs DC {DC}{damage}.");
				String damage = evadeMatcher.group(2);
				test.doCheck();
				if (!damage.equals("0"))
					damage = " and takes " + damage + " damage";
				else
					damage = "";
				input = Helper.replaceSubstring(input, test.getLine().replace("{damage}", damage), evadeMatcher.start(), evadeMatcher.end());
			}
			return input;
		}),
		EVADE_CONSEQUENCE("evade_qc", "{evade_qc:DC:success:fail}", "Allows triggering an evade event, resulting in a consequence on failure. A d20 roll is made and compared against the DC.", (input) -> {
			Pattern evadePattern = Pattern.compile("\\{evade_qc:(\\d+):(.*):(.*)}");
			Matcher evadeMatcher = evadePattern.matcher(input);
			if (evadeMatcher.find()) {
				DiceTest test = new DiceTest(Integer.parseInt(evadeMatcher.group(1)), evadeMatcher.group(2) + " ({result} vs DC {DC})", evadeMatcher.group(3) + " ({result} vs DC {DC})");
				test.doCheck();
				input = Helper.replaceSubstring(input, test.getLine(), evadeMatcher.start(), evadeMatcher.end());
			}
			return input;
		}),
		APPEARANCE("appearance", "{appearance}", "Returns a random appearance, capitalized, without prefix.", (input) -> {
			return input.replace("{appearance}", PotionHelper.getRandomAppearance().getName(false, false));
		}),
		APPEARANCE_LOWER("appearance_lc", "{appearance_lc}", "Returns a random appearance in lowercase, without prefix.", (input) -> {
			return input.replace("{appearance_lc}", PotionHelper.getRandomAppearance().getName(false, true));
		}),
		APPEARANCE_PREFIX("appearance_p", "{appearance_p}", "Returns a random appearance, capitalized, with prefix.", (input) -> {
			return input.replace("{appearance_p}", PotionHelper.getRandomAppearance().getName(true, false));
		}),
		APPEARANCE_PREFIX_LOWER("appearance_p_lc", "{appearance_p_lc}", "Returns a random appearance in lowercase, with prefix.", (input) -> {
			return input.replace("{appearance_p_lc}", PotionHelper.getRandomAppearance().getName(true, true));
		}),
		TURN_APPEARANCE("turn_appearance", "{turn_appearance}", "Returns the turnsTo form of a random appearance.", (input) -> {
			return input.replace("{turn_appearance}", PotionHelper.getRandomAppearance().turnsTo());
		}),
		TURN_APPEARANCE_LOWER("turn_appearance_lc", "{turn_appearance_lc}", "Returns the turnsTo form of a random appearance, in lowercase.", (input) -> {
			return input.replace("{turn_appearance_lc}", PotionHelper.getRandomAppearance().turnsTo(true));
		}),
		CONSISTENCY("consistency", "{consistency}", "Returns a random consistency, capitalized, without prefix.", (input) -> {
			return input.replace("{consistency}", PotionHelper.getRandomConsistency().getName(false, false));
		}),
		CONSISTENCY_LOWER("consistency_lc", "{consistency_lc}", "Returns a random consistency, in lowercase, without prefix.", (input) -> {
			return input.replace("{consistency_lc}", PotionHelper.getRandomConsistency().getName(false, true));
		}),
		CONSISTENCY_PREFIX("consistency_p", "{consistency_p}", "Returns a random consistency, capitalized, with prefix.", (input) -> {
			return input.replace("{consistency_p}", PotionHelper.getRandomConsistency().getName(true, false));
		}),
		CONSISTENCY_PREFIX_LOWER("consistency_p_lc", "{consistency_p_lc}", "Returns a random consistency, in lowercase, with prefix.", (input) -> {
			return input.replace("{consistency_p_lc}", PotionHelper.getRandomConsistency().getName(true, true));
		}),
		TRANSFORMATION("transformation", "{transformation}", "Returns a random transformation, in lowercase, without prefix.", (input) -> {
			return input.replace("{transformation}", Helper.getRandomTransformation(true, false, false, true));
		}),
		TRANSFORMATION_PREFIX("transformation_p", "{transformation_p}", "Returns a random transformation, in lowercase, with prefix.", (input) -> {
			return input.replace("{transformation_p}", Helper.getRandomTransformation(true, true, false, true));
		}),
		TRANSFORMATION_CONDITIONAL_PREFIX("transformation_pc", "{transformation_pc}", "Returns a random transformation, in lowercase. This respects conditional prefixes, Such as \"turns into a lava cat\" vs \"turns into a lava\"", (input) -> {
			return input.replace("{transformation_pc}", Helper.getRandomTransformation(true, true, false, false));
		}),
		TRANSFORMATION_2("transformation2", "{transformation2}", "Returns a random transformation, in lowercase, without prefix. Used to have two different transformations in one string.", (input) -> {
			return input.replace("{transformation2}", Helper.getRandomTransformation(true, false, false, true));
		}),
		TRANSFORMATION_2_PREFIX("transformation2_p", "{transformation2_p}", "Returns a random transformation, in lowercase, with prefix. Used to have two different transformations in one string.", (input) -> {
			return input.replace("{transformation2_p}", Helper.getRandomTransformation(true, true, false, true));
		}),
		TRANSFORMATIONS("transformations", "{transformations}", "A transformation in plural, such as \"cats\", in lowercase, without prefix.", (input) -> {
			return input.replace("{transformations}", Helper.getRandomTransformation(false, false, true, true));
		}),
		TRANSFORMATIONS_PREFIX("transformations_p", "{transformations_p}", "A transformation in plural, such as \"cats\", in lowercase, with prefix.", (input) -> {
			return input.replace("{transformations_p}", Helper.getRandomTransformation(true, true, true, true));
		}),
		TRANSFORMATIONS_2("transformations2", "{transformations2}", "A transformation in plural, such as \"cats\", in lowercase, without prefix. Used to have two different transformations in one string.", (input) -> {
			return input.replace("{transformations2}", Helper.getRandomTransformation(true, false, true, true));
		}),
		TRANSFORMATIONS_2_PREFIX("transformations2_p", "{transformations2_p}", "A transformation in plural, such as \"cats\", in lowercase, with prefix. Used to have two different transformations in one string.", (input) -> {
			return input.replace("{transformations2_p}", Helper.getRandomTransformation(true, true, true, true));
		}),
		LIMIT("limit", "{limit}", "Returns a random time limit string.", (input) -> {
			return input.replace("{limit}", PotionHelper.getLimit());
		}),
		CODEWORD("codeword", "{codeword}", "", (input) -> {
			return input.replace("{codeword}", Helper.getRandomCodeWord());
		}),
		CODEWORD_2("codeword2", "{codeword2}", "", (input) -> {
			return input.replace("{codeword2}", Helper.getRandomCodeWord());
		});

		public String name;
		public String tag;
		public String desc;
		public Function<String, String> replace;

		DynaParam(String name, String tag, String desc, Function<String, String> replace) {
			this.name = name;
			this.tag = tag;
			this.desc = desc;
			this.replace = replace;
		}

		public static String ReplaceParameters(String input) {
			for (DynaParam p : DynaParam.values()) {
				input = p.replace.apply(input);
			}
			return input;
		}
	}

	public static String replaceParamsInEffectString(String effect, String targetName, String triggererName) {
		String tempEffect = "";
		int timeout = 10;
		while (timeout > 0) {
			timeout++;

			effect = DynaParam.ITEM.replace.apply(effect);

			effect = DiceRoll.rollDiceInString(effect, true);

			if (targetName != null)
				effect = effect.replaceAll("\\{user}", targetName);

			if (triggererName != null)
				effect = effect.replaceAll("\\{trigger}", triggererName);

			try {
				Pattern pattern = Pattern.compile("\\{r:(\\d\\d?\\d?)-(\\d\\d?\\d?)(?::(.*?))?}");
				Matcher matcher = pattern.matcher(effect);
				while (matcher.find()) {
					int num_min = Integer.parseInt(matcher.group(1));
					int num_max = Integer.parseInt(matcher.group(2));
					int value = Helper.getRandomInt(num_min, num_max);
					String repl = matcher.group(0).replace("{", "\\{").replace("}", "\\}");
					effect = effect.replaceFirst(repl, value + (matcher.group(3) != null && !matcher.group(3).equals("") ? " " + Noun.pluralOf(matcher.group(3), value) : ""));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			effect = DynaParam.ReplaceParameters(effect);

			if (tempEffect.equals(effect))
				break;
			tempEffect = effect;
		}

		return effect;
	}

	public static int countEffectVariations(String effect) {
		int count = 0;
		if (effect.contains("{appearance}"))
			count += getAppearanceCount();
		if (effect.contains("{apperance_lc}"))
			count += getAppearanceCount();
		if (effect.contains("{appearance_p}"))
			count += getAppearanceCount();
		if (effect.contains("{appearance_p_lc}"))
			count += getAppearanceCount();
		if (effect.contains("{turn_appearance}"))
			count += getAppearanceCount();
		if (effect.contains("{turn_appearance_lc}"))
			count += getAppearanceCount();
		if (effect.contains("{consistency}"))
			count += getConsistencyCount();
		if (effect.contains("{consistency_lc}"))
			count += getConsistencyCount();
		if (effect.contains("{consistency_p}"))
			count += getConsistencyCount();
		if (effect.contains("{consistency_p_lc}"))
			count += getConsistencyCount();
		if (effect.contains("{transformation}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{transformation_p}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{transformation_pc}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{transformation2}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{transformations}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{transformations_p}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{transformations2}"))
			count += Helper.getAnimalCount();
		if (effect.contains("{junk}"))
			count += Helper.getGarbageItemCount();
		if (effect.contains("{junk_p}"))
			count += Helper.getGarbageItemCount();
		if (effect.contains("{limit}"))
			count += getLimitCount();
		if (effect.contains("{codeword}"))
			count += Helper.getCodeWordCount();
		if (effect.contains("{codeword2}"))
			count += Helper.getCodeWordCount();
		return count;
	}

	public static void addConsistencyEntry(AppearanceEntry entry) {
		DrinkPotion.consistencyEntries.put(entry.Name.toLowerCase(), entry);
	}

	public static void addAppearanceEntry(AppearanceEntry entry) {
		DrinkPotion.appearanceEntries.put(entry.Name.toLowerCase(), entry);
	}
}
