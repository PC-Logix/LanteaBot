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

	public static EffectEntry setCombinationEffect(AppearanceEntry consistency, AppearanceEntry appearance, EffectEntry effect) {
		String key = getCombinationKey(consistency, appearance);
		System.out.println("Registering effect for combination '" + key + "'");
		EffectEntry copy = effect.copy();
		System.out.println("Store effect:");
		System.out.println(copy.effectDrink);
		System.out.println(copy.effectSplash);
		System.out.println(copy.effectDrinkDiscovered);
		System.out.println(copy.effectSplashDiscovered);
		DrinkPotion.potions.put(key, copy);
		return copy;
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
		return replaceParamsInEffectString(effect, null, null);
	}

	public static String replaceParamsInEffectString(String effect, String nick) {
		return replaceParamsInEffectString(effect, nick, null);
	}

	public static String replaceParamsInEffectString(String effect, String nick, String triggererName) {
		String[] e = new String[] { effect };
		e = replaceParamsInEffectString(e, nick, triggererName);
		return e[0];
	}

	public enum DynaParam {
		ITEM("item", "{item}", "Replaced with a random item from the inventory, or a random junk item if nothing is found.", true, (input) -> {
			String tag = "{item}";
			Item item = Inventory.getRandomItem();
			String replItem;
			if (item == null)
				replItem = TablesOfRandomThings.getRandomGarbageItem();
			else
				replItem = item.getNameRaw();
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace(tag, replItem);
			return input;
		}),
		ITEM_JUNK("item_junk", "{junk_or_item}", "Returns either a random item from the inventory or a junk item, without prefix, all lower case.", true, (input) -> {
			String junkoritem = "nothing";
			try {
				junkoritem = Inventory.getRandomItem().getNameWithoutPrefix();
				if (Helper.getRandomInt(0, 1) == 1)
					junkoritem = TablesOfRandomThings.getRandomGarbageItem(false, true);
			} catch (Exception ex) {
				//Ignore no item found
			}
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{junk_or_item}", junkoritem);
			return input;
		}),
		ITEM_JUNK_PREFIX("item_junk_prefix", "{junk_or_item_p}", "Returns either a random item from the inventory or a junk item, with prefix, all lower case.", true, (input) -> {
			String junkoritem = "nothing";
			try {
				junkoritem = Inventory.getRandomItem().getNameWithoutPrefix();
				if (Helper.getRandomInt(0, 1) == 1)
					junkoritem = TablesOfRandomThings.getRandomGarbageItem(true, true);
			} catch (Exception ex) {
				//Ignore no item found
			}
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{junk_or_item_p}", junkoritem);
			return input;
		}),
		JUNK("junk", "{junk}", "Returns a random junk item, capitalized, without prefix", true, (input) -> {
			String item = TablesOfRandomThings.getRandomGarbageItem(false, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{junk}", item);
			return input;
		}),
		JUNK_PREFIX("junk_p", "{junk_p}", "Returns a random junk item, capitalized, with prefix", true, (input) -> {
			String item = TablesOfRandomThings.getRandomGarbageItem(true, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{junk_p}", item);
			return input;
		}),
		JUNK_PREFIX_LOWER("junk_p_lc", "{junk_p_lc}", "Returns a random junk item, in lowercase, with prefix", true, (input) -> {
			String item = TablesOfRandomThings.getRandomGarbageItem(true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{junk_p_lc}", item);
			return input;
		}),
		EVADE_DAMAGE("evade", "{evade:DC:damage}", "Allows triggering an evade event, resulting in damage on failure. A d20 roll is made and compared against the DC.", false, (input) -> {
			Pattern evadePattern = Pattern.compile("\\{evade:(\\d+):(.*)}");
			for (int i = 0; i < input.length; i++) {
				if (input[i] != null) {
					Matcher evadeMatcher = evadePattern.matcher(input[i]);
					if (evadeMatcher.find()) {
						DiceTest test = new DiceTest(Integer.parseInt(evadeMatcher.group(1)), "They successfully evaded it with a {result} vs DC {DC}!", "They fail to evade it with a {result} vs DC {DC}{damage}.");
						String damage = evadeMatcher.group(2);
						test.doCheck();
						if (!damage.equals("0"))
							damage = " and takes " + damage + " damage";
						else
							damage = "";
						input[i] = Helper.replaceSubstring(input[i], test.getLine().replace("{damage}", damage), evadeMatcher.start(), evadeMatcher.end());
					}
				}
			}
			return input;
		}),
		EVADE_CONSEQUENCE("evade_qc", "{evade_qc:DC:success:fail}", "Allows triggering an evade event, resulting in a consequence on failure. A d20 roll is made and compared against the DC.", false, (input) -> {
			Pattern evadePattern = Pattern.compile("\\{evade_qc:(\\d+):(.*):(.*)}");
			for (int i = 0; i < input.length; i++) {
				if (input[i] != null) {
					Matcher evadeMatcher = evadePattern.matcher(input[i]);
					if (evadeMatcher.find()) {
						DiceTest test = new DiceTest(Integer.parseInt(evadeMatcher.group(1)), evadeMatcher.group(2) + " ({result} vs DC {DC})", evadeMatcher.group(3) + " ({result} vs DC {DC})");
						test.doCheck();
						input[i] = Helper.replaceSubstring(input[i], test.getLine(), evadeMatcher.start(), evadeMatcher.end());
					}
				}
			}
			return input;
		}),
		APPEARANCE("appearance", "{appearance}", "Returns a random appearance, capitalized, without prefix.", true, (input) -> {
			String appearance = PotionHelper.getRandomAppearance().getName(false, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{appearance}", appearance);
			return input;
		}),
		APPEARANCE_LOWER("appearance_lc", "{appearance_lc}", "Returns a random appearance in lowercase, without prefix.", true, (input) -> {
			String appearance = PotionHelper.getRandomAppearance().getName(false, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{appearance_lc}", appearance);
			return input;
		}),
		APPEARANCE_PREFIX("appearance_p", "{appearance_p}", "Returns a random appearance, capitalized, with prefix.", true, (input) -> {
			String appearance = PotionHelper.getRandomAppearance().getName(true, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{appearance_p}", appearance);
			return input;
		}),
		APPEARANCE_PREFIX_LOWER("appearance_p_lc", "{appearance_p_lc}", "Returns a random appearance in lowercase, with prefix.", true, (input) -> {
			String appearance = PotionHelper.getRandomAppearance().getName(true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{appearance_p_lc}", appearance);
			return input;
		}),
		TURN_APPEARANCE("turn_appearance", "{turn_appearance}", "Returns the turnsTo form of a random appearance.", true, (input) -> {
			String appearance = PotionHelper.getRandomAppearance().turnsTo();
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{turn_appearance}", appearance);
			return input;
		}),
		TURN_APPEARANCE_LOWER("turn_appearance_lc", "{turn_appearance_lc}", "Returns the turnsTo form of a random appearance, in lowercase.", true, (input) -> {
			String appearance = PotionHelper.getRandomAppearance().turnsTo(true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{turn_appearance_lc}", appearance);
			return input;
		}),
		CONSISTENCY("consistency", "{consistency}", "Returns a random consistency, capitalized, without prefix.", true, (input) -> {
			String consistency = PotionHelper.getRandomConsistency().getName(false, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{consistency}", consistency);
			return input;
		}),
		CONSISTENCY_LOWER("consistency_lc", "{consistency_lc}", "Returns a random consistency, in lowercase, without prefix.", true, (input) -> {
			String consistency = PotionHelper.getRandomConsistency().getName(false, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{consistency_lc}", consistency);
			return input;
		}),
		CONSISTENCY_PREFIX("consistency_p", "{consistency_p}", "Returns a random consistency, capitalized, with prefix.", true, (input) -> {
			String consistency = PotionHelper.getRandomConsistency().getName(true, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{consistency_p}", consistency);
			return input;
		}),
		CONSISTENCY_PREFIX_LOWER("consistency_p_lc", "{consistency_p_lc}", "Returns a random consistency, in lowercase, with prefix.", true, (input) -> {
			String consistency = PotionHelper.getRandomConsistency().getName(true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{consistency_p_lc}", consistency);
			return input;
		}),
		TRANSFORMATION("transformation", "{transformation}", "Returns a random transformation, in lowercase, without prefix.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, false, false, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformation}", transformation);
			return input;
		}),
		TRANSFORMATION_PREFIX("transformation_p", "{transformation_p}", "Returns a random transformation, in lowercase, with prefix.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, true, false, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformation_p}", transformation);
			return input;
		}),
		TRANSFORMATION_CONDITIONAL_PREFIX("transformation_pc", "{transformation_pc}", "Returns a random transformation, in lowercase. This respects conditional prefixes, Such as \"turns into a lava cat\" vs \"turns into a lava\"", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, true, false, false);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformation_pc}", transformation);
			return input;
		}),
		TRANSFORMATION_2("transformation2", "{transformation2}", "Returns a random transformation, in lowercase, without prefix. Used to have two different transformations in one string.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, false, false, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformation2}", transformation);
			return input;
		}),
		TRANSFORMATION_2_PREFIX("transformation2_p", "{transformation2_p}", "Returns a random transformation, in lowercase, with prefix. Used to have two different transformations in one string.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, true, false, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformation2_p}", transformation);
			return input;
		}),
		TRANSFORMATIONS("transformations", "{transformations}", "A transformation in plural, such as \"cats\", in lowercase, without prefix.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(false, false, true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformations}", transformation);
			return input;
		}),
		TRANSFORMATIONS_PREFIX("transformations_p", "{transformations_p}", "A transformation in plural, such as \"cats\", in lowercase, with prefix.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, true, true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformations_p}", transformation);
			return input;
		}),
		TRANSFORMATIONS_2("transformations2", "{transformations2}", "A transformation in plural, such as \"cats\", in lowercase, without prefix. Used to have two different transformations in one string.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, false, true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformations2}", transformation);
			return input;
		}),
		TRANSFORMATIONS_2_PREFIX("transformations2_p", "{transformations2_p}", "A transformation in plural, such as \"cats\", in lowercase, with prefix. Used to have two different transformations in one string.", true, (input) -> {
			String transformation = TablesOfRandomThings.getRandomTransformation(true, true, true, true);
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{transformations2_p}", transformation);
			return input;
		}),
		LIMIT("limit", "{limit}", "Returns a random time limit string.", true, (input) -> {
			String limit = PotionHelper.getLimit();
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{limit}", limit);
			return input;
		}),
		CODEWORD("codeword", "{codeword}", "", true, (input) -> {
			String word = TablesOfRandomThings.getRandomCodeWord();
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{codeword}", word);
			return input;
		}),
		CODEWORD_2("codeword2", "{codeword2}", "", true, (input) -> {
			String word = TablesOfRandomThings.getRandomCodeWord();
			for (int i = 0; i < input.length; i++)
				if (input[i] != null)
					input[i] = input[i].replace("{codeword2}", word);
			return input;
		}),
		RANDOM_INT("random_int", "{r:n-n:p}", "Inserts a random integer within the chosen range, optionally a word can be inserted at p which will be pluralized if necessary and suffixed.", true, (input) -> {
			try {
				Pattern pattern = Pattern.compile("\\{r:(\\d\\d?\\d?)-(\\d\\d?\\d?)(?::(.*?))?}");
				for (int i = 0; i < input.length; i++) {
					if (input[i] != null) {
						Matcher matcher = pattern.matcher(input[i]);
						while (matcher.find()) {
							int num_min = Integer.parseInt(matcher.group(1));
							int num_max = Integer.parseInt(matcher.group(2));
							int value = Helper.getRandomInt(num_min, num_max);
							String repl = matcher.group(0).replace("{", "\\{").replace("}", "\\}");
							input[i] = input[i].replaceFirst(repl, value + (matcher.group(3) != null && !matcher.group(3).equals("") ? " " + Noun.pluralOf(matcher.group(3), value) : ""));
						}
					}
				}
			} catch (Exception ignored) { }
			return input;
		}),
		APPEARANCE_OBJECT("appearance_object", "{appearance:object:p}", "Inserts an appearance followed by the object, if p is present appearance will be prefixed, otherwise no prefix is included.", true, (input) -> {
			try {
				Pattern pattern = Pattern.compile("\\{appearance:(.*):(p?)}");
				for (int i = 0; i < input.length; i++) {
					if (input[i] != null) {
						Matcher matcher = pattern.matcher(input[i]);
						while (matcher.find()) {
							String appearance_item = matcher.group(1);
							boolean use_prefix = false;
							if (matcher.group(2).equals("p"))
								use_prefix = true;
							input[i] = input[i].replace(matcher.group(0), PotionHelper.getRandomAppearance().appearanceItem(appearance_item, use_prefix));
						}
					}
				}
			} catch (Exception ignored) { }
			return input;
		});

		public String name;
		public String tag;
		public String desc;
		public boolean store;
		public Function<String[], String[]> replace;

		DynaParam(String name, String tag, String desc, boolean store, Function<String[], String[]> replace) {
			this.name = name;
			this.tag = tag;
			this.desc = desc;
			this.store = store;
			this.replace = replace;
		}

		public static String[] ReplaceParameters(String[] input) {
			return ReplaceParameters(input, false);
		}

		public static String[] ReplaceParameters(String[] input, boolean prepareForStorage) {
			for (DynaParam p : DynaParam.values()) {
				if (!prepareForStorage || p.store)
					input = p.replace.apply(input);
			}
			return input;
		}
	}

	public static void replaceParamsInEffectString(EffectEntry effect) {
		if (effect.effectDrinkDiscovered == null && effect.effectSplashDiscovered == null) {
			String[] effects = new String[]{effect.effectDrink, effect.effectSplash};
			effects = replaceParamsInEffectString(effects);
			effect.effectDrinkDiscovered = effects[0];
			effect.effectSplashDiscovered = effects[1];
		}
	}

	public static void replaceParamsInEffectString(EffectEntry effect, String targetName) {
		if (effect.effectDrinkDiscovered == null && effect.effectSplashDiscovered == null) {
			String[] effects = new String[]{effect.effectDrink, effect.effectSplash};
			effects = replaceParamsInEffectString(effects, targetName);
			effect.effectDrinkDiscovered = effects[0];
			effect.effectSplashDiscovered = effects[1];
		}
	}

	public static void replaceParamsInEffectString(EffectEntry effect, String targetName, String triggerName) {
		if (effect.effectDrinkDiscovered == null && effect.effectSplashDiscovered == null) {
			String[] effects = new String[]{effect.effectDrink, effect.effectSplash};
			effects = replaceParamsInEffectString(effects, targetName, triggerName);
			effect.effectDrinkDiscovered = effects[0];
			effect.effectSplashDiscovered = effects[1];
		}
	}

	public static void replaceParamsInEffectString(EffectEntry effect, String targetName, String triggerName, boolean prepareForStorage) {
		if (effect.effectDrinkDiscovered == null && effect.effectSplashDiscovered == null) {
			String[] effects = new String[]{effect.effectDrink, effect.effectSplash};
			effects = replaceParamsInEffectString(effects, targetName, triggerName, prepareForStorage);
			effect.effectDrinkDiscovered = effects[0];
			effect.effectSplashDiscovered = effects[1];
		}
	}

	public static String[] replaceParamsInEffectString(String[] effects) {
		return replaceParamsInEffectString(effects, null, null, false);
	}

	public static String[] replaceParamsInEffectString(String[] effects, String targetName) {
		return replaceParamsInEffectString(effects, targetName, null, false);
	}

	public static String[] replaceParamsInEffectString(String[] effects, String targetName, String triggererName) {
		return replaceParamsInEffectString(effects, targetName, triggererName, false);
	}

	public static String[] replaceParamsInEffectString(String[] effects, String targetName, String triggererName, boolean prepareForStorage) {
		int timeout = 10;
		while (timeout > 0) {
			String[] lastEffects = effects.clone();
			timeout--;

			effects = DynaParam.ITEM.replace.apply(effects);

			if (targetName != null && !prepareForStorage) {
				for (int i = 0; i < effects.length; i++)
					if (effects[i] != null)
						effects[i] = effects[i].replaceAll("\\{user}", targetName);
			}

			if (triggererName != null && !prepareForStorage) {
				for (int i = 0; i < effects.length; i++)
					if (effects[i] != null)
						effects[i] = effects[i].replaceAll("\\{trigger}", triggererName);
			}

			effects = DynaParam.ReplaceParameters(effects, prepareForStorage);

			if (!prepareForStorage) {
				for (int i = 0; i < effects.length; i++) {
					if (effects[i] != null) {
						effects[i] = DiceRoll.rollDiceInString(effects[i], true);
					}
				}
			}

			boolean stop = false;
			for (int i = 0; i < effects.length; i++) {
				if (effects[i] != null) {
					if (effects[i].equals(lastEffects[i]))
						stop = true;
				}
			}
			if (stop)
				break;
		}

		return effects;
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
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{transformation_p}"))
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{transformation_pc}"))
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{transformation2}"))
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{transformations}"))
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{transformations_p}"))
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{transformations2}"))
			count += TablesOfRandomThings.getAnimalCount();
		if (effect.contains("{junk}"))
			count += TablesOfRandomThings.getGarbageItemCount();
		if (effect.contains("{junk_p}"))
			count += TablesOfRandomThings.getGarbageItemCount();
		if (effect.contains("{limit}"))
			count += getLimitCount();
		if (effect.contains("{codeword}"))
			count += TablesOfRandomThings.getCodeWordCount();
		if (effect.contains("{codeword2}"))
			count += TablesOfRandomThings.getCodeWordCount();
		return count;
	}

	public static void addConsistencyEntry(AppearanceEntry entry) {
		DrinkPotion.consistencyEntries.put(entry.Name.toLowerCase(), entry);
	}

	public static void addAppearanceEntry(AppearanceEntry entry) {
		DrinkPotion.appearanceEntries.put(entry.Name.toLowerCase(), entry);
	}

	public static String concealPlaceholdersForDisplay(String input) {
		input = input.replaceAll("\\{user}", "User");
		input = input.replaceAll("\\{evade:DC:damage}", "{Evade Challenge}");
		return input.replaceAll("\\{evade_qc:DC:success:fail}", "{Evade Challenge}");
	}
}
