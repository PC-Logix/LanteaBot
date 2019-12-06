package pcl.lc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum ExplodeMode {
    NONE,
    EXPLODE_SEPARATE,
    EXPLODE_SUMMARIZE
}

enum SuccessMode {
    NONE,
    HIGHER,
    LOWER
}

/**
 * A class for returning dice roll results
 * Created by Forecaster on 2017-03-10.
 */
public class DiceRoll {
    public int diceCount;
    public int diceSize;
    private ArrayList<Integer> results;
    private String resultString;
    private int sum;
    public ExplodeMode explodeMode;

    public DiceRoll(String dice) throws Exception {
        this(getDiceCountFromString(dice), getDiceSizeFromString(dice));
    }

    public DiceRoll(int diceSize) {
        this(1, diceSize);
    }

    public DiceRoll(int diceCount, int diceSize) {
        this(diceCount, diceSize, ExplodeMode.NONE);
    }

    public DiceRoll(int diceCount, int diceSize, ExplodeMode explodeMode) {
        System.out.println("Roll between " + diceCount + " and " + diceSize);
        this.diceCount = diceCount;
        this.diceSize = diceSize;
        this.explodeMode = explodeMode;
        int sum = 0;
        ArrayList<Integer> results = new ArrayList<>();
        if (diceSize == 1) {
            for (int i = 0; i < diceCount; i++) {
                results.add(1);
                sum += 1;
            }
        } else {
            for (int i = 0; i < diceCount; i++) {
                int steps = Helper.getRandomInt(1, diceSize);
                int gone = 0;
                int result;
                for (result = 1; gone < steps; gone++) {
                    if (Objects.equals(result, diceSize))
                        result = 0;
                    result++;
                }
                if (result == diceSize) {
                    if (explodeMode.equals(ExplodeMode.EXPLODE_SUMMARIZE))
                        result += new DiceRoll(1, diceSize, ExplodeMode.EXPLODE_SUMMARIZE).getSum();
                    else if (explodeMode.equals(ExplodeMode.EXPLODE_SEPARATE)) {
                        diceCount++;
                    }
                }
                results.add(result);
                sum += result;
            }
        }
        SetResults(results, sum);
    }

    public DiceRoll(ArrayList<Integer> results) {
        this.SetResults(results, null);
    }

    public static int getDiceCountFromString(String diceString) throws Exception {
        final String regex = "(\\d*)d(\\d+)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(diceString);

        if (matcher.matches()) {
            int num_dice = (matcher.group(1).equals("") ? 1 : Integer.valueOf(matcher.group(1)));
            num_dice = Math.min(100, num_dice);

            return num_dice;
        } else {
            throw new Exception("Invalid dice format (Eg 1d6)");
        }
    }

    public static int getDiceSizeFromString(String diceString) throws Exception {
        final String regex = "(\\d*)d(\\d+)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(diceString);

        if (matcher.matches()) {
            return Integer.valueOf(matcher.group(2));
        } else {
            throw new Exception("Invalid dice format (Eg 1d6)");
        }
    }

    public void SetResults(ArrayList<Integer> results) { SetResults(results, null); }

    public void SetResults(ArrayList<Integer> results, Integer sum) {
        this.results = results;

        if (sum == null) {
            this.sum = 0;
            for (int roll : results)
                this.sum += roll;
        }
        else
            this.sum = sum;
        this.resultString = results.toString();
    }

    public String getResultString() {
        return getResultString(true);
    }

    public String getResultString(boolean includeSum) {
        return this.resultString + ((results.size() > 1 && includeSum) ? " = " + this.sum : "");
    }

    public int getSum() {
        return this.sum;
    }

    public ArrayList<Integer> getResults() {
        return this.results;
    }

    @Override
    public String toString() {
        return this.resultString;
    }

    private static int maxIteration = 100;
    public static String rollDiceInString(String input) {
        return rollDiceInString(input, maxIteration);
    }

    public static String rollDiceInString(String input, boolean includeOriginalStringBeforeResult) {
        return rollDiceInString(input, maxIteration, includeOriginalStringBeforeResult);
    }

    public static String rollDiceInString(String input, int maxIteration) {
        return rollDiceInString(input, maxIteration, false);
    }

