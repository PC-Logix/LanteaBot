package pcl.lc.utils;
/*
 * Author AfterLifeLochie
 */
public enum EnumChangeType {
	UNKNOWN("unknown"),

	CREATED_PAGE("created"), EDITED_PAGE("edited"), MOVED_PAGE("moved"), DELETED_PAGE(
			"deleted"),

	EDITED_USER_PAGE("edited user page"), EDITED_TALK_PAGE("edited talk page"), EDITED_USER_TALK_PAGE(
			"edited user talk page"),

	UPLOADED_FILE("uploaded"), CREATED_ACCOUNT("created account"), ADMIN_CHANGED_RIGHTS(
			"changed user rights for"), ADMIN_BLOCKED_ACCOUNT("blocked account");

	public String val;

	EnumChangeType(String val) {
		this.val = val;
	}
}