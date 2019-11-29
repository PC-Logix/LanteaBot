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
		AppearanceEntry consistency = PotionHelper.findConsistencyInString(params);
		AppearanceEntry appearance = PotionHelper.findAppearanceInString(params);

		if (consistency == null || appearance == null || !params.toLowerCase().contains("potion"))
			throw new InvalidPotionException();

		this.consistency = consistency;
		this.appearance = appearance;

		this.isNew = !PotionHelper.combinationHasEffect(consistency, appearance);
	}

    public EffectEntry getEffect(String user) {
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

				String replace_appearance = PotionHelper.getAppearance().getName();
				String replace_appearance_prefix = PotionHelper.getAppearance().getName(true);
				String turn_appearance = PotionHelper.getAppearance().turnsTo();
				String replace_consistency = PotionHelper.getConsistency().getName();
				String limit = PotionHelper.getLimit();

				String effectp = DrinkPotion.effects.get(effect)
						.replace("{appearance}", replace_appearance)
						.replace("{appearance_p}", replace_appearance_prefix)
						.replace("{turn_appearance}", turn_appearance)
						.replace("{consistency}", replace_consistency)
						.replace("{transformation}", Helper.getRandomTransformation(true, false, false))
						.replace("{transformation_p}", Helper.getRandomTransformation(true, true, false))
						.replace("{transformation2}", Helper.getRandomTransformation(true, false, false))
						.replace("{transformations}", Helper.getRandomTransformation(true, true, true))
						.replace("{transformations_p}", Helper.getRandomTransformation(true, false, true))
						.replace("{transformations2}", Helper.getRandomTransformation(true, false, true))
						.replace("{limit}", limit);
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
}
