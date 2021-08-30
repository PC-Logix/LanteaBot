import pcl.lc.irc.entryClasses.AppearanceEntry;
import pcl.lc.irc.entryClasses.CommandArgument;
import pcl.lc.irc.entryClasses.CommandArgumentParser;
import pcl.lc.irc.entryClasses.EffectEntry;
import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.utils.Database;
import pcl.lc.utils.PotionHelper;
import pcl.lc.utils.TablesOfRandomThings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum TestType {
	DYNA_PARAM,
	ARGPARSE,
}

class TestUnitGroup {
	public String name;
	public TestUnit[] tests;
	public int successes = 0;
	public int failures = 0;

	public TestUnitGroup(String name, TestUnit[] tests) {
		this.name = name;
		this.tests = tests;
	}
}

class TestUnit {
	public String input;
	public String[] output;
	public TestType type;
	public boolean mode = true;
	public boolean succeeded = false;
	public String result_string =  "";

	public TestUnit(String input, String output, TestType type) {
		this(input, new String[] {output}, type, false);
	}

	public TestUnit(String input, String output, TestType type, boolean mode) {
		this(input, new String[] {output}, type, mode);
	}

	public TestUnit(String input, String[] output, TestType type) {
		this(input, output, type, false);
	}

	public TestUnit(String input, String[] output, TestType type, boolean mode) {
		this.input = input;
		this.output = output;
		this.type = type;
		this.mode = mode;
	}

	public boolean runTest() {
		if (this.type == TestType.DYNA_PARAM) {
			EffectEntry effect = new EffectEntry(input, input);
			if (input.equals("{user}"))
				PotionHelper.replaceParamsInEffectString(effect, "UserTest", null, this.mode);
			else if (input.equals("{trigger}"))
				PotionHelper.replaceParamsInEffectString(effect, null, "TriggerTest", this.mode);
			else
				PotionHelper.replaceParamsInEffectString(effect, null, null, this.mode);

			String actual_output = "";
			for (String outp : this.output) {
				Pattern pattern = Pattern.compile(outp);
				Matcher matcherDrink = pattern.matcher(effect.effectDrinkDiscovered);
				Matcher matcherSplash = pattern.matcher(effect.effectSplashDiscovered);
				if (!matcherDrink.find()) {
					succeeded = false;
					actual_output = effect.effectDrinkDiscovered;
				} else if (!matcherSplash.find()) {
					succeeded = false;
					actual_output = effect.effectSplashDiscovered;
				}
				succeeded = true;
				actual_output = effect.effectDrinkDiscovered;
			}
			String output_str = " one of '" + String.join("', '", output) + "'";
			if (output.length == 1)
				output_str = "'" + output[0] + "'";
			result_string = ((succeeded) ? "PASSED!" : "*FAILED!* ") + " Input: '" + input + "', expected output: " + output_str + ", output: '" + actual_output + "'";
		} else if (this.type == TestType.ARGPARSE) {
			CommandArgumentParser parser = new CommandArgumentParser(1, new CommandArgument("String", "Input"));
			parser.debug = false;
			parser.parseArguments(this.input);
			String output = parser.getArgument("Input");

			if (output.equals(this.output[0])) {
				succeeded = true;
				result_string = "PASSED! Input: '" + input + "', expected output: '" + this.output[0] + "', output: '" + output + "'";
			} else {
				result_string = "*FAILED*! Input: '" + input + "', expected output: '" + this.output[0] + "', output: '" + output + "'";
			}
		}
		return succeeded;
	}
}

public class UnitTests {
	static int totalSuccesses = 0;
	static int totalFailures = 0;
	static int total = 0;

