import pcl.lc.irc.entryClasses.AppearanceEntry;
import pcl.lc.irc.entryClasses.EffectEntry;
import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.irc.hooks.Inventory;
import pcl.lc.utils.Database;
import pcl.lc.utils.PotionHelper;
import pcl.lc.utils.TablesOfRandomThings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitTests {
	static int totalSuccesses = 0;
	static int totalFailures = 0;
	static int successes = 0;
	static int failures = 0;

	public static void testPlaceholder(String placeholder, String expectedOutput, boolean prepareForStorage) throws Exception {
		EffectEntry effect = new EffectEntry(placeholder, placeholder);
		if (placeholder.equals("{user}"))
			PotionHelper.replaceParamsInEffectString(effect, "UserTest", null, prepareForStorage);
		else if (placeholder.equals("{trigger}"))
			PotionHelper.replaceParamsInEffectString(effect, null, "TriggerTest", prepareForStorage);
		else
			PotionHelper.replaceParamsInEffectString(effect, null, null, prepareForStorage);

		Pattern pattern = Pattern.compile(expectedOutput);
		Matcher matcherDrink = pattern.matcher(effect.effectDrinkDiscovered);
		Matcher matcherSplash = pattern.matcher(effect.effectSplashDiscovered);
		if (!matcherDrink.find()) {
			failures++;
			totalFailures++;
			throw new Exception("*FAILED!* Drink effect discrepancy for input '" + placeholder + "' expected output '" + expectedOutput + "' output '" + effect.effectDrinkDiscovered + "'");
		} else if (!matcherSplash.find()) {
			failures++;
			totalFailures++;
			throw new Exception("*FAILED!* Splash effect discrepancy for input '" + placeholder + "' expected output '" + expectedOutput + "' output '" + effect.effectDrinkDiscovered + "'");
		}
		successes++;
		totalSuccesses++;
		throw new Exception("PASSED! input '" + placeholder + "' expected output '" + expectedOutput + "' output '" + effect.effectDrinkDiscovered + "'");
	}

	public static void main(String[] args) {
		try {
			Database.init();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			return;
		}
		//<editor-fold desc="Database init">
		Database.addPreparedStatement("getCompressedSentences", "SELECT id, item_name, uses_left FROM Inventory WHERE item_name LIKE '%Compressed Sentence%'");
		Database.addPreparedStatement("setCompressedSentences", "UPDATE Inventory SET item_name = ?, uses_left = ? WHERE id = ?");
		Database.addPreparedStatement("newCompressedSentence", "INSERT INTO Inventory (item_name, uses_left, is_favourite, added_by, added) VALUES (?,?,?,?,?)");
		Database.addPreparedStatement("getItems", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory;");
		Database.addPreparedStatement("getItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE id = ?;");
		Database.addPreparedStatement("getFavouriteItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE is_favourite = 1 LIMIT 1");
		Database.addPreparedStatement("getItemByName", "SELECT id, item_name, uses_left, is_favourite, added_by, added FROM Inventory WHERE item_name = ?;");
		Database.addPreparedStatement("getRandomItem", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItems", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory ORDER BY Random() LIMIT ?");
		Database.addPreparedStatement("getRandomItemNonFavourite", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT 1");
		Database.addPreparedStatement("getRandomItemsNonFavourite", "SELECT id, item_name, uses_left, is_favourite, added_by, added, owner, cursed FROM Inventory WHERE is_favourite IS 0 ORDER BY Random() LIMIT ?");
		Database.addPreparedStatement("addItem", "INSERT INTO Inventory (id, item_name, uses_left, is_favourite, added_by, added) VALUES (NULL, ?, ?, ?, ?, ?)");
		Database.addPreparedStatement("removeItemId", "DELETE FROM Inventory WHERE id = ?");
		Database.addPreparedStatement("removeItemName", "DELETE FROM Inventory WHERE item_name = ?");
		Database.addPreparedStatement("decrementUses", "UPDATE Inventory SET uses_left = uses_left - 1 WHERE id = ?");
		Database.addPreparedStatement("setUses", "UPDATE Inventory SET uses_left = ? WHERE id = ?");
		Database.addPreparedStatement("clearFavourite", "UPDATE Inventory SET is_favourite = 0 WHERE is_favourite = 1");
		Database.addPreparedStatement("preserveItem", "UPDATE Inventory SET uses_left = -1 WHERE item_name = ?");
		Database.addPreparedStatement("unPreserveItem", "UPDATE Inventory SET uses_left = 5 WHERE item_name = ?");
		//</editor-fold>

		//<editor-fold desc="Init potion arrays with testing values">
		DrinkPotion.appearanceEntries.put("testappearance", new AppearanceEntry("TestAppearance", "A"));
		DrinkPotion.consistencyEntries.put("testconsistency", new AppearanceEntry("TestConsistency", "A"));
		DrinkPotion.limits.add("TestLimit");
		DrinkPotion.specialFluids.put("testspecialfluid", new EffectEntry("TestSpecialFluidEffectDrink", "TestSpecialFluidEffectSplash"));
		DrinkPotion.effects.add(new EffectEntry("TestEffectDrink", "TestEffectSplash"));
		//</editor-fold>
		//<editor-fold desc="Init random tables with testing values">
		TablesOfRandomThings.responsesFail = new String[]{ "FailTest" };
		TablesOfRandomThings.responsesSuccess = new String[]{ "SuccessTest" };
		TablesOfRandomThings.responsesSurprise = new String[]{ "SurpriseTest" };
		TablesOfRandomThings.responsesThanks = new String[]{ "ThanksTest" };
		TablesOfRandomThings.responsesAffirmative = new String[]{ "AffirmativeTest" };
		TablesOfRandomThings.careDetectorResponses = new String[]{ "CareTest" };
		TablesOfRandomThings.garbageDisposals = new String[]{ "GarbageDisposalTest" };
		TablesOfRandomThings.responsesHurt = new String[]{ "HurtTest" };
		TablesOfRandomThings.hitLocations = new String[]{ "HitLocationTest" };
		TablesOfRandomThings.garbageItems = new String[][]{ new String[] { "A", "GarbageItemTest" } };
		TablesOfRandomThings.animals = new String[][]{ new String[]{"An", "AnimalTest", "s", null} };
		TablesOfRandomThings.warpLocations = new String[]{ "WarpLocationTest" };
		TablesOfRandomThings.codeWords = new String[]{ "CodeWordTest" };
		TablesOfRandomThings.smashTargets = new ArrayList<>();
		TablesOfRandomThings.smashTargets.add(new String[][] { new String[]{ "A", "SmashTargetTest" }, new String[] { "SmashTargetResultTest" } });
		//</editor-fold>

		String[][] testPlaceholders = new String[][] {
			new String[] {"{item}","A GarbageItemTest"},
			new String[] {"{junk_or_item}", "nothing"},
			new String[] {"{junk_or_item_p}", "nothing"},
			new String[] {"{junk}", "GarbageItemTest"},
			new String[] {"{junk_p}", "A GarbageItemTest"},
			new String[] {"{junk_p_lc}", "a garbageitemtest"},
			new String[] {"{evade:1:10}", "They successfully evaded it with a \\d\\d? vs DC 1!"},
			new String[] {"{evade_qc:1:success:fail}", "success \\(\\d\\d? vs DC 1\\)"},
			new String[] {"{appearance}", "TestAppearance"},
			new String[] {"{appearance_lc}", "testappearance"},
			new String[] {"{appearance_p}", "A TestAppearance"},
			new String[] {"{appearance_p_lc}", "a testappearance"},
			new String[] {"{turn_appearance}", "TestAppearance"},
			new String[] {"{turn_appearance_lc}", "testappearance"},
			new String[] {"{consistency}", "TestConsistency"},
			new String[] {"{consistency_lc}", "testconsistency"},
			new String[] {"{consistency_p}", "A TestConsistency"},
			new String[] {"{consistency_p_lc}", "a testconsistency"},
			new String[] {"{transformation}", "animaltest"},
			new String[] {"{transformation_p}", "an animaltest"},
			new String[] {"{transformation_pc}", "an animaltest"},
			new String[] {"{transformation2}", "animaltest"},
			new String[] {"{transformation2_p}", "an animaltest"},
			new String[] {"{transformations}", "AnimalTests"},
			new String[] {"{transformations_p}", "animaltests"},
			new String[] {"{transformations2}", "animaltests"},
			new String[] {"{transformations2_p}", "animaltests"},
			new String[] {"{limit}", "TestLimit"},
			new String[] {"{codeword}", "CodeWordTest"},
			new String[] {"{codeword2}", "CodeWordTest"},
			new String[] {"{user}", "UserTest"},
			new String[] {"{trigger}", "TriggerTest"},
			new String[] {"{r:1-1}", "1"},
			new String[] {"{r:1-1:soups}", "1 soups" }
		};

		for (int i = 0; i < testPlaceholders.length; i++) {
			String[] entry = testPlaceholders[i];
			String num = String.valueOf(i);
			if (num.length() == 1)
				num = "0" + num;
			System.out.print("Entry " + num + ": ");
			try {
				testPlaceholder(entry[0], entry[1], false);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println("Test 1 completed. " + failures + " failure" + (failures == 1 ? "" : "s") + ", and " + successes + " success" + (successes == 1 ? "" : "es") + ".\n");
		failures = 0;
		successes = 0;

		testPlaceholders = new String[][] {
			new String[] {"{item}","A GarbageItemTest"},
			new String[] {"{junk_or_item}", "nothing"},
			new String[] {"{junk_or_item_p}", "nothing"},
			new String[] {"{junk}", "GarbageItemTest"},
			new String[] {"{junk_p}", "A GarbageItemTest"},
			new String[] {"{junk_p_lc}", "a garbageitemtest"},
			new String[] {"{evade:1:10}", "\\{evade:1:10\\}"},
			new String[] {"{evade_qc:1:success:fail}", "\\{evade_qc:1:success:fail\\}"},
			new String[] {"{appearance}", "TestAppearance"},
			new String[] {"{appearance_lc}", "testappearance"},
			new String[] {"{appearance_p}", "A TestAppearance"},
			new String[] {"{appearance_p_lc}", "a testappearance"},
			new String[] {"{turn_appearance}", "TestAppearance"},
			new String[] {"{turn_appearance_lc}", "testappearance"},
			new String[] {"{consistency}", "TestConsistency"},
			new String[] {"{consistency_lc}", "testconsistency"},
			new String[] {"{consistency_p}", "A TestConsistency"},
			new String[] {"{consistency_p_lc}", "a testconsistency"},
			new String[] {"{transformation}", "animaltest"},
			new String[] {"{transformation_p}", "an animaltest"},
			new String[] {"{transformation_pc}", "an animaltest"},
			new String[] {"{transformation2}", "animaltest"},
			new String[] {"{transformation2_p}", "an animaltest"},
			new String[] {"{transformations}", "AnimalTests"},
			new String[] {"{transformations_p}", "animaltests"},
			new String[] {"{transformations2}", "animaltests"},
			new String[] {"{transformations2_p}", "animaltests"},
			new String[] {"{limit}", "TestLimit"},
			new String[] {"{codeword}", "CodeWordTest"},
			new String[] {"{codeword2}", "CodeWordTest"},
			new String[] {"{user}", "\\{user\\}"},
			new String[] {"{trigger}", "\\{trigger\\}"},
			new String[] {"{r:1-1}", "1"},
			new String[] {"{r:1-1:soups}", "1 soups" },
			new String[] {"{appearance:marble:}", "TestAppearance marble" }
		};

		for (int i = 0; i < testPlaceholders.length; i++) {
			String[] entry = testPlaceholders[i];
			String num = String.valueOf(i);
			if (num.length() == 1)
				num = "0" + num;
			System.out.print("Entry " + num + ": ");
			try {
				testPlaceholder(entry[0], entry[1], true);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println("Test 2 completed. " + failures + " failure" + (failures == 1 ? "" : "s") + ", and " + successes + " success" + (successes == 1 ? "" : "es") + ".");

		if (totalFailures > 0)
			System.out.print("Total failed: " + totalFailures + " ");
		if (totalSuccesses > 0)
			System.out.print("Total succeeded: " + totalSuccesses);
		System.out.println();
	}
}
