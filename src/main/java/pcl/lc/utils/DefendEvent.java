package pcl.lc.utils;

import pcl.lc.irc.hooks.Defend;

import java.util.Date;

public class DefendEvent {
	public String triggerer;
	public String target;
	public Date time;
	public int damage;
	public String implement;
	public Defend.EventTypes type;

	public DefendEvent(String triggerer, String target, Date time, int damage, String implement, Defend.EventTypes type) {
		this.triggerer = triggerer;
		this.target = target;
		this.time = time;
		this.damage = damage;
		this.implement = implement;
		this.type = type;
	}

	public DefendEvent(String triggerer, String target, Date time, String implement, Defend.EventTypes type) {
		this.triggerer = triggerer;
		this.target = target;
		this.time = time;
		this.damage = 0;
		this.implement = implement;
		this.type = type;
	}
}
