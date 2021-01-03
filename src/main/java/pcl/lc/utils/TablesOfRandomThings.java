package pcl.lc.utils;

import java.util.ArrayList;

public class TablesOfRandomThings {
	public static String[] responsesFail;
	public static String[] responsesSuccess;
	public static String[] responsesSurprise;
	public static String[] responsesThanks;
	public static String[] responsesAffirmative;
	public static String[] careDetectorResponses;
	public static String[] garbageDisposals;
	public static String[] responsesHurt;
	public static String[] hitLocations;
	public static String[][] garbageItems;
	public static String[][] animals;
	public static String[] warpLocations;
	public static String[] codeWords;
	public static ArrayList<String[][]> smashTargets;

	public static void initRandomTables() {
		responsesFail = new String[]{
			"Oops...",
			"ohno",
			"Not again...",
			"Dammit!",
			"#@%&!!",
			"Fore!",
			"I hope nobody saw that...",
			"I didn't do it!",
		};

		responsesSuccess = new String[]{
			"Yes!",
			"I did it!",
			"Woo!",
			"I'm awesome!",
			"Take that RNG!",
			"In yo face!",
			"Exactly as planned.",
		};

		responsesSurprise = new String[]{
			"What? D:",
			"Nooooo",
			"Whatever.",
			"Nuuh",
			"I'll stab you in the face!",
			"How dare you?!",
			"Fight me!",
			"No u!",
			"Someone's mad.",
		};

		responsesThanks = new String[]{
			"Thanks!",
			"Wow thanks!",
		};

		responsesAffirmative = new String[]{
			"Meh.",
			"Sure, I guess",
			"Hm?",
			"What? No.",
			"That's fine I guess",
			"Sure, whatever",
			"Yeah right.",
			"Maybe.",
			"Okay",
		};

		careDetectorResponses = new String[]{
			"No caring detected in the area",
			"The tricorder shows 0%, captain",
			"Records show zero shits given",
			"Scans indicate 0.001 units of caring, with a 0.02% margin of error",
			"Earlier instances indicate you do not.",
			"Barely even registers on the care-o-meter",
			"The needle seems to be stuck below 0",
			"Detecting trace amounts of background caring, but nothing significant",
		};

		garbageDisposals = new String[]{
			"into a garbage compactor",
			"into a black hole",
			"in the Sarlacc pit",
			"in the tentacle pit",
			"in the incinerator",
			"into a portal to the moon",
			"into a stargate to deep space",
			"a forgotten box in somebodies garage",
			"into the core of a dying star",
			"into a nearby garbage can",
			"in a hole with lava in it",
			"into the void",
		};

		responsesHurt = new String[]{
			"ow",
			"ouch",
			"owies",
			"ohno D:",
			"aaah",
			"agh",
			"ack",
			"owwwww",
		};

		hitLocations = new String[]{
			"on the arm",
			"in the head",
			"on the butt",
			"in their pride",
			"in the small of the back",
			"on the heel",
			"on the left hand",
			"underneath their foot",
			"in their spleen",
			"on a body part they didn't even know they had",
			"in the face",
			"on a small but very important bone",
			"right where they didn't expect",
			"right where the last item hit",
			"right in their lunch",
		};

		garbageItems = new String[][]{
			new String[]{"a", "Twig"},
			new String[]{"a", "Pebble"},
			new String[]{"a", "Piece of cloth"},
			new String[]{"a", "Leaf"},
			new String[]{"a", "Weed"},
			new String[]{"a", "Paper crane"},
			new String[]{"a", "Half-eaten fortune cookie"},
			new String[]{"a", "Cookie with raisins"},
			new String[]{"a", "Turnip"},
			new String[]{"a", "Potato"},
			new String[]{"a", "Doorknob"},
			new String[]{"a", "Rickety Gazebo"},
			new String[]{"", "Half of an IKEA shelf"},
			new String[]{"a", "Metal bearing"},
			new String[]{"a", "Wooden bird"},
			new String[]{"", "Cheese residue"},
			new String[]{"a", "Slice of butter"},
			new String[]{"a", "Depleted 9v battery"},
			new String[]{"a", "Brick"},
			new String[]{"a", "Charred piece of bacon"},
			new String[]{"a", "Single grain of rice"},
			new String[]{"an", "Empty bottle"},
			new String[]{"a", "Bottle filled with concrete"},
			new String[]{"a", "Ball of yarn"},
			new String[]{"a", "Fork"},
			new String[]{"", "Some half-melted snow"},
			new String[]{"a", "Deed for a bridge"},
			new String[]{"an", "Unlabeled key"},
			new String[]{"a", "Napkin with scribbles on it"},
			new String[]{"a", "Butterfly"},
			new String[]{"a", "Phone battery"},
			new String[]{"a", "Set of assorted wires"},
			new String[]{"a", "Pen"},
			new String[]{"a", "Pencil"},
			new String[]{"an", "Eraser"},
			new String[]{"an", "Empty post-it note"},
			new String[]{"a", "Hood ornament in the shape of a tomato"},
			new String[]{"a", "Bottle cap"},
			new String[]{"an", "Empty Array"},
			new String[]{"", "attempt to index nil value"},
			new String[]{"a", "Expired lottery ticket"},
			new String[]{"a", "Tiny bag of catnip"},
			new String[]{"a", "Tiny snail"},
			new String[]{"a", "Corn on the cob"},
			new String[]{"a", "Pecan pie"},
			new String[]{"an", "Empty drive slot"},
			new String[]{"a", "Dropbox account with zero capacity"},
			new String[]{"an", "Empty shot glass"},
			new String[]{"a", "Lootcrate"},
			new String[]{"a", "Power adapter incompatible with everything"},
			new String[]{"a", "Lockpick"},
			new String[]{"", "Two lockpicks"},
			new String[]{"a", "Monopoly top hat figure"},
			new String[]{"a", "Pretty average hat"},
			new String[]{"a", "Knight who says NI"},
			new String[]{"the", "Bottom of a barrel"},
			new String[]{"an", "Impossible geometric shape"},
			new String[]{"a", "Geode"},
			new String[]{"a", "Sad looking flower"},
			new String[]{"a", "Happy flower"},
			new String[]{"a", "Particularly fat bee"},
			new String[]{"a", "Box full of wasps"},
			new String[]{"a", "Box full of worms"},
			new String[]{"a", "Box full of thumbtacks"},
			new String[]{"a", "Box full of caps"},
			new String[]{"", "randompotion"},
			new String[]{"a", "Picture of a crudely drawn appendage"},
			new String[]{"a", "Broken .jpg"},
			new String[]{"a", "Broken .png"},
			new String[]{"a", "Broken .gif"},
			new String[]{"a", "Broken .tif"},
			new String[]{"a", "Broken .mov"},
			new String[]{"a", "Broken .zip"},
			new String[]{"a", "Broken .psd"},
			new String[]{"a", "Broken .7z"},
			new String[]{"a", "Broken .mp3"},
			new String[]{"a", "Broken .mp4"},
			new String[]{"a", "Broken .mp5"},
			new String[]{"a", "Broken .mp6"},
			new String[]{"a", "Pentagram pendant"},
			new String[]{"a", "Rosary"},
			new String[]{"an", "Upside-down cross"},
			new String[]{"a", "Poofy ball of fluff"},
			new String[]{"a", "Paperclip, big one"},
			new String[]{"a", "Leftover pumpkin"},
			new String[]{"a", "Fork in the road"},
			new String[]{"a", "Chocolate bar that was left out in the sun"},
			new String[]{"an", "Impossibly green dress"},
			new String[]{"a", "Piece of rope slightly too small to be useful"},
			new String[]{"a", "20ft Pole"},
			new String[]{"", "Ten birds in a bush"},
			new String[]{"a", "Very stale piece of pizza"},
			new String[]{"a", "Tiny packet of cream"},
			new String[]{"a", "Tiny packet of ketchup"},
			new String[]{"a", "Tiny packet of salt"},
			new String[]{"a", "Tiny packet of packets"},
			new String[]{"a", "Tiny packet of rubber bands"},
			new String[]{"a", "Tiny model shoe"},
			new String[]{"a", "Mermaids tear"},
			new String[]{"a", "Mermaid scale"},
			new String[]{"a", "Dragon tooth"},
			new String[]{"a", "Dragon scale"},
			new String[]{"a", "Book that is glued shut"},
			new String[]{"a", "Sealed unmarked canister"},
			new String[]{"a", "Canister of neurotoxin"},
			new String[]{"a", "Frog leg"},
			new String[]{"", "Eye of newt"},
			new String[]{"", "Roberto's knife"},
			new String[]{"an", "Unassuming lamp"},
			new String[]{"an", "Assuming lamp"},
			new String[]{"a", "Sinister lamp"},
			new String[]{"a", "Dead lamp"},
			new String[]{"a", "Lit lamp"},
			new String[]{"a", "Double lamp"},
			new String[]{"", "Half a lamp"},
			new String[]{"", "Three quarters of lamp"},
			new String[]{"a", "Super lamp"},
			new String[]{"a", "Dim lamp"},
			new String[]{"a", "Copy of \"The Lusty Argonian Maid\""},
			new String[]{"a", "Cabbage leaf"},
			new String[]{"an", "Ornate chandelier"},
			new String[]{"a", "Tiny cage"},
			new String[]{"a", "Tiny fork"},
			new String[]{"a", "Tiny spoon"},
			new String[]{"a", "Tiny knife"},
			new String[]{"an", "Ornate Nate"},
			new String[]{"a", "Tiny figurine"},
			new String[]{"a", "Mask of your face"},
			new String[]{"a", "Mask of someones face"},
			new String[]{"a", "Tiny clay figure"},
			new String[]{"an", "Empty soup can"},
			new String[]{"an", "Empty wooden chest"},
			new String[]{"a", "Portable stick"},
			new String[]{"a", "Stationary stick"},
			new String[]{"an", "Inanimate carbon rod"},
			new String[]{"a", "Living tombstone"},
			new String[]{"a", "Talking sword that wont stop talking"},
			new String[]{"a", "3D-printer that only prints in papier mache"},
			new String[]{"a", "Raspberry Pi that only beeps at you"},
			new String[]{"a", "Sphere that just wont stop talking"},
			new String[]{"a", "Talking fork"},
			new String[]{"a", "Talking spoon"},
			new String[]{"a", "Talking knife"},
			new String[]{"a", "Talking spork"},
			new String[]{"a", "Eerily quiet singing fish"},
			new String[]{"a", "Suspicious looking statue"},
			new String[]{"a", "Radioactive teapot"},
			new String[]{"a", "Miraculous Miracle Man (MMM) #1 comic"},
			new String[]{"the", "official laws and migration guidelines of Pluto"},
			new String[]{"the", "official baby talk translation guide"},
			new String[]{"", "Loot boxes for dummies volume 1"},
			new String[]{"the", "Extra-terrestrials guide to Earth fourth edition"},
			new String[]{"the", "Ultimate guide to killing all humans"},
			new String[]{"a", "Shiny metal posterior"},
			new String[]{"an", "Unfinished m"},
			new String[]{"a", "Sort-of-holy symbol"},
			new String[]{"a", "Guide to Talking to Rocks"},
			new String[]{"", "randompotion"},
			new String[]{"a", "triangular ball"},
			new String[]{"a", "pie-shaped cake"},
			new String[]{"an", "Inverted hole"},
			new String[]{"a", "Small pile of dirt"},
			new String[]{"a", "Jar of dirt"},
			new String[]{"a", "Cracked crack"},
			new String[]{"an", "Extremely short fork"},
			new String[]{"an", "Incredibly thin sheet of air"},
			new String[]{"a", "Poofy cloud"},
			new String[]{"a", "Hard cloud"},
			new String[]{"a", "Pointy cloud"},
			new String[]{"a", "Soft cloud"},
			new String[]{"an", "Angry cloud"},
			new String[]{"a", "Nice cloud"},
			new String[]{"", "Another settlement that needs your help"},
			new String[]{"a", "baseball cap with the Starbucks logo on it"},
			new String[]{"a", "baseball cap with the McDonalds logo on it"},
			new String[]{"a", "baseball cap with the IKEA logo on it"},
			new String[]{"a", "baseball cap with the Walmart logo on it"},
			new String[]{"a", "baseball cap with the BuyNLarge logo on it"}, //Wall-E
			new String[]{"a", "baseball cap with the Mom's Friendly Robot Company logo on it"}, // Futurama
			new String[]{"a", "baseball cap with the Octan logo on it"}, // LEGO
			new String[]{"", "The perfect hiding place"},
			new String[]{"an", "Incomplete ce"},
		};

		animals = new String[][]{
			//prefix, name, suffix, remove n characters from end before applying suffix
			new String[]{"A", "Pig", "s", null},
			new String[]{"A", "Horse", "s", null},
			new String[]{"A", "Cat", "s", null},
			new String[]{"A", "Dog", "s", null},
			new String[]{"A", "Fish", "", null},
			new String[]{"A", "Crocodile", "s", null},
			new String[]{"A", "Bird", "s", null},
			new String[]{"A", "Lizard", "s", null},
			new String[]{"A", "Fox", "es", null},
			new String[]{"A", "Turtle", "s", null},
			new String[]{"A", "Sloth", "s", null},
			new String[]{"A", "Wolf", "ves", "1"},
			new String[]{"A", "Robot", "s", null},
			new String[]{"A", "Golem", "s", null},
			new String[]{"A", "Unicorn", "s", null},
			new String[]{"A", "Dryad", "s", null},
			new String[]{"A", "Dragon", "s", null},
			new String[]{"A", "Fairy", "ies", "1"},
			new String[]{"A*", "Spaghetti", "", null},
			new String[]{"A*", "Water", "", null},
			new String[]{"A*", "Lava", "", null},
			new String[]{"A", "Shark", "s", null},
			new String[]{"An", "Otter", "s", null},
			new String[]{"A", "Goat", "s", null},
			new String[]{"A", "Sheep", "", null},
			new String[]{"A", "Toad", "s", null},
			new String[]{"A", "Sword", "s", null},
			new String[]{"A", "Bear", "s", null},
			new String[]{"A", "Platypus", "i", "2"},
			new String[]{"A", "Frog", "s", null},
			new String[]{"An", "Octopus", "i", "3"},
			new String[]{"A", "Unicorn", "s", null},
		};

		//Valid tags: {user},{appearance},{turn_appearance},{appearance:item},{consistency},{p_transformation},{transformation},{transformation2},{transformations},{transformations2}
		warpLocations = new String[]{
			"You end up at home.",
			"You end up in your bed.",
			"You end up in a dimension populated by {transformations}.",
			"You end up in a dimension populated by {transformation} girls.",
			"You end up in a dimension populated by {transformation} boys.",
			"You end up in a dimension populated by {transformation} {transformation2} girls.",
			"You end up in a dimension populated by {transformation} {transformation2} boys.",
			"You end up in a dimension populated by {transformation} {transformations2}.",
			"You end up in a dimension inhabited by {p_transformation}.",
			"You end up in a dimension entirely filled with {consistency} {appearance} potion.",
			"You end up in a dimension ruled by {item}.",
			"You end up in a dimension that is just an endless field of flowers.",
			"You end up in a frozen world.",
			"You end up in a dry world.",
			"You end up in a world inhabited by mimes.",
			"You end up in a world inhabited by bards.",
			"You end up in a world inhabited by clowns.",
			"You end up at the location of a great treasure. The treasure of friendship!",
		};

		codeWords= new String[]{
			"Blatherskite",
			"Mew",
			"Nyan",
			"Woof",
			"Ohmygawd",
			"Jeez",
			"Crystal",
			"Doom",
			"Nice",
			"Awesome",
			"Wat",
			"Yip",
			"Wenk",
			"Harmony",
			"Swing",
			"Classic",
			"Noir",
			"Supercalifragilisticexpialidocious",
			"Rather",
			"Technically",
			"Actually",
			"Sup",
			"Soup",
		};

		smashTargets = new ArrayList<String[][]>() {{
			add(new String[][] {new String[]{"A", "Statue"}, new String[]{
				"Nothing but dust remains.",
				"It is no longer recognizable.",
				"Naught remains but gravel.",
				"They've got some statue bits stuck in their hair now.",
				"Other nearby statues are horrified by the display.",
				"It turns out if you hit a statue just right it produces fireworks.",
				"It never saw it coming.",
				"If it could, the statue would be very surprised.",
				"The statue had a shiny thing inside of it."}
			});
			add(new String[][] {new String[]{"A", "Vase"}, new String[]{
				"It looked very expensive.",
				"Until just now it was priceless.",
				"You're sure it was your worst enemy's favourite vase.",
				"It'll take someone quite some time to glue this back together.",
				"You now have a great source of tiny sharp things.",
				"Some would say it's more valuable now.",
				"A future archeologist will ponder in awe over this display of destruction.",
				"A particularly sharp piece hit someone you don't like in the face."
			}});
			add(new String[][] {new String[]{"A", "Pot"}, new String[]{
				"{r:1-12:large green gem} of some sort fell out.",
				"{r:1-8:large red gem} of some sort fell out.",
				"{r:1-4:large blue gem} of some sort fell out.",
				"A large purple gem of some sort fell out.",
				"It took someone a good chunk of time to make this, and now it's gone.",
				"For some reason you briefly feel like wearing a green tunic and pointy hat.",
				"A small fairly appears, swirls around you. You feel as if you have multiple hearts now, weird.",
				"Where the pot used to be there was a pressure plate, you hear a door open somewhere.",
				"The pottery store owner yells at you, time to run away!"
			}});
			add(new String[][] {new String[]{"A", "Bust"}, new String[]{
				"Taking a mallet to a bust is a great stress reliever.",
				"You think it was supposed to be of some emperor or something.",
				"Whoever this was supposed to be of it's not recognizable anymore.",
				"The aftermath leaves a great 3D puzzle for a determined individual.",
				"The bust was looking at you funny, so it deserved this.",
				"It definitely bore a resemblance to someone you don't like.",
				"It's nose was too big anyway."
			}});
			add(new String[][] {new String[]{"A", "Bug"}, new String[]{
				"It never knew what hit it! But now there's suddenly {r:2-6} more.",
				"Bu⊥ now ⊥hƐ charac⊥Ɛr Ɛncodings are all wronɓ.",
				"You thought this was the last one, but there's {r:10-40} more behind it.",
				"But now all the NPC's are upside down.",
				"But it seems to have made things worse somewhow."
			}});
			add(new String[][] {new String[]{"A", "Box"}, new String[]{
				"It turns out to be a loot crate. It contained {junk_or_item_p}.",
				"It was empty, but your path is now clear.",
				"It made a satisfying cracking sound as it shattered.",
				"Another box falls down to take its place.",
				"It drops {junk}"
			}});
		}};
	}

