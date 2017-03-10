package pcl.lc.irc;

import java.util.ArrayList;

/**
 * A class for returning dice roll results
 * Created by Forecaster on 2017-03-10.
 */
public class DiceRoll {
    private ArrayList<Integer> results;
    private String resultString;
    private int sum;

    public DiceRoll(ArrayList<Integer> results) {
        this.results = results;
        this.sum = 0;
        for (int roll : results)
            this.sum += roll;
        this.resultString = results.toString() + ((results.size() > 1) ? " = " + this.sum : "");
    }

    public DiceRoll(ArrayList<Integer> results, int sum) {
        this.results = results;
        this.sum = sum;
    }

    public String getResultString() {
        return this.resultString;
    }

    public int getSum() {
        return this.sum;
    }

    public ArrayList<Integer> getResults() {
        return this.results;
    }
}
