package pcl.lc.utils;

public class DiceRollResult {
    public int rollResult;
    public DiceRollBonusCollection bonus;
    public int minValue;
    public int diceAmount;
    public int diceSize;

    public DiceRollResult(int rollResult, DiceRollBonusCollection bonus, int minValue, int diceAmount, int diceSize) {
        this.rollResult = rollResult;
        this.bonus = bonus;
        this.minValue = minValue;
        this.diceAmount = diceAmount;
        this.diceSize = diceSize;
    }

    public DiceRollResult() {
        this.rollResult = 0;
        this.bonus = new DiceRollBonusCollection();
        this.minValue = 0;
        this.diceAmount = 0;
        this.diceSize = 0;
    }

    public int getTotal() {
        return rollResult + bonus.getTotal();
    }

    public String getResultString() {
        if (diceAmount == 0 || diceSize == 0)
            return null;
        return diceAmount + "d" + diceSize + (bonus.size() > 0 ? " = " + rollResult + " + (" + bonus + ")" : "") + " = " + getTotal();
    }
}
