package pcl.lc.utils;

import java.util.ArrayList;

public class DiceRollGroup {
    private ArrayList<DiceRoll> groups;
    private String resultString;
    private int sum;

    public DiceRollGroup() {}

    public DiceRollGroup(String dice) throws Exception {
        ArrayList<DiceRoll> groups = new ArrayList<>();

        if (dice.contains(" ")) {
            String[] stringGroups = dice.split(" ");
            for (String group : stringGroups) {
                groups.add(new DiceRoll(group));
            }
        } else {
            groups.add(new DiceRoll(dice));
        }
        SetResults(groups);
    }

    public DiceRollGroup(ArrayList<DiceRoll> groups) {
        SetResults(groups);
    }

    public void SetResults(ArrayList<DiceRoll> g) { SetResults(g, null); }

    public void SetResults(ArrayList<DiceRoll> g, Integer sum) {
        this.groups = g;

        if (sum == null) {
            this.sum = 0;
            for (DiceRoll roll : g) {
                this.sum += roll.getSum();
            }
        } else {
            this.sum = sum;
        }
        System.out.println("Sum: " + this.sum);

        this.resultString = "";
        for (DiceRoll roll : g)
            this.resultString = this.resultString.concat((!this.resultString.equals("") ? " + " : "").concat(roll.getResultString(false)));
    }

    public String getResultString() {
        return getResultString(true);
    }

    public String getResultString(boolean includeSum) {
        if (includeSum)
            return this.resultString.concat(" = ").concat(String.valueOf(this.sum));
        return this.resultString;
    }

    public int getSum() {
        return this.sum;
    }

    public DiceRoll getFirstGroupOrNull() {
        if (this.groups.size() > 0)
            return this.groups.get(0);
        return null;
    }
}
