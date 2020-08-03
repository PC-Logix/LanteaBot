package pcl.lc.irc.entryClasses;

import java.util.function.Function;

public class EffectEntry {
	public String effectDrink;
	public String effectSplash;
	public String discoverer;
	public Function<String, String> action;

	public EffectEntry(String effectDrink, String effectSplash, Function<String, String> action, String discoverer) {
		this.effectDrink = effectDrink;
		this.effectSplash = effectSplash;
		this.action = action;
		this.discoverer = discoverer;
	}
	public EffectEntry(String effectDrink, String effectSplash, Function<String, String> action) {
		this(effectDrink, effectSplash, action, null);
	}
	public EffectEntry(String effectDrink, String effectSplash) {
		this(effectDrink, effectSplash, null, null);
	}
	public EffectEntry(String effectDrink, Function<String, String> action) {
		this(effectDrink, null, action, null);
	}
	public EffectEntry(String effectDrink) {
		this(effectDrink, null, null, null);
	}

	@Override
	public String toString() {
		return effectDrink;
	}
}
