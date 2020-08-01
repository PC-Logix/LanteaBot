package pcl.lc.irc;

import java.sql.Timestamp;
import java.util.HashMap;

public class CommandRateLimit {
	private int limit;
	private long lastExecution;
	private boolean perUserLimit;
	private boolean ignorePermissions;
	private HashMap<String, Long> lastExecutionPerUser;

	public CommandRateLimit(int limitSeconds) {
		this(0, 0, limitSeconds);
	}

	public CommandRateLimit(int limitHours, int limitMinutes, int limitSeconds) {
		this(limitHours, limitMinutes, limitSeconds, false, false);
	}

	public CommandRateLimit(int limitSeconds, boolean perUserLimit) {
		this(0, 0, limitSeconds, perUserLimit, false);
	}

	public CommandRateLimit(int limitSeconds, boolean perUserLimit, boolean ignorePermissions) {
		this(0, 0, limitSeconds, perUserLimit, ignorePermissions);
	}

	public CommandRateLimit(int limitHours, int limitMinutes, int limitSeconds, boolean perUserLimit, boolean ignorePermissions) {
		this.lastExecution = 0;
		this.lastExecutionPerUser = new HashMap<>();
		this.limit = limitSeconds + (limitMinutes * 60) + (limitHours * 60 * 60);
		this.perUserLimit = perUserLimit;
		this.ignorePermissions = ignorePermissions;
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

	public long getHeatValue(String user) {
		if (!perUserLimit) {
			if (this.lastExecution == 0)
				return 0;
			long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
			long difference = timestamp - this.lastExecution;
			long time = (this.limit * 1000) - difference;
			return Math.max(0, time);
		} else {
			if (!this.lastExecutionPerUser.containsKey(user))
				return 0;
			else {
				long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
				long difference = timestamp - this.lastExecutionPerUser.get(user);
				long time = (this.limit * 1000) - difference;
				return Math.max(0, time);
			}
		}
	}

	public boolean isCooledDown(String user) {
		return getHeatValue(user) == 0;
	}

	public void updateLastExecution(String user) {
		if (this.perUserLimit)
			this.lastExecutionPerUser.put(user, new Timestamp(System.currentTimeMillis()).getTime());
		else
			this.lastExecution = new Timestamp(System.currentTimeMillis()).getTime();
	}

	public void setLastExecution(String user, long lastExecution) {
		if (this.perUserLimit)
			this.lastExecutionPerUser.put(user, lastExecution);
		else
			this.lastExecution = lastExecution;
	}

	public long getLastExecution(String user) {
		if (this.perUserLimit) {
			if (this.lastExecutionPerUser.containsKey(user))
				return this.lastExecutionPerUser.get(user);
			return 0;
		}
		return this.lastExecution;
	}

	public Timestamp getLastExecutionTimestamp(String user) {
		if (this.perUserLimit) {
			if (this.lastExecutionPerUser.containsKey(user))
				return new Timestamp(this.lastExecutionPerUser.get(user));
			return new Timestamp(0);
		}
		return new Timestamp(this.lastExecution);
	}

	public boolean getIgnorePermissions() {
		return this.ignorePermissions;
	}

	public void setIgnorePermissions(boolean ignorePermissions) {
		this.ignorePermissions = ignorePermissions;
	}
}
