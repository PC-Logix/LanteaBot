package pcl.lc.irc.entryClasses;

import pcl.lc.irc.hooks.DrinkPotion;
import pcl.lc.utils.Exceptions.InvalidPotionException;
import pcl.lc.utils.Helper;
import pcl.lc.utils.PotionHelper;

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

	public EffectEntry getEffectSplash(String targetName, String triggererName) {
		return getEffect(targetName, true, triggererName);
	}

	public EffectEntry getEffect(String targetName) {
		return getEffect(targetName, false, null);
	}

	public EffectEntry getEffect(String targetName, boolean splash, String triggererName) {
		if (this.effect == null) {
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

				EffectEntry eff = DrinkPotion.effects.get(effect);
				String effect_drink = eff.effectDrink;
				String effect_splash = eff.effectSplash != null ? eff.effectSplash : eff.effectDrink;

				if (eff.action != null) {
					EffectActionParameters parameters_drink = new EffectActionParameters(targetName, triggererName, false);
					EffectActionParameters parameters_splash = new EffectActionParameters(targetName, triggererName, true);
					effect_drink = eff.action.apply(parameters_drink);
					effect_splash = eff.action.apply(parameters_splash);
				}

				effect_drink = PotionHelper.replaceParamsInEffectString(effect_drink, null, null);
				effect_splash = PotionHelper.replaceParamsInEffectString(effect_splash, null, null);

				try {
					Pattern pattern = Pattern.compile("\\{appearance:(.*):(p?)}");
					Matcher matcher = pattern.matcher(effect_drink);
					while (matcher.find()) {
						String appearance_item = matcher.group(1);
						boolean use_prefix = false;
						if (matcher.group(2).equals("p"))
							use_prefix = true;
						effect_drink = effect_drink.replace(matcher.group(0), PotionHelper.getRandomAppearance().appearanceItem(appearance_item, use_prefix));
					}
					matcher = pattern.matcher(effect_splash);
					while (matcher.find()) {
						String appearance_item = matcher.group(1);
						boolean use_prefix = false;
						if (matcher.group(2).equals("p"))
							use_prefix = true;
						effect_splash = effect_splash.replace(matcher.group(0), PotionHelper.getRandomAppearance().appearanceItem(appearance_item, use_prefix));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				this.isNew = true;
				this.effect = eff;
				String discoverer = splash ? triggererName + " (" + targetName + ")" : targetName;
				this.effect.setDiscovered(discoverer, effect_drink, effect_splash);
				PotionHelper.setCombinationEffect(consistency, appearance, this.effect);
			}
		}
		return this.effect;
	}

	public static PotionEntry setFromString(String str) {
		if (str == null)
			return null;
		AppearanceEntry consistency = PotionHelper.findConsistencyInString(str);
		AppearanceEntry appearance = PotionHelper.findAppearanceInString(str);

		if (!str.toLowerCase().contains("potion"))
			return null;
		if (consistency == null) {
			consistency = PotionHelper.getRandomConsistency();
			System.out.println("No consistency found in '" + str + "'. Using '" + consistency.Name + "'");
		}
		if (appearance == null) {
			appearance = PotionHelper.getRandomAppearance();
			System.out.println("No appearance found in '" + str + "'. Using '" + appearance.Name + "'");
		}
		return new PotionEntry(consistency, appearance, !PotionHelper.combinationHasEffect(consistency, appearance));
	}

	public String getEffectString() {
		return getEffectString(false);
	}

	public String getEffectString(boolean splash) {
		if (effect == null)
			return "No effect.";
		if (!isNew && this.effect.action != null)
			return effect.getEffectString(splash);
		return effect.getEffectStringDiscovered(splash);
	}
}