	public static int getFailResponseCount() {
		return responsesFail.length;
	}

	public static String getFailResponse() {
		return responsesFail[Helper.getRandomInt(0, responsesFail.length - 1)];
	}

	public static int getSuccessResponseCount() {
		return responsesSuccess.length;
	}

	public static String getSuccessResponse() {
		return responsesSuccess[Helper.getRandomInt(0, responsesSuccess.length - 1)];
	}

	public static int getSurpriseResponseCount() {
		return responsesSurprise.length;
	}

	public static String getSurpriseResponse() {
		return responsesSurprise[Helper.getRandomInt(0, responsesSurprise.length - 1)];
	}

	public static int getThanksResponseCount() {
		return responsesThanks.length;
	}

	public static String getThanksResponse() {
		return responsesThanks[Helper.getRandomInt(0, responsesThanks.length - 1)];
	}

	public static int getAffirmativeResponseCount() {
		return responsesAffirmative.length;
	}

	public static String getAffirmativeResponse() {
		return responsesAffirmative[Helper.getRandomInt(0, responsesAffirmative.length - 1)];
	}

	public static int getCareDetectorResponseCount() {
		return careDetectorResponses.length;
	}

	public static String getCareDetectorResponse() {
		return careDetectorResponses[Helper.getRandomInt(0, careDetectorResponses.length - 1)];
	}

