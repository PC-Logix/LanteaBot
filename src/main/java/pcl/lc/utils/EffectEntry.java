package pcl.lc.utils;

public class EffectEntry {
	public String Effect;
	public String Discoverer;

	public EffectEntry(String effect, String discoverer) {
		Effect = effect;
		Discoverer = discoverer;
	}

	public EffectEntry(String effect) {
		Effect = effect;
		Discoverer = "";
	}

	@Override
	public String toString() {
		return Effect;
	}
}
