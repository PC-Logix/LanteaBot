package pcl.lc.irc.entryClasses;

public class DiceTest {
	public boolean successful;
	public int DC;
	public int result;
	public int diceCount;
	public int diceSize;
	public String successLine;
	public String failLine;

	/**
	 * @param DC The target that must be met or exceeded to pass the check.
	 * @param successLine The line used when the check is successful. May contain {DC}, {dice} and {result}.
	 * @param failLine The line used when the check is failed. May contain {DC}, {dice} and {result}.
	 */
	public DiceTest(int DC, String successLine, String failLine) {
		this.successful = false;
		this.DC = DC;
		this.result = -1;
		this.diceCount = 1;
		this.diceSize = 20;
		this.successLine = successLine;
		this.failLine = failLine;
	}

	/**
	 * @param DC The target that must be met or exceeded to pass the check.
	 * @param successLine The line used when the check is successful. May contain {user], {DC}, {dice} and {result}.
	 * @param failLine The line used when the check is failed. May contain {user}, {DC}, {dice} and {result}.
	 * @param diceCount The number of dice to be rolled against the test DC
	 * @param diceSize The size of the dice to be rolled against the test DC
	 */
	public DiceTest(int DC, String successLine, String failLine, int diceCount, int diceSize) {
		this.successful = false;
		this.DC = DC;
		this.result = -1;
		this.diceCount = diceCount;
		this.diceSize = diceSize;
		this.successLine = successLine;
		this.failLine = failLine;
	}

	public boolean doCheck() {
		int roll = new DiceRoll(this.diceCount, this.diceSize).getSum();
		this.result = roll;
		if (roll >= this.DC) {
			this.successful = true;
			return true;
		}
		this.successful = false;
		return false;
	}

	public String getLine() {
		String line;
		if (this.successful)
			line = this.successLine;
		else
			line = this.failLine;

		line = line.replace("{DC}", String.valueOf(this.DC));
		line = line.replace("{dice}", (this.diceCount > 1 ? this.diceCount : "") + "d" + this.diceSize);
		if (this.result == -1)
			line = line.replace("{result}", "nothing");
		else
			line = line.replace("{result}", String.valueOf(this.result));
		return line;
	}
}