    public static String rollDiceInString(String input, int maxIteration, boolean includeOriginalStringBeforeResult) {
        int i = 0;
        Pattern dicePattern = Pattern.compile("(\\d*)d(\\d+)(?:kh?(\\d+))?(?:kl(\\d+))?(!?!?)(?:(<?>?)(\\d+))?");

        while (i < maxIteration) {
            String appendToOutput = "";
            Matcher matcher = dicePattern.matcher(input);

            if (!matcher.find())
                break;
            int startIndex = matcher.start();
            int endIndex = matcher.end();
            if (!matcher.group(1).equals("")) {
                try {
                    ExplodeMode explodeMode = (matcher.group(5).equals("!!") ? ExplodeMode.EXPLODE_SUMMARIZE : (matcher.group(5).equals("!") ? ExplodeMode.EXPLODE_SEPARATE : ExplodeMode.NONE));
                    DiceRoll roll = new DiceRoll(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), explodeMode);
                    ArrayList<Integer> results = roll.getResults();

                    if (matcher.group(3) != null) {
                        ArrayList<Integer>[] keepHighest = DiceRoll.keepHighest(roll, Integer.parseInt(matcher.group(3)));
                        results = keepHighest[0];
                        appendToOutput = " dropped [" + String.join(",", Helper.covertIntegerListToStringList(keepHighest[1])) + "]";
                    } else if (matcher.groupCount() > 3 && matcher.group(4) != null) {
                        ArrayList<Integer>[] keepLowest = DiceRoll.keepLowest(roll, Integer.parseInt(matcher.group(4)));
                        results = keepLowest[0];
                        appendToOutput = " dropped [" + String.join(",", Helper.covertIntegerListToStringList(keepLowest[1])) + "]";
                    }
                    if (matcher.groupCount() > 5 && matcher.group(6) != null) {
                        SuccessMode successMode = matcher.group(6).equals(">") ? SuccessMode.HIGHER : SuccessMode.LOWER;
                        int targetResult = Integer.parseInt(matcher.group(7));
                        int successes = 0;

                        for (Integer result : results) {
                            if (successMode.equals(SuccessMode.HIGHER) && result >= targetResult)
                                successes++;
                            else if (successMode.equals(SuccessMode.LOWER) && result <= targetResult)
                                successes++;
                        }
                        appendToOutput = " => " + successes + " successes";
                    }

                    ArrayList<String> resultsConverted = new ArrayList<>();
                    for (Integer in : results) {
                        resultsConverted.add(String.valueOf(in));
                    }
                    String insert;
                    if (Integer.parseInt(matcher.group(1)) > 1)
                        insert = "[" + String.join(",", resultsConverted) + "]" + appendToOutput;
                    else if (Integer.parseInt(matcher.group(1)) == 0)
                        insert = "0";
                    else
                        insert = String.valueOf(roll.getSum());
                    input = Helper.replaceSubstring(input, (includeOriginalStringBeforeResult ? matcher.group(0).replace("d", "d\u200B") + " => " : "") + insert, startIndex, endIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try { input = Helper.replaceSubstring(input, String.valueOf(new DiceRoll(matcher.group(0)).getSum()), startIndex, endIndex); } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
        return input;
    }

    /** @noinspection Duplicates*/
    public static ArrayList<Integer>[] keepHighest(DiceRoll diceRoll, int keep) {
        ArrayList<Integer> res = diceRoll.results;
        Collections.sort(res);
        Collections.reverse(res);

        ArrayList<Integer> keepRolls = new ArrayList<>();
        ArrayList<Integer> dropRolls = new ArrayList<>();

        for (int i = 0; i < res.size(); i++) {
            if (i < keep)
                keepRolls.add(res.get(i));
            else
                dropRolls.add(res.get(i));
        }

        return new ArrayList[] { keepRolls, dropRolls };
    }

    /** @noinspection Duplicates*/
    public static ArrayList<Integer>[] keepLowest(DiceRoll diceRoll, int keep) {
        ArrayList<Integer> res = diceRoll.results;
        Collections.sort(res);

        ArrayList<Integer> keepRolls = new ArrayList<>();
        ArrayList<Integer> dropRolls = new ArrayList<>();

        for (int i = 0; i < res.size(); i++) {
            if (i < keep)
                keepRolls.add(res.get(i));
            else
                dropRolls.add(res.get(i));
        }

        return new ArrayList[] { keepRolls, dropRolls };
    }
}