	public static int getGarbageDisposalCount() {
		return garbageDisposals.length;
	}

	public static String getGarbageDisposal() {
		return garbageDisposals[Helper.getRandomInt(0, garbageDisposals.length - 1)];
	}

	public static int getHurtResponseCount() {
		return responsesHurt.length;
	}

	public static String getHurtResponse() {
		return responsesHurt[Helper.getRandomInt(0, responsesHurt.length - 1)];
	}

	public static int getHitPlaceCount() {
		return hitLocations.length;
	}

	public static String getHitPlace() {
		return hitLocations[Helper.getRandomInt(0, hitLocations.length - 1)];
	}



	public static String getRandomExclamations() {
		return getRandomExclamations(false, false);
	}

	public static String getRandomExclamations(boolean allowOnes, boolean allowQuestionmarks) {
		return getRandomExclamations(Helper.getRandomInt(1, 8), 1, allowOnes, allowQuestionmarks);
	}

	public static String getRandomExclamations(int maxLength, int minLength) {
		return getRandomExclamations(maxLength, minLength, false, false);
	}

	public static String getRandomExclamations(int maxLength, int minLength, boolean allowOnes, boolean allowQuestionMarks) {
		String charss = "!";
		if (allowOnes)
			charss += "1";
		if (allowQuestionMarks)
			charss += "?";
		char[] chars = charss.toCharArray();
		String output = "";
		for (int i = minLength; i < maxLength; i++) {
			output += chars[Helper.getRandomInt(0, chars.length - 1)];
		}
		if (!output.contains("!"))
			output += "!";
		return output;
	}

