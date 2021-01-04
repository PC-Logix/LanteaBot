package pcl.lc.irc.entryClasses;

public class EffectActionParameters {
	public String targetName;
	public String triggererName;
	public boolean isSplash;
	public boolean isNew;

	/**
	 *
	 * @param targetName The username of the target
	 * @param triggererName The username of the potion user (if not provided this is set to targetName, for example when drinking potion instead of splashing)
	 * @param isSplash Whether potion is being thrown or not
	 * @param isNew Whether potion is new or not
	 */
	public EffectActionParameters(String targetName, String triggererName, boolean isSplash, boolean isNew) {
		this.targetName = targetName;
		if (triggererName == null)
			this.triggererName = targetName;
		else
			this.triggererName = triggererName;
		this.isSplash = isSplash;
		this.isNew = isNew;
	}
}