	public static void main(String[] args) {
		try {
			Database.init();
		} catch (SQLException e) {
			System.out.println("Database init failed");
			e.printStackTrace();
			return;
		}

		String static_item_query = "SELECT 1 as `id`, 'InventoryItem' as `item_name`, 1 as `uses_left`, 0 as `is_favourite`, 'Forecaster' as `added_by`, '1609796191457' as `added`, NULL as `owner`, 0 as `cursed`";

		//<editor-fold desc="Database init">
		Database.addPreparedStatement("getCompressedSentences", "SELECT id, item_name, uses_left FROM Inventory WHERE item_name LIKE '%Compressed Sentence%'");
		Database.addPreparedStatement("setCompressedSentences", "UPDATE Inventory SET item_name = ?, uses_left = ? WHERE id = ?");
		Database.addPreparedStatement("newCompressedSentence", "INSERT INTO Inventory (item_name, uses_left, is_favourite, added_by, added) VALUES (?,?,?,?,?)");
		Database.addPreparedStatement("getItems", static_item_query);
		Database.addPreparedStatement("getItem", static_item_query);
		Database.addPreparedStatement("getFavouriteItem", static_item_query);
		Database.addPreparedStatement("getItemByName", static_item_query);
		Database.addPreparedStatement("getRandomItem", static_item_query);
		Database.addPreparedStatement("getRandomItems", static_item_query);
		Database.addPreparedStatement("getRandomItemNonFavourite", static_item_query);
		Database.addPreparedStatement("getRandomItemsNonFavourite", static_item_query);
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

		TestUnitGroup[] tests = new TestUnitGroup[] {
			new TestUnitGroup("Single placeholders", new TestUnit[] {
				new TestUnit("{item}", 										"InventoryItem", TestType.DYNA_PARAM),
				new TestUnit("{junk_or_item}", 						new String[] { "InventoryItem", "GarbageItemTest" }, TestType.DYNA_PARAM),
				new TestUnit("{junk_or_item_p}", 					new String[] { "An InventoryItem", "A GarbageItemTest" }, TestType.DYNA_PARAM),
				new TestUnit("{junk}", 										"GarbageItemTest", TestType.DYNA_PARAM),
				new TestUnit("{junk_p}", 									"A GarbageItemTest", TestType.DYNA_PARAM),
				new TestUnit("{junk_p_lc}", 							"a garbageitemtest", TestType.DYNA_PARAM),
				new TestUnit("{evade:1:10}", 							"They successfully evaded it with a \\d\\d? vs DC 1!", TestType.DYNA_PARAM),
				new TestUnit("{evade_qc:1:success:fail}", "success \\(\\d\\d? vs DC 1\\)", TestType.DYNA_PARAM),
				new TestUnit("{evade:1:10}", 							"\\{evade:1:10\\}", TestType.DYNA_PARAM, true),
				new TestUnit("{evade_qc:1:success:fail}", "\\{evade_qc:1:success:fail\\}", TestType.DYNA_PARAM, true),
				new TestUnit("{appearance}", 							"TestAppearance", TestType.DYNA_PARAM),
				new TestUnit("{appearance_lc}", 					"testappearance", TestType.DYNA_PARAM),
				new TestUnit("{appearance_p}", 						"A TestAppearance", TestType.DYNA_PARAM),
				new TestUnit("{appearance_p_lc}", 				"a testappearance", TestType.DYNA_PARAM),
				new TestUnit("{turn_appearance}", 				"TestAppearance", TestType.DYNA_PARAM),
				new TestUnit("{turn_appearance_lc}", 			"testappearance", TestType.DYNA_PARAM),
				new TestUnit("{consistency}", 						"TestConsistency", TestType.DYNA_PARAM),
				new TestUnit("{consistency_lc}", 					"testconsistency", TestType.DYNA_PARAM),
				new TestUnit("{consistency_p}", 					"A TestConsistency", TestType.DYNA_PARAM),
				new TestUnit("{consistency_p_lc}", 				"a testconsistency", TestType.DYNA_PARAM),
				new TestUnit("{transformation}", 					"animaltest", TestType.DYNA_PARAM),
				new TestUnit("{transformation_p}", 				"an animaltest", TestType.DYNA_PARAM),
				new TestUnit("{transformation_pc}", 			"an animaltest", TestType.DYNA_PARAM),
				new TestUnit("{transformation2}", 				"animaltest", TestType.DYNA_PARAM),
				new TestUnit("{transformation2_p}", 			"an animaltest", TestType.DYNA_PARAM),
				new TestUnit("{transformations}", 				"AnimalTests", TestType.DYNA_PARAM),
				new TestUnit("{transformations_p}", 			"animaltests", TestType.DYNA_PARAM),
				new TestUnit("{transformations2}", 				"animaltests", TestType.DYNA_PARAM),
				new TestUnit("{transformations2_p}", 			"animaltests", TestType.DYNA_PARAM),
				new TestUnit("{limit}", 									"TestLimit", TestType.DYNA_PARAM),
				new TestUnit("{codeword}", 								"CodeWordTest", TestType.DYNA_PARAM),
				new TestUnit("{codeword2}", 							"CodeWordTest", TestType.DYNA_PARAM),
				new TestUnit("{user}", 										"UserTest", TestType.DYNA_PARAM),
				new TestUnit("{trigger}", 								"TriggerTest", TestType.DYNA_PARAM),
				new TestUnit("{r:1-1}", 									"1", TestType.DYNA_PARAM),
				new TestUnit("{r:1-1:soup}", 							"1 soup", TestType.DYNA_PARAM),
			}),
			new TestUnitGroup("Combined placeholders", new TestUnit[] {
				new TestUnit("I once saw {transformation_p} with {junk_p}", "I once saw an animaltest with A GarbageTest", TestType.DYNA_PARAM),
			}),
			new TestUnitGroup("Argparse", new TestUnit[] {
				new TestUnit("Stuff and things for sure", "Stuff and things for sure", TestType.ARGPARSE),
			}),
		};

		for (TestUnitGroup group : tests) {
			System.out.println("");
			System.out.println("Run test group '" + group.name + "'");

			for (TestUnit test : group.tests) {
				total++;
				test.runTest();

				if (test.succeeded) {
					group.successes++;
					totalSuccesses++;
				} else {
					group.failures++;
					totalFailures++;
				}
				System.out.println(test.result_string);
			}
			System.out.println("~~~~~~~~~~~~~~~~");
			if (group.failures > 0)
				System.out.print("Total group failures: " + group.failures + " ");
			if (group.successes > 0)
				System.out.println("Total group successes: " + group.successes + "/" + group.tests.length);
		}

		System.out.println("");
		System.out.println("==================");
		if (totalFailures > 0)
			System.out.print("Total failures: " + totalFailures + " ");
		if (totalSuccesses > 0)
			System.out.println("Total successes: " + totalSuccesses + "/" + total);
		System.out.println();
	}
}
