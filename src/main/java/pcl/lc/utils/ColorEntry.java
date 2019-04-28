package pcl.lc.utils;

public class ColorEntry {
    public String Prefix;
    public String Name;
    private String ItemPattern; //e.g. "{color} {item}" or "{item} the color of {color}"
    private String TurnPattern; //e.g. "thing turns {color}" or "thing turns the color of {color}"

    public ColorEntry(String name) {
        this(name, null, null, null);
    }

    public ColorEntry(String name, String prefix) {
        this(name, prefix, null, null);
    }

    public ColorEntry(String name, String prefix, String itemPattern) {
        this(name, prefix, itemPattern, null);
    }

    public ColorEntry(String name, String prefix, String itemPattern, String turnPattern) {
        Name = name;
        System.out.println("Name: " + Name);
        Prefix = (prefix == null ? "" : prefix);
        if (itemPattern == null || itemPattern.isEmpty())
            ItemPattern = Prefix + (Prefix.equals("") ? "" : " ") + "{color} {item}";
        else
            ItemPattern = itemPattern;
        if (turnPattern == null || turnPattern.isEmpty())
            TurnPattern = "{color}";
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
        System.out.println("getname: " + Name);
        return (prefix ? Prefix + " " + Name : Name);
    }

    public String colorItem(String itemName) {
        if (ItemPattern != null && !ItemPattern.equals("")) {
            return ItemPattern.replace("{color}", Name).replace("{item}", itemName);
        }
        return itemName;
    }

    public String turnsTo() {
        System.out.println("Turntoname: " + Name);
        return TurnPattern.replace("{color}", Name);
    }
}