package uz.app.projectv1.rbac;

public final class Permissions {

    public static final String USER_READ =   "user:read";
    public static final String USER_CREATE = "user:create";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";

    public static final String ROLE_READ   = "role:read";
    public static final String ROLE_CREATE = "role:create";
    public static final String ROLE_UPDATE = "role:update";
    public static final String ROLE_DELETE = "role:delete";
    public static final String ROLE_ASSIGN = "role:assign";

    public static final String CAN_READ_USER   = "hasAuthority('" + USER_READ + "')";
    public static final String CAN_CREATE_USER = "hasAuthority('" + USER_CREATE + "')";
    public static final String CAN_UPDATE_USER = "hasAuthority('" + USER_UPDATE + "')";
    public static final String CAN_DELETE_USER = "hasAuthority('" + USER_DELETE + "')";

    private Permissions() {}
}
