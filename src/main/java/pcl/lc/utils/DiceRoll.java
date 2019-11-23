package pcl.lc.utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for returning dice roll results
 * Created by Forecaster on 2017-03-10.
 */
public class DiceRoll {
    private ArrayList<Integer> results;
    private String resultString;
    private int sum;

    public DiceRoll(String dice) throws Exception {
        final String regex = "(\\d*)d(\\d+)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(dice);

        if (matcher.matches()) {
            int num_dice = (matcher.group(1).equals("") ? 1 : Integer.valueOf(matcher.group(1)));
            num_dice = Math.min(100, num_dice);
            int dice_size = Integer.valueOf(matcher.group(2));

            int sum = 0;
            ArrayList<Integer> results = new ArrayList<>(100);
            for (int i = 0; i < num_dice; i++)
            {
                int steps = Helper.getRandomInt(1, dice_size);
                int gone = 0;
                int result;
                for (result = 1; gone < steps; gone++)
                {
                    if (Objects.equals(result, dice_size))
                        result = 0;
                    result++;
                }
                results.add(result);
                sum += result;
            }
            SetResults(results, sum);
        } else {
            throw new Exception("Invalid dice format (Eg 1d6)");
        }
    }

    public DiceRoll(ArrayList<Integer> results) {
        this.SetResults(results, null);
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

    public static String rollDiceInString(String input) {
        return rollDiceInString(input, 100);
    }

    public static String rollDiceInString(String input, int maxIteration) {
        int i = 0;
        Pattern dicePattern = Pattern.compile("(\\d*)d(\\d+)");

        while (i < maxIteration) {
            Matcher matcher = dicePattern.matcher(input);

            if (!matcher.find())
                break;
            int startIndex = matcher.start();
            int endIndex = matcher.end();
            if (!matcher.group(1).equals("")) {
                try {
                    DiceRoll roll = new DiceRoll(matcher.group(1) + "d" + matcher.group(2));
                    ArrayList<Integer> results = roll.getResults();
                    ArrayList<String> resultsConverted = new ArrayList<>();
                    for (Integer in : results) {
                        resultsConverted.add(String.valueOf(in));
                    }
                    String insert = "";
                    if (Integer.parseInt(matcher.group(1)) > 1)
                        insert = "[" + String.join(",", resultsConverted) + "]";
                    else if (Integer.parseInt(matcher.group(1)) == 0)
                        insert = "0";
                    else
                        insert = String.valueOf(roll.getSum());
                    input = Helper.replaceSubstring(input, insert, startIndex, endIndex);
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
}

