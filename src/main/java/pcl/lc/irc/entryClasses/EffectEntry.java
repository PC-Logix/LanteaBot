package pcl.lc.irc.entryClasses;

import pcl.lc.utils.Helper;
import pcl.lc.utils.PotionHelper;

import java.util.function.Function;

public class EffectEntry {
	public String key;
	public String effectDrink;
	public String effectSplash;
	public String effectDrinkDiscovered;
	public String effectSplashDiscovered;
	public String discoverer;
	public int baseUses;
	public int totalUses = -1;
	public int usedUses = 0;
	private static final int usesVariance = 2;

	public Function<EffectActionParameters, String> action;

	public EffectEntry(String key, String effectDrink, String effectSplash, int baseUses) {
		this(key, effectDrink, effectSplash, null, null, null, baseUses);
	}

	public EffectEntry(String key, String effectDrink, String effectSplash) {
		this(key, effectDrink, effectSplash, null, null, null);
	}

	public EffectEntry(String key, String effectDrink, String effectSplash, Function<EffectActionParameters, String> action, int baseUses) {
		this(key, effectDrink, effectSplash, null, null, action, baseUses);
	}

	public EffectEntry(String key, String effectDrink, Function<EffectActionParameters, String> action, int baseUses) {
		this(key, effectDrink, null, null, null, action, baseUses);
	}

	public EffectEntry(String key, String effectDrink, String effectSplash, Function<EffectActionParameters, String> action) {
		this(key, effectDrink, effectSplash, null, null, action);
	}

	public EffectEntry(String key, String effectDrink, Function<EffectActionParameters, String> action) {
		this(key, effectDrink, null, null, null, action);
	}

	public EffectEntry(String key, String effectDrink, int baseUses) {
		this(key, effectDrink, null, null, null, null, baseUses);
	}

	public EffectEntry(String key, String effectDrink) {
		this(key, effectDrink, null, null, null, null);
	}

	public EffectEntry(String key, String effectDrink, String effectSplash, String effectDrinkDiscovered, String effectSplashDiscovered, Function<EffectActionParameters, String> action) {
		this(key, effectDrink, effectSplash, effectDrinkDiscovered, effectSplashDiscovered, action, -1);
	}

	public EffectEntry(String key, String effectDrink, String effectSplash, String effectDrinkDiscovered, String effectSplashDiscovered, Function<EffectActionParameters, String> action, int baseUses) {
		this.key = key;
		this.effectDrink = effectDrink;
		this.effectSplash = effectSplash;
		this.effectDrinkDiscovered = effectDrinkDiscovered;
		this.effectSplashDiscovered = effectSplashDiscovered;
		this.action = action;
		this.baseUses = baseUses;
	}

	public String getEffectString(String target) {
		return getEffectString(target, null, false);
	}

	public String getEffectString(String target, String trigger) {
		return getEffectString(target, trigger, false);
	}

	public String getEffectString(String target, String trigger, boolean splash) {
		if (trigger == null)
			trigger = "";
		String[] effects;
		if (effectDrink != null && effectSplash != null)
			effects = new String[] { effectDrink, effectSplash };
		else if (effectDrink != null)
			effects = new String[] { effectDrink };
		else
			effects = new String[] { "No effect." };
		effects = PotionHelper.replaceParamsInEffectString(effects, target, trigger);
		if (splash && effects.length > 1)
			return effects[1];
		return effects[0];
	}

	public String getEffectStringDiscovered(String target) {
		return getEffectStringDiscovered(target, null, false);
	}

	public String getEffectStringDiscovered(String target, String trigger) {
		return getEffectStringDiscovered(target, trigger, false);
	}

	public String getEffectStringDiscovered(String target, String trigger, boolean splash) {
		if (trigger == null)
			trigger = "";
		String[] effects;
		if (effectDrinkDiscovered != null && effectSplashDiscovered != null)
			effects = new String[] { effectDrinkDiscovered, effectSplashDiscovered };
		else if (effectDrinkDiscovered != null)
			effects = new String[] { effectDrinkDiscovered };
		else if (effectDrink != null && effectSplash != null)
			effects = new String[] { effectDrink, effectSplash };
		else if (effectDrink != null)
			effects = new String[] { effectDrink };
		else
			effects = new String[] { "No effect." };
		effects = PotionHelper.replaceParamsInEffectString(effects, target, trigger);
		if (splash && effects.length > 1)
			return effects[1];
		return effects[0];
	}

	public EffectEntry copy() {
		EffectEntry copy = new EffectEntry(this.key, this.effectDrink, this.effectSplash, this.effectDrinkDiscovered, this.effectSplashDiscovered, this.action, this.baseUses);
		copy.discoverer = this.discoverer;
		return copy;
	}

	public void setTotalUses() { setTotalUses(false); }

	public void setTotalUses(boolean reset) {
		if (reset || this.totalUses == -1)
			this.totalUses = Math.max(1, this.baseUses + Helper.getRandomInt(usesVariance*-1, usesVariance));
	}

	public int usesRemaining() {
		setTotalUses();
		return this.totalUses - this.usedUses;
	}

	public String usesRemainingOutOf() {
		setTotalUses();
		return usesRemaining() + "/" + this.totalUses + "(" + this.baseUses + "±" + usesVariance + ")";
	}

	public boolean hasUsesRemaining() {
		return usesRemaining() > 0;
	}

	public boolean use() {
		if (!hasUsesRemaining())
			return false;
		this.usedUses++;
		return true;
	}

	/**
	 *
	 * @param params The parameters passed to the action function
	 * @return Return a string to be sent instead of regular potion message, or null if no action function is defined.
	 */
	public String doAction(EffectActionParameters params) {
		if (this.action != null) {
			if (this.use()) {
				String actionString = this.action.apply(params);
				if (actionString != null)
					return actionString + " (Rem. uses: " + this.usesRemaining() + ")";
				return params.targetName + ": Nothing seemed to happen...";
			}
			return params.targetName + ": The magic of this potion seems to be depleted...";
		}
		return null;
	}
}
