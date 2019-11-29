package pcl.lc.utils;

public class EffectEntry {
	public String Effect;
	public String Discoverer;

	public EffectEntry(String effect, String discoverer) {
		Effect = effect;
		Discoverer = discoverer;
	}

	@Override
	public String toString() {
		return Effect;
	}
}
