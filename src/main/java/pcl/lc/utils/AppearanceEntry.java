package pcl.lc.utils;

public class AppearanceEntry {
    public String Prefix;
    public String Name;
    private String ItemPattern; //e.g. "{appearance} {item}" or "{item} the color of {appearance}"
    private String TurnPattern; //e.g. thing "turns {appearance}" or thing "turns the color of {appearance}" (Note that item name is not part of pattern)

    public AppearanceEntry(String name) {
        this(name, null, null, null);
    }

    public AppearanceEntry(String name, String prefix) {
        this(name, prefix, null, null);
    }

    public AppearanceEntry(String name, String prefix, String itemPattern) {
        this(name, prefix, itemPattern, null);
    }

    public AppearanceEntry(String name, String prefix, String itemPattern, String turnPattern) {
        Name = name;
//        System.out.println("Name: " + Name);
        Prefix = (prefix == null ? "" : prefix);
        if (itemPattern == null || itemPattern.isEmpty())
            ItemPattern = "{appearance} {item}";
        else
            ItemPattern = itemPattern;
        if (turnPattern == null || turnPattern.isEmpty())
            TurnPattern = "{appearance}";
        else
            TurnPattern = turnPattern;
    }

    @Override
    public String toString() {
        return Name;
    }

    public String getName() {
        return getName(false);
    }

    public String getName(boolean prefix) {
//        System.out.println("getname: " + Name);
        return (prefix ? Prefix + " " + Name : Name);
    }

    public String appearanceItem(String itemName) {
        return appearanceItem(itemName, true);
    }

    public String appearanceItem(String itemName, boolean use_prefix) {
        if (ItemPattern != null && !ItemPattern.equals("")) {
            return (use_prefix && !Prefix.isEmpty() ? Prefix + " " : "" ) + ItemPattern.replace("{appearance}", Name).replace("{item}", itemName);
        }
        return itemName;
    }

    public String turnsTo() {
//        System.out.println("Turntoname: " + Name);
        return TurnPattern.replace("{appearance}", Name);
    }
}