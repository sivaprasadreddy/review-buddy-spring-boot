package dev.sivalabs.reviewbuddy.users;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public static String getRoleHierarchy() {
        return ROLE_ADMIN.name() + " > " + ROLE_USER.name();
    }
}
