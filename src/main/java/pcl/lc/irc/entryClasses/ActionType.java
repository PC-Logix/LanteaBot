package pcl.lc.irc.entryClasses;

public class ActionType {
    public String actionNameIs;
    public String actionNameWas;
    public String actionNameWill;
    public String actionNamePast;

    public ActionType(String actionNameIs, String actionNameWas, String actionNameWill, String actionNamePast) {
        this.actionNameIs = actionNameIs;
        this.actionNameWas = actionNameWas;
        this.actionNameWill = actionNameWill;
        this.actionNamePast = actionNamePast;
    }
}
