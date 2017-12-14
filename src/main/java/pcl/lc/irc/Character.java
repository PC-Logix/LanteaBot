package pcl.lc.irc;

import com.sun.istack.internal.Nullable;
import pcl.lc.utils.Database;
import pcl.lc.utils.Helper;
import pcl.lc.utils.Item;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

//Database.addStatement("CREATE TABLE IF NOT EXISTS RPGUsers(
// account STRING UNIQUE PRIMARY KEY,
// userName STRING,
// health,
// xp,
// level,
// strength,
// defense,
// accuracy,
// dodge,
// numAttacked,
// numAttacks,
// deaths,
// revives)");
@SuppressWarnings("FieldCanBeLocal")
public class Character {
  private static int fatalHitpoints = -10;

  private int columnIndexAccountName = 1;
  private int columnIndexUserName = 2;
  private int columnIndexHealth = 3;
  private int columnIndexXp = 4;
  private int columnIndexLevel = 5;
  private int columnIndexStrength = 6;
  private int columnIndexDefense = 7;
  private int columnIndexAccuracy = 8;
  private int columnIndexDodge = 9;
  private int columnIndexNumAttacked = 10;
  private int columnIndexNumAttacks = 11;
  private int columnIndexDeaths = 12;
  private int columnIndexRevives = 13;

  private String accountName;
  private String userName;
  private String activeTarget;
  private double health;
  private int xp;
  private int level;
  private int strength;
  private int defense;
  private int accuracy;
  private int dodge;
  private int numAttacked;
  private int numAttacks;
  private int deaths;
  private int revives;

  public Character(String accountName, String userName, String activeTarget) throws Exception {
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
    this.numAttacked = 0;
    this.numAttacks = 0;
    this.deaths = 0;
    this.revives = 0;
    PreparedStatement statement = Database.getPreparedStatement("getCharacter");
    statement.setString(1, accountName);
    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      if (this.userName == null || this.userName == "")
        this.userName = resultSet.getString(columnIndexUserName);
      this.health = resultSet.getDouble(columnIndexHealth);
      this.xp = resultSet.getInt(columnIndexXp);
      this.level = resultSet.getInt(columnIndexLevel);
      this.strength = resultSet.getInt(columnIndexStrength);
      this.defense = resultSet.getInt(columnIndexDefense);
      this.accuracy = resultSet.getInt(columnIndexAccuracy);
      this.dodge = resultSet.getInt(columnIndexDodge);
      this.numAttacked = resultSet.getInt(columnIndexNumAttacked);
      this.numAttacks = resultSet.getInt(columnIndexNumAttacks);
      this.deaths = resultSet.getInt(columnIndexDeaths);
      this.revives = resultSet.getInt(columnIndexRevives);
    } else {
      PreparedStatement newCharacter = Database.getPreparedStatement("newCharacter");
      newCharacter.setString(columnIndexAccountName, this.accountName);
      newCharacter.setDouble(columnIndexHealth, this.health);
      newCharacter.setInt(columnIndexXp, this.xp);
      newCharacter.setInt(columnIndexLevel, this.level);
      newCharacter.setInt(columnIndexStrength, this.strength);
      newCharacter.setInt(columnIndexDefense, this.defense);
      newCharacter.setInt(columnIndexAccuracy, this.accuracy);
      newCharacter.setInt(columnIndexDodge, this.dodge);
      newCharacter.setInt(columnIndexNumAttacked, this.numAttacked);
      newCharacter.setInt(columnIndexNumAttacks, this.numAttacks);
      newCharacter.setInt(columnIndexDeaths, this.deaths);
      newCharacter.setInt(columnIndexRevives, this.revives);
      newCharacter.executeQuery();
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

  public int getXp() {
    return xp;
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
    return userName + ", Level " + level + ", " + this.experienceToNextLevel() + " xp to next level.";
  }

  public double getMaxHealth() {
    return 20 + Math.floor(this.level * 0.2);
  }

  /**
   * Performs an attack against target using weapon, target uses shield to defend.
   * @param target Character to attack.
   * @param weapon Item to use for attack by attacker.
   * @param shield Item to use for defense by target.
   * @throws Exception Throws exception if attack cannot be performed with reason as the message.
   */
  public void attack(Character target, @Nullable Item weapon, @Nullable Item shield) throws Exception{
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
    return Math.max(maxUntilFatal, damage);
  }

  /**
   *
   * @param health Hitpoints to heal.
   * @return Hitpoints healed (up to max health).
   */
  public double heal(double health) {
    double maxHealed = this.getMaxHealth() - this.health;
    this.health = Math.min(this.getMaxHealth(), this.health + health);
    return Math.min(maxHealed, health);
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
      status =  "down";
    else if (healthPercentage < .25)
      status =  "mortally wounded";
    else if (healthPercentage < .5)
      status =  "wounded";
    else if (healthPercentage < .75)
      status =  "hurt";
    else if (healthPercentage < 1)
      status =  "uncomfortable";
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
  public void gainExperience(int experience) {
    this.xp += experience;
  }


  /**
   * Levels up character if they have sufficient xp.
   * Overload for levelUp(boolean)
   * @return An array containing the gained strength, defense and accuracy respectively.
   */
  public int[] levelUp() {
    return levelUp(false);
  }

  /**
   * Levels up character if they have sufficient xp (or if override is true).
   * @param override Whether to ignore current xp and force level up.
   *                 Note that xp level doesn't change, so reaching the next level would still require gaining the xp for this level.
   *                 It would be preferable to call character.gainExperience(character.experienceToNextLevel()) followed by character.levelUp()
   * @return An array containing the gained strength, defense and accuracy respectively.
   */
  public int[] levelUp(boolean override) {
    if (!override && this.xp < this.nextLevelThreshhold())
      return null;
    this.level++;
    this.health = this.getMaxHealth();
    int strengthBonus = Helper.rollDice("1d4").getSum();
    int defenseBonus = Helper.rollDice("1d4").getSum();
    int accuracyBonus = Helper.rollDice("1d4").getSum();
    this.strength += strengthBonus;
    this.defense += defenseBonus;
    this.accuracy += accuracyBonus;
    return new int[] { strengthBonus, defenseBonus, accuracyBonus };
  }
}
