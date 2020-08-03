package pcl.lc.irc.entryClasses;

import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SuppressWarnings("FieldCanBeLocal")
public class RPGCharacter {
  private static int fatalHitpoints = -10;

  private String accountName;
  private String userName;
  private String activeTarget;
  private double health;
  private double xp;
  private int level;
  private int strength;
  private int defense;
  private int accuracy;
  private int dodge;
  private int gainStrength;
  private int gainDefense;
  private int gainAccuracy;
  private int gainDodge;
  private int numAttacked;
  private int numAttacks;
  private int deaths;
  private int revives;

  public RPGCharacter(String accountName, String userName, String activeTarget) throws Exception {
    this.accountName = accountName;
    this.userName = userName;
    this.activeTarget = activeTarget;
    //Default stats
    this.health = 20;
    this.xp = 0;
    this.level = 1;
    this.strength = 1;
    this.defense = 1;
    this.accuracy = 1;
    this.dodge = 1;
    this.gainStrength = 0;
    this.gainDefense = 0;
    this.gainAccuracy = 0;
    this.gainDodge = 0;
    this.numAttacked = 0;
    this.numAttacks = 0;
    this.deaths = 0;
    this.revives = 0;
    PreparedStatement statement = Database.getPreparedStatement("getRPGCharacter");
    statement.setString(1, accountName);
    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      if (this.userName == null || this.userName == "")
        this.userName = resultSet.getString(2);
      this.health = resultSet.getDouble(3);
      this.xp = resultSet.getDouble(4);
      this.level = resultSet.getInt(5);
      this.strength = resultSet.getInt(6);
      this.defense = resultSet.getInt(7);
      this.accuracy = resultSet.getInt(8);
      this.dodge = resultSet.getInt(9);
      this.gainStrength = resultSet.getInt(10);
      this.gainDefense = resultSet.getInt(11);
      this.gainAccuracy = resultSet.getInt(12);
      this.gainDodge = resultSet.getInt(13);
      this.numAttacked = resultSet.getInt(14);
      this.numAttacks = resultSet.getInt(15);
      this.deaths = resultSet.getInt(16);
      this.revives = resultSet.getInt(17);
    } else {
      PreparedStatement newCharacter = Database.getPreparedStatement("newRPGCharacter");
      newCharacter.setString(1, this.accountName);
      newCharacter.setString(2, this.userName);
      newCharacter.setDouble(3, this.health);
      newCharacter.setDouble(4, this.xp);
      newCharacter.setInt(5, this.level);
      newCharacter.setInt(6, this.strength);
      newCharacter.setInt(7, this.defense);
      newCharacter.setInt(8, this.accuracy);
      newCharacter.setInt(9, this.dodge);
      newCharacter.setInt(10, this.gainStrength);
      newCharacter.setInt(11, this.gainDefense);
      newCharacter.setInt(12, this.gainAccuracy);
      newCharacter.setInt(13, this.gainDodge);
      newCharacter.setInt(14, this.numAttacked);
      newCharacter.setInt(15, this.numAttacks);
      newCharacter.setInt(16, this.deaths);
      newCharacter.setInt(17, this.revives);
      newCharacter.executeUpdate();
    }
  }

  private void save() {
    try {
      PreparedStatement updateCharacter = Database.getPreparedStatement("updateRPGCharacter");
      updateCharacter.setString(1, this.userName);
      updateCharacter.setDouble(2, this.health);
      updateCharacter.setDouble(3, this.xp);
      updateCharacter.setInt(4, this.level);
      updateCharacter.setInt(5, this.strength);
      updateCharacter.setInt(6, this.defense);
      updateCharacter.setInt(7, this.accuracy);
      updateCharacter.setInt(8, this.dodge);
      updateCharacter.setInt(9, this.gainStrength);
      updateCharacter.setInt(10, this.gainDefense);
      updateCharacter.setInt(11, this.gainAccuracy);
      updateCharacter.setInt(12, this.gainDodge);
      updateCharacter.setInt(13, this.numAttacked);
      updateCharacter.setInt(14, this.numAttacks);
      updateCharacter.setInt(15, this.deaths);
      updateCharacter.setInt(16, this.revives);
      updateCharacter.setString(17, this.accountName);
      updateCharacter.executeUpdate();
      System.out.println("Character " + this.getUserName() + " (" + this.getAccountName() + ") saved!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //<editor-fold desc="Standard Getters">
  public String getAccountName() {
    return accountName;
  }

  public String getUserName() {
    return userName;
  }

  public double getHealth() {
    return health;
  }

  public String getHealthString() {
    return Double.toString(this.health).replace(".0", "");
  }

  public double getXp() {
    return xp;
  }

  public String getXpString() {
    return Double.toString(this.xp).replace(".0", "");
  }

  public int getLevel() {
    return level;
  }

  public int getStrength() {
    return strength;
  }

  public int getDefense() {
    return defense;
  }

  public int getAccuracy() {
    return accuracy;
  }

  public int getDodge() {
    return dodge;
  }

  public int getNumAttacked() {
    return numAttacked;
  }

  public int getNumAttacks() {
    return numAttacks;
  }

  public int getDeaths() {
    return deaths;
  }

  public int getRevives() {
    return revives;
  }
  //</editor-fold>

  @Override
  public String toString() {
    return userName + " [" + this.getHealthString() + "/" + this.getMaxHealthString() + "], Level " + level + ", " + this.getXpString() + " xp out of " + this.nextLevelThreshhold() + " xp for next level.";
  }

  public double getMaxHealth() {
    return 20 + Math.floor(this.level * 0.2);
  }

  public String getMaxHealthString() {
    return Double.toString(this.getMaxHealth()).replace(".0", "");
  }

  /**
   * Performs an attack against target using weapon, target uses shield to defend.
   * @param target RPGCharacter to attack.
   * @param weapon Item to use for attack by attacker.
   * @param shield Item to use for defense by target.
   * @throws Exception Throws exception if attack cannot be performed with reason as the message.
   */
  public void attack(RPGCharacter target, @Nullable Item weapon, @Nullable Item shield) throws Exception{
    if (target.isUnconcious())
      throw new Exception("Can't attack unconscious target!");
    double attack = this.strength;
    double defense = target.getDefense();
    String weaponDust = "";
    if (weapon != null) {
      attack += weapon.getUsesLeft();
      weaponDust = weapon.damage(2,false, true, true);
    }
    String shieldDust = "";
    if (shield != null) {
      defense += shield.getUsesLeft();
      shieldDust = shield.damage(1,false, true, true);
    }

    double damage = attack - defense;
    if (damage > 0) {
      Helper.sendMessage(this.activeTarget, target.getUserName() + " is struck for " + damage + " damage and is on " + target.takeDamage(damage) + " health.");
      if (target.getHealth() <= fatalHitpoints)
        Helper.sendMessage(this.activeTarget, target.getUserName() + " died.");
      else if (target.getHealth() <= 0)
        Helper.sendMessage(this.activeTarget, target.getUserName() + " is down!");
    }
    else if (damage == 0)
      Helper.sendMessage(this.activeTarget, this.getUserName() + " tries to strike " + target.getUserName() + ", but " + (weapon == null ? "their fists" : weapon.getName(true)) + " glances off their " + (shield == null ? "armor" : "defensive " + shield.getNameWithoutPrefix()) + ".");
    else
      Helper.sendMessage(this.activeTarget, this.getUserName() + " tries to strike " + target.getUserName() + ", but " + (weapon == null ? "their fists" : weapon.getName(true)) + " miss.");
    if (weaponDust != "" || shieldDust != "")
    {
      String dust = weaponDust + ((weaponDust != "" && shieldDust != "") ? " " : "") + shieldDust;
      Helper.sendMessage(this.activeTarget, dust);
    }
  }

  /**
   * Apply damage to character.
   * @param damage Damage to apply.
   * @return Actual damage applied (down to fatal hitpoints).
   */
  public double takeDamage(double damage) {
    double maxUntilFatal = this.health - fatalHitpoints;
    this.health -= damage;
    this.save();
    return Math.max(maxUntilFatal, damage);
  }

  public String takeDamageString(double damage) {
    return Double.toString(this.takeDamage(damage)).replace(".0", "");
  }

  /**
   *
   * @param health Hitpoints to heal.
   * @return Hitpoints healed (up to max health).
   */
  public double heal(double health) {
    double maxHealed = this.getMaxHealth() - this.health;
    this.health = Math.min(this.getMaxHealth(), this.health + health);
    this.save();
    return Math.min(maxHealed, health);
  }

  public String healString(double health) {
    return Double.toString(this.heal(health)).replace(".0", "");
  }

  public int getMinutesUntilDeath() {
    if (this.health > 0)
      return -1;
    return (int)(fatalHitpoints - this.health) * -1;
  }

  /**
   * Overload for getStatus(boolean).
   * @return Current character status non-capitalized.
   */
  public String getStatus() {
    return getStatus(false);
  }

  /**
   * Returns current general health status.
   * @param capitalize Whether the status returned should have the first letter capitalized or not.
   * @return Current character status.
   */
  public String getStatus(boolean capitalize) {
    String status = "unknown";
    double healthPercentage = this.getHealth() / this.getMaxHealth();
    if (healthPercentage < 0)
      status = "down";
    else if (healthPercentage < .25)
      status = "mortally wounded";
    else if (healthPercentage < .5)
      status = "wounded";
    else if (healthPercentage < .75)
      status = "hurt";
    else if (healthPercentage < 1)
      status = "uncomfortable";
    else if (healthPercentage == 1)
      status = "healthy";
    if (capitalize)
      status = status.substring(0, 1).toUpperCase() + status.substring(1);
    return status;
  }

  public boolean isUnconcious() {
    return (this.health <= 0);
  }

  public double nextLevelThreshhold() {
    return (this.level * 2) * 1.25;
  }

  public double experienceToNextLevel() {
    return nextLevelThreshhold() - this.xp;
  }

  /**
   * Adds experience to character.
   * Call levelUp(false) after adding any amount of experience to character.
   * @param experience Experience to add.
   */
  public void gainExperience(double experience) {
    System.out.println(this.userName + " gains " + experience + " going from " + this.xp + " to " + (this.xp + experience) + "!");
    this.xp += experience;
    this.save();
  }

  public boolean shouldLevelUp() {
    return (this.nextLevelThreshhold() < this.xp && this.gainStrength == 0 && this.gainDefense == 0 && this.gainAccuracy == 0 && this.gainDodge == 0);
  }

  /**
   * Levels up character if they have sufficient xp.
   * Overload for levelUp(boolean)
   * @return An array containing the gained strength, defense and accuracy respectively.
   */
  public int[] levelUpRaw() {
    return levelUpRaw(false);
  }

  /**
   * Levels up character if they have sufficient xp (or if override is true).
   * @param override Whether to ignore current xp and force level up.
   *                 Note that xp level doesn't change, so reaching the next level would still require gaining the xp for this level.
   *                 It would be preferable to call character.gainExperience(character.experienceToNextLevel()) followed by character.levelUp()
   * @return An array containing the gained strength, defense, accuracy and dodge respectively.
   */
  public int[] levelUpRaw(boolean override) {
    if (!override && !this.shouldLevelUp())
      return null;
    this.level++;
    this.health = this.getMaxHealth();
    int strengthBonus = Helper.rollDice("1d4").getSum();
    int defenseBonus = Helper.rollDice("1d4").getSum();
    int accuracyBonus = Helper.rollDice("1d4").getSum();
    int dodgeBonus = Helper.rollDice("1d4").getSum();
    this.gainStrength = strengthBonus;
    this.gainDefense = defenseBonus;
    this.gainAccuracy = accuracyBonus;
    this.gainDodge = dodgeBonus;
    this.save();
    return new int[] { strengthBonus, defenseBonus, accuracyBonus, dodgeBonus };
  }

  public boolean levelUp() {
    return levelUp(false);
  }

  public boolean levelUp(boolean override) {
    int[] gains = levelUpRaw(override);
    if (gains != null) {
      Helper.sendMessage(this.activeTarget, this.userName + " leveled up! Gain " + gains[0] + " strength, " + gains[1] + " defense, " + gains[2] + " accuracy or " + gains[3] + " dodge by entering the appropriate sub command now!");
      return true;
    }
    return false;
  }

  private void resetGains() {
    this.gainStrength = 0;
    this.gainDefense = 0;
    this.gainAccuracy = 0;
    this.gainDodge = 0;
  }

  public int applyStrength() {
    if (this.gainStrength > 0) {
      int gain = this.gainStrength;
      this.strength += gain;
      resetGains();
      this.save();
      return gain;
    }
    return 0;
  }

  public int applyDefense() {
    if (this.gainDefense > 0) {
      int gain = this.gainDefense;
      this.defense += gain;
      resetGains();
      this.save();
      return gain;
    }
    return 0;
  }

  public int applyAccuracy() {
    if (this.gainAccuracy > 0) {
      int gain = this.gainAccuracy;
      this.accuracy += gain;
      resetGains();
      this.save();
      return gain;
    }
    return 0;
  }

  public int applyDodge() {
    if (this.gainDodge > 0) {
      int gain = this.gainDodge;
      this.dodge += gain;
      resetGains();
      this.save();
      return gain;
    }
    return 0;
  }
}
