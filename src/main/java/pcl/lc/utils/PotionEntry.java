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

    public PotionEntry() {}

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

			PotionHelper.tryResetPotionList();

			if (PotionHelper.combinationHasEffect(consistency, appearance)) {
				this.isNew = false;
				this.effect = PotionHelper.getCombinationEffect(consistency, appearance);
				System.out.println("Effect recorded for " + PotionHelper.getConsistencyIndexByName(consistency.getName()) + "," + PotionHelper.getAppearanceIndexByName(appearance.getName()) + ": " + this.effect);
				return this.effect;
			} else {
				int min = 0;
				int max = DrinkPotion.effects.size() - 1;
				if (consistency.getName().toLowerCase().equals("mutable")) {
					min = 1;
					max = 6;
				}
				int effect = Helper.getRandomInt(min, max);
				System.out.println("No effect recorded for " + PotionHelper.getConsistencyIndexByName(consistency.getName()) + "," + PotionHelper.getAppearanceIndexByName(appearance.getName()) + ", Assign " + effect);

				String effectp = null;

				if (splash) {
                    try {
                        effectp = DrinkPotion.effects.get(effect)[1];
                    } catch (Exception ignored) {}
                }

				if (effectp == null)
				    effectp = DrinkPotion.effects.get(effect)[0];

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
				try {
					Pattern pattern = Pattern.compile("\\{r:(\\d\\d?\\d?)-(\\d\\d?\\d?):(.*?)}");
					Matcher matcher = pattern.matcher(effectp);
					while (matcher.find()) {
						int num_min = Integer.parseInt(matcher.group(1));
						int num_max = Integer.parseInt(matcher.group(2));
						int value = Helper.getRandomInt(num_min, num_max);
						effectp = effectp.replace(matcher.group(0), value + (!matcher.group(3).equals("") ? " " + Noun.pluralOf(matcher.group(3), value) : ""));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				this.isNew = true;
				this.effect = new EffectEntry(effectp, user);
				PotionHelper.setCombinationEffect(consistency, appearance, this.effect);
				return this.effect;
			}
		}
	}

	public static PotionEntry setFromString(String str) {
    	if (str == null)
    		return null;
		AppearanceEntry consistency = PotionHelper.findConsistencyInString(str);
		AppearanceEntry appearance = PotionHelper.findAppearanceInString(str);

		if (consistency == null || appearance == null || !str.toLowerCase().contains("potion"))
			return null;
		return new PotionEntry(consistency, appearance, !PotionHelper.combinationHasEffect(consistency, appearance));
	}
}
