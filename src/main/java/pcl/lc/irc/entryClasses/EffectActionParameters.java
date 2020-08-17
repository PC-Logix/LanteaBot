package pcl.lc.irc.entryClasses;

public class EffectActionParameters {
	public String targetName;
	public String triggererName;
	public boolean isSplash;

	public EffectActionParameters(String targetName, String triggererName, boolean isSplash) {
		this.targetName = targetName;
		this.triggererName = triggererName;
		this.isSplash = isSplash;
	}
}
