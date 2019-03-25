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
        final String regex = "(\\d\\d?\\d?)d(\\d\\d?\\d?)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(dice);

        if (matcher.matches()) {
            Integer num_dice = Math.min(100, Integer.valueOf(matcher.group(1)));
            Integer dice_size = Integer.valueOf(matcher.group(2));

            Integer sum = 0;
            ArrayList<Integer> results = new ArrayList<>(100);
            for (Integer i = 0; i < num_dice; i++)
            {
                Integer steps = Helper.getRandomInt(1, dice_size);
                Integer gone = 0;
                Integer result;
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
        System.out.println(this.resultString);
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
}

