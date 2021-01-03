package pcl.lc.irc.entryClasses;

import java.util.function.Function;

public class EffectEntry {
	public String effectDrink;
	public String effectSplash;
	public String effectDrinkDiscovered;
	public String effectSplashDiscovered;
	public String discoverer;
	public Function<EffectActionParameters, String> action;

	public EffectEntry(String effectDrink, String effectSplash, Function<EffectActionParameters, String> action) {
		this.effectDrink = effectDrink;
		this.effectSplash = effectSplash;
		this.action = action;
	}
	public EffectEntry(String effectDrink, String effectSplash) {
		this(effectDrink, effectSplash, null);
	}
	public EffectEntry(String effectDrink, Function<EffectActionParameters, String> action) {
		this(effectDrink, null, action);
	}
	public EffectEntry(String effectDrink) {
		this(effectDrink, null, null);
	}

	public void setDiscovered(String discoverer, String effectDrinkDiscovered, String effectSplashDiscovered) {
		this.discoverer = discoverer;
		this.effectDrinkDiscovered = effectDrinkDiscovered;
		this.effectSplashDiscovered = effectSplashDiscovered;
	}

	public String getEffectString(boolean splash) {
		if (splash && effectSplash != null)
			return effectSplash;
		return effectDrink;
	}

	public String getEffectString() {
		return getEffectString(false);
	}

	public String getEffectStringDiscovered(boolean splash) {
		if (splash && effectSplashDiscovered != null)
			return effectSplashDiscovered;
		return effectDrinkDiscovered;
	}

	public String getEffectStringDiscovered() {
		return getEffectStringDiscovered(false);
	}

	public EffectEntry copy() {
		EffectEntry copy = new EffectEntry(this.effectDrink, this.effectSplash);
		copy.effectDrinkDiscovered = this.effectDrinkDiscovered;
		copy.effectSplashDiscovered = this.effectSplashDiscovered;
		copy.discoverer = this.discoverer;
		copy.action = this.action;
		return copy;
	}
}
