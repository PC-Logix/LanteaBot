package pcl.lc.irc.entryClasses;

import pcl.lc.irc.hooks.Defend;

import java.util.Date;

public class DefendEvent {
	public String triggeringUser;
	public String targetUser;
	public String target;
	public Date time;
	public int damage;
	public String implement;
	public Defend.EventTypes type;
	public String result;

	public DefendEvent(String triggeringUser, String targetUser, String target, Date time, int damage, String implement, Defend.EventTypes type, String result) {
		this.triggeringUser = triggeringUser;
		this.targetUser = targetUser;
		this.target = target;
		this.time = time;
		this.damage = damage;
		this.implement = implement;
		this.type = type;
		this.result = result;
	}
}
