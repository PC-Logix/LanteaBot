package pcl.lc.irc.entryClasses;

import pcl.lc.utils.PotionHelper;

import java.util.function.Function;

public class EffectEntry {
	public String effectDrink;
	public String effectSplash;
	public String effectDrinkDiscovered;
	public String effectSplashDiscovered;
	public String discoverer;
	public int usesRemaining;
	public Function<EffectActionParameters, String> action;

	public EffectEntry(String effectDrink, String effectSplash, int usesRemaining) {
		this(effectDrink, effectSplash, null, null, null, usesRemaining);
	}

	public EffectEntry(String effectDrink, String effectSplash) {
		this(effectDrink, effectSplash, null, null, null);
	}

	public EffectEntry(String effectDrink, Function<EffectActionParameters, String> action, int usesRemaining) {
		this(effectDrink, null, null, null, action, usesRemaining);
	}

	public EffectEntry(String effectDrink, Function<EffectActionParameters, String> action) {
		this(effectDrink, null, null, null, action);
	}

	public EffectEntry(String effectDrink, int usesRemaining) {
		this(effectDrink, null, null, null, null, usesRemaining);
	}

	public EffectEntry(String effectDrink) {
		this(effectDrink, null, null, null, null);
	}

	public EffectEntry(String effectDrink, String effectSplash, String effectDrinkDiscovered, String effectSplashDiscovered, Function<EffectActionParameters, String> action) {
		this(effectDrink, effectSplash, effectDrinkDiscovered, effectSplashDiscovered, action, -1);
	}

	public EffectEntry(String effectDrink, String effectSplash, String effectDrinkDiscovered, String effectSplashDiscovered, Function<EffectActionParameters, String> action, int usesRemaining) {
		this.effectDrink = effectDrink;
		this.effectSplash = effectSplash;
		this.effectDrinkDiscovered = effectDrinkDiscovered;
		this.effectSplashDiscovered = effectSplashDiscovered;
		this.action = action;
		this.usesRemaining = usesRemaining;
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
		EffectEntry copy = new EffectEntry(this.effectDrink, this.effectSplash, this.effectDrinkDiscovered, this.effectSplashDiscovered, this.action, this.usesRemaining);
		copy.discoverer = this.discoverer;
		return copy;
	}

	/**
	 *
	 * @param params The parameters passed to the action function
	 * @return Return a string to be sent instead of regular potion message, or null if no action function is defined.
	 */
	public String doAction(EffectActionParameters params) {
		if (this.action != null) {
			if (this.usesRemaining == -1 || this.usesRemaining > 0) {
				if (this.usesRemaining > 0)
					this.usesRemaining--;
				String actionString = this.action.apply(params);
				if (actionString != null)
					return actionString;
				return params.targetName + ": Nothing seemed to happen...";
			}
			return params.targetName + ": The magic of this potion seems to be depleted...";
		}
		return null;
	}
}
