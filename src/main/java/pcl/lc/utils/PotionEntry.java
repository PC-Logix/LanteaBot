package pcl.lc.utils;

import org.jvnet.inflector.Noun;
import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.utils.Exceptions.InvalidPotionException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PotionEntry {
	public AppearanceEntry consistency;
	public AppearanceEntry appearance;
	public boolean isNew;
	public EffectEntry effect;

	public PotionEntry() {
	}

	public PotionEntry(AppearanceEntry consistency, AppearanceEntry appearance, boolean isNew) {
		this.consistency = consistency;
		this.appearance = appearance;
		this.isNew = isNew;
		System.out.println("Created PotionEntry (isNew: " + isNew + ")");
	}

	public void setFromCommandParameters(ArrayList<String> params) throws InvalidPotionException {
		setFromCommandParameters(String.join(" ", params));
	}

	public void setFromCommandParameters(String params) throws InvalidPotionException {
		PotionEntry potion = setFromString(params);

		if (potion == null)
			throw new InvalidPotionException();

		this.consistency = potion.consistency;
		this.appearance = potion.appearance;
		this.isNew = potion.isNew;
	}

	public EffectEntry getEffect(String user) {
		return getEffect(user, false);
	}

	public EffectEntry getEffect(String user, boolean splash) {
		if (this.effect != null)
			return this.effect;
		else {
			if (appearance == null || consistency == null) {
				return null;
			}
			System.out.println("Con: '" + consistency.getName() + "', App: '" + appearance.getName() + "'");
			String key = PotionHelper.getCombinationKey(consistency, appearance);

			PotionHelper.tryResetPotionList();

			if (PotionHelper.combinationHasEffect(key)) {
				this.isNew = false;
				this.effect = PotionHelper.getCombinationEffect(key);
				System.out.println("Effect recorded for " + key + ": " + this.effect);
			} else {
				int min = 0;
				int max = DrinkPotion.effects.size() - 1;
				if (consistency.getName().toLowerCase().equals("mutable")) {
					min = 1;
					max = 6;
				}
				int effect = Helper.getRandomInt(min, max);
				System.out.println("No effect recorded for " + key + ", Assign effect with index: " + effect);

				String effectp = null;
				EffectEntry eff = DrinkPotion.effects.get(effect);

				if (splash) {
					try {
						effectp = eff.effectSplash;
					} catch (Exception ignored) {
					}
				}

				if (effectp == null)
					effectp = eff.effectDrink;

				effectp = PotionHelper.replaceParamsInEffectString(effectp);

				try {
					Pattern pattern = Pattern.compile("\\{appearance:(.*):(p?)}");
					Matcher matcher = pattern.matcher(effectp);
					while (matcher.find()) {
						String appearance_item = matcher.group(1);
						boolean use_prefix = false;
						if (matcher.group(2).equals("p"))
							use_prefix = true;
						effectp = effectp.replace(matcher.group(0), PotionHelper.getAppearance().appearanceItem(appearance_item, use_prefix));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				this.isNew = true;
				this.effect = new EffectEntry(effectp, user, eff.action, user);
				PotionHelper.setCombinationEffect(consistency, appearance, this.effect);
			}
			return this.effect;
		}
	}

	public static PotionEntry setFromString(String str) {
		if (str == null)
			return null;
		AppearanceEntry consistency = PotionHelper.findConsistencyInString(str);
		AppearanceEntry appearance = PotionHelper.findAppearanceInString(str);

		if (!str.toLowerCase().contains("potion"))
			return null;
		if (consistency == null) {
			consistency = PotionHelper.getConsistency();
			System.out.println("No consistency found in '" + str + "'. Using '" + consistency.Name + "'");
		}
		if (appearance == null) {
			appearance = PotionHelper.getAppearance();
			System.out.println("No appearance found in '" + str + "'. Using '" + appearance.Name + "'");
		}
		return new PotionEntry(consistency, appearance, !PotionHelper.combinationHasEffect(consistency, appearance));
	}
}