	public static int getGarbageItemCount() {
		return garbageItems.length;
	}

	/**
	 * Calls getRandomGarbageItem(boolean all_lower_case) with all_lower_case = false
	 * Returns a random mundane garbage item
	 *
	 * @return A random name
	 */
	public static String getRandomGarbageItem() {
		return getRandomGarbageItem(true, false);
	}

	/**
	 * Returns a random mundane garbage item
	 *
	 * @param all_lower_case Whether to return all lower case
	 * @return A random name
	 */
	public static String getRandomGarbageItem(boolean include_prefix, boolean all_lower_case) {
		String name;
		try {
			int index = Helper.getRandomInt(0, garbageItems.length - 1);
			String[] item = garbageItems[index];
			name = (include_prefix && !item[0].equals("") ? item[0] + " " : "") + item[1];
			if (all_lower_case)
				name = name.toLowerCase();
		} catch (Exception ex) {
			name = "[Error]";
		}
		return name;
	}

	public static int getAnimalCount() {
		return animals.length;
	}

	public static String getTransformationByIndex(int index) {
		return getTransformationByIndex(index, false, false, false, true);
	}

	public static String getTransformationByIndex(int index, boolean lower_case, boolean prefix, boolean plural, boolean ignoreConditionalPrefixes) {
		String ret = "";
		try {
			String[] transformation = animals[index];
			if (!plural) {
				ret = (prefix && !transformation[0].equals("") && !(ignoreConditionalPrefixes && transformation[0].contains("*")) ? transformation[0].replaceAll("\\*", "") + " " : "") + transformation[1];
			} else {
				if (transformation[3] != null)
					ret = transformation[1].substring(0, transformation[1].length() - Integer.parseInt(transformation[3])) + transformation[2];
				else
					ret = transformation[1] + transformation[2];
			}
		} catch (Exception ignored) {
		}
		return !lower_case ? ret : ret.toLowerCase();
	}

