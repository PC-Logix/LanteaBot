package pcl.lc.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiceRollBonusCollection {
    public boolean incapable;
    public HashMap<String, Integer> bonuses;

    public DiceRollBonusCollection(HashMap<String, Integer> bonuses) {
        this.incapable = false;
        this.bonuses = bonuses;
    }

    public DiceRollBonusCollection() {
        incapable = false;
        bonuses = new HashMap<>();
    }

    public void addBonus(String bonusName, int modifier) {
        if (bonuses.containsKey(bonusName)) {
            int mod = bonuses.get(bonusName);
            mod += modifier;
            bonuses.put(bonusName, mod);
        } else {
            bonuses.put(bonusName, modifier);
        }
    }

    public int getTotal() {
        final int[] total = {0};
        bonuses.forEach((s, integer) -> {
            total[0] += integer;
        });
        return total[0];
    }

    @Override
    public String toString() {
        ArrayList<String> output = new ArrayList<>();
        bonuses.forEach((s, integer) -> {
            if (integer != 0)
                output.add(s + " " + (integer > 0 ? "+" + integer : integer));
        });
        return String.join(", ", output);
    }

    public int size() {
        return bonuses.size();
    }

    public int length() {
        return size();
    }

    private static DiceRollBonusCollection searchForBonuses(DiceRollBonusCollection bonusCollection, List<String> matchCollection, String itemName, int modifier) {
        for (String str : matchCollection)
            if (itemName.contains(str.toLowerCase()))
                bonusCollection.addBonus(str, modifier);
        return bonusCollection;
    }

    public static DiceRollBonusCollection getOffensiveItemBonus(Item item)
    {
        return getOffensiveItemBonus(item.getName());
    }

    public static DiceRollBonusCollection getOffensiveItemBonus(String itemName)
    {
        DiceRollBonusCollection bonuses = new DiceRollBonusCollection();
        itemName = itemName.toLowerCase();

        List<String> noDamage = new ArrayList<>();
        noDamage.add("Discharged ");
        noDamage.add("Friendly ");
        noDamage.add("Baby");
        noDamage.add("Fake");
        noDamage.add("Artificial ");
        noDamage.add("Replica ");

        for (String str : noDamage)
            if (itemName.contains(str.toLowerCase()))
                bonuses.incapable = true;

        List<String> minusTwo = new ArrayList<>();
        minusTwo.add("broken ");

        searchForBonuses(bonuses, minusTwo, itemName, -2);

        List<String> minusOne = new ArrayList<>();
        minusOne.add("Ripped ");
        minusOne.add("Fragile ");
        minusOne.add("Crumbling ");
        minusOne.add("Dull ");
        minusOne.add("Stuffed ");
        minusOne.add("Soft ");
        minusOne.add("Fluffy ");
        minusOne.add("Ill ");
        minusOne.add("Plush");
        minusOne.add("Defeat ");
        minusOne.add("Depress");
        minusOne.add("Artificial ");
        minusOne.add("Power");
        minusOne.add("Damaged ");

        searchForBonuses(bonuses, minusOne, itemName, -1);

        List<String> plusOne = new ArrayList<>();
        plusOne.add("Heavy ");
        plusOne.add("Blunt ");
        plusOne.add("Pointy ");
        plusOne.add("Charged ");
        plusOne.add("Kitten ");
        plusOne.add("Poison");
        plusOne.add("Double ");
        plusOne.add("Bees ");
        plusOne.add("Military grade ");
        plusOne.add("Shark ");
        plusOne.add("Bear ");
        plusOne.add("Tiger ");
        plusOne.add("Lion ");

        searchForBonuses(bonuses, plusOne, itemName, 1);

        List<String> plusTwo = new ArrayList<>();
        plusTwo.add("Sharp");
        plusTwo.add("Weighted ");
        plusTwo.add("Dangerous");
        plusTwo.add("Special ");
        plusTwo.add("Cat ");
        plusTwo.add("Super ");
        plusTwo.add("Magic");
        plusTwo.add("Orbital ");
        plusTwo.add("Vorpal ");
        plusTwo.add("Chicken ");
        plusTwo.add("Nuclear ");
        plusTwo.add("Hippo");

        searchForBonuses(bonuses, plusTwo, itemName, 2);

        return bonuses;
    }

    public static DiceRollBonusCollection getDefensiveItemBonus(Item item)
    {
        return getDefensiveItemBonus(item.getName());
    }

    public static DiceRollBonusCollection getDefensiveItemBonus(String itemName)
    {
        DiceRollBonusCollection bonuses = new DiceRollBonusCollection();
        itemName = itemName.toLowerCase();

        List<String> noDefense = new ArrayList<>();
        noDefense.add("Paper ");
        noDefense.add("Fragile ");
        noDefense.add("Artificial ");
        noDefense.add("Replica ");
        noDefense.add("Fake");

        for (String str : noDefense)
            if (itemName.contains(str.toLowerCase()))
                bonuses.incapable = true;

        List<String> minusTwo = new ArrayList<>();
        minusTwo.add("Broken ");

        searchForBonuses(bonuses, minusTwo, itemName, -2);

        List<String> minusOne = new ArrayList<>();
        minusOne.add("Ripped ");
        minusOne.add("Fragile ");
        minusOne.add("Crumbling ");
        minusOne.add("Dull");
        minusOne.add("Soft ");
        minusOne.add("Ill ");
        minusOne.add("Plush ");
        minusOne.add("Defeat");
        minusOne.add("Depress");
        minusOne.add("Artificial ");
        minusOne.add("Power");
        minusOne.add("Damaged ");

        searchForBonuses(bonuses, minusOne, itemName, -1);

        List<String> plusOne = new ArrayList<>();
        plusOne.add("Hard");
        plusOne.add("Solid ");
        plusOne.add("Rugged ");
        plusOne.add("Charged ");
        plusOne.add("Defensive ");
        plusOne.add("Military grade ");

        searchForBonuses(bonuses, plusOne, itemName, 1);

        List<String> plusTwo = new ArrayList<>();
        plusTwo.add("Reinforced ");
        plusTwo.add("Shielded ");
        plusTwo.add("Robust ");
        plusTwo.add("Super");
        plusTwo.add("Magic");

        searchForBonuses(bonuses, plusTwo, itemName, 2);

        return bonuses;
    }

    public static DiceRollBonusCollection getHealingItemBonus(Item item) {
        return getHealingItemBonus(item.getName());
    }

    public static DiceRollBonusCollection getHealingItemBonus(String itemName)
    {
        DiceRollBonusCollection bonuses = new DiceRollBonusCollection();
        itemName = itemName.toLowerCase();

        List<String> noHeal = new ArrayList<>();
        noHeal.add("Plague");
        noHeal.add("Ill ");
        noHeal.add("Sick ");
        noHeal.add("Infected ");
        noHeal.add("Corrupt");
        noHeal.add("Fake");
        noHeal.add("Replica ");

        for (String str : noHeal)
            if (itemName.contains(str.toLowerCase()))
                bonuses.incapable = true;

        List<String> minusTwo = new ArrayList<>();
        minusTwo.add("broken");

        searchForBonuses(bonuses, minusTwo, itemName, -2);


        List<String> minusOne = new ArrayList<>();
        minusOne.add("Ripped ");
        minusOne.add("Crumbling ");
        minusOne.add("Ill ");
        minusOne.add("Plush ");
        minusOne.add("Defeat ");
        minusOne.add("Depress");
        minusOne.add("Artificial ");
        minusOne.add("Damage");
        minusOne.add("Attack ");
        minusOne.add("Hurt ");
        minusOne.add("Bees ");
        minusOne.add("Nuclear ");

        searchForBonuses(bonuses, minusOne, itemName, -1);

        List<String> plusOne = new ArrayList<>();
        plusOne.add("Friend");
        plusOne.add("Happy ");
        plusOne.add("Charged ");
        plusOne.add("Healing ");
        plusOne.add("Refreshing ");
        plusOne.add("Sugar ");
        plusOne.add("Lewd ");

        searchForBonuses(bonuses, plusOne, itemName, 1);

        List<String> plusTwo = new ArrayList<>();
        plusTwo.add("Healthy ");
        plusTwo.add("Friendly ");
        plusTwo.add("Magic");
        plusTwo.add("Super");

        searchForBonuses(bonuses, plusTwo, itemName, 2);

        return bonuses;
    }
}
