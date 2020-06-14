package pcl.lc.irc;

import java.sql.Timestamp;

public class CommandRateLimit {
	private int limit;
	private long lastExecution;

	public CommandRateLimit(int limitSeconds) {
		this.lastExecution = 0;
		this.limit = limitSeconds;
	}

	public CommandRateLimit(int limitHours, int limitMinutes, int limitSeconds) {
		this.lastExecution = 0;
		this.limit = limitSeconds + (limitMinutes * 60) + (limitHours * 60 * 60);
	}

	public void setLimitSeconds(int seconds) {
		this.limit = seconds;
	}

	public void setLimitMinutes(int minutes) {
		setLimitSeconds(minutes * 60);
	}

	public void setLimitHours(int hours) {
		setLimitMinutes(hours * 60);
	}

	public long getHeatValue() {
		if (this.lastExecution == 0)
			return 0;
		long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
		long difference = timestamp - this.lastExecution;
		if (difference > (this.limit * 1000))
			return 0;
		return (this.limit * 1000) - difference;
	}

	public boolean isCooledDown() {
		return getHeatValue() == 0;
	}

	public void updateLastExecution() {
		this.lastExecution = new Timestamp(System.currentTimeMillis()).getTime();
	}

	public void setLastExecution(long lastExecution) {
		this.lastExecution = lastExecution;
	}

	public long getLastExecution() {
		return this.lastExecution;
	}

	public Timestamp getLastExecutionTimestamp() {
		return new Timestamp(this.lastExecution);
	}
}
