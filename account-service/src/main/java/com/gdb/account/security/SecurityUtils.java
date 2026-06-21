package com.gdb.account.security;

public class SecurityUtils {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TELLER = "TELLER";

    public static void checkAdminRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !ROLE_ADMIN.equals(context.getRole())) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        // Admin and Teller are staff for banking operations
        if (!ROLE_ADMIN.equals(role) && !ROLE_TELLER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkManagerRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        // Admin and Manager can view management level data
        if (!ROLE_ADMIN.equals(role) && !ROLE_MANAGER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkAnyStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        // Any bank staff (Admin, Manager, Teller)
        if (!ROLE_ADMIN.equals(role) && !ROLE_MANAGER.equals(role) && !ROLE_TELLER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }
}