	public static String getRandomTransformation() {
		return getRandomTransformation(false, false, false, true);
	}

	public static String getRandomTransformation(boolean lower_case, boolean prefix, boolean plural, boolean ignoreConditionalPrefixes) {
		int index = Helper.getRandomInt(0, animals.length - 1);
		return getTransformationByIndex(index, lower_case, prefix, plural, ignoreConditionalPrefixes);
	}

	public static int getWarpLocationCount() {
		return warpLocations.length;
	}

	public static String getWarpLocationByIndex(int index) {
		return getWarpLocationByIndex(index, false);
	}

	public static String getWarpLocationByIndex(int index, boolean lower_case) {
		String ret = PotionHelper.replaceParamsInEffectString(warpLocations[index], "");
		return !lower_case ? ret : ret.toLowerCase();
	}

	public static String getRandomWarpLocation() {
		return getRandomWarpLocation(false);
	}

	public static String getRandomWarpLocation(boolean lower_case) {
		int index = Helper.getRandomInt(0, warpLocations.length - 1);
		return getWarpLocationByIndex(index, lower_case);
	}

	public static int getCodeWordCount() {
		return codeWords.length;
	}

	public static String getRandomCodeWord() {
		return codeWords[Helper.getRandomInt(0, codeWords.length - 1)];
	}

	public static String[][] getRandomSmashTarget() {
		return smashTargets.get(Helper.getRandomInt(0, smashTargets.size() - 1));
	}

	public static String getRandomSmashString() {
		return getRandomSmashString(true, true);
	}

	public static String getRandomSmashString(boolean prefix) {
		return getRandomSmashString(prefix, true);
	}

	public static String getRandomSmashString(boolean prefix, boolean toLowerCase) {
		String[][] target = getRandomSmashTarget();
		String[] results = target[1];
		String result = results[Helper.getRandomInt(0, results.length - 1)];
		String output = "";
		if (prefix)
			output += (toLowerCase ? target[0][0].toLowerCase() : target[0][0]) + " ";
		output += (toLowerCase || prefix ? target[0][1].toLowerCase() : target[0][1]) + ". " + result;
		return PotionHelper.replaceParamsInEffectString(output);
	}
}