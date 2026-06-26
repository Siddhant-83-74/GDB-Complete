package com.gdb.creditcards.security;

public class SecurityUtils {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TELLER = "TELLER";

    private SecurityUtils() {
    }

    public static UserContext currentUser() {
        UserContext context = UserContextHolder.getContext();
        if (context == null) {
            throw new RuntimeException("ACCESS_DENIED");
        }
        return context;
    }

    public static void checkAdminRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !ROLE_ADMIN.equals(context.getRole())) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkAnyStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null) {
            throw new RuntimeException("ACCESS_DENIED");
        }
        String role = context.getRole();
        if (!ROLE_ADMIN.equals(role) && !ROLE_MANAGER.equals(role) && !ROLE_TELLER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }
}
