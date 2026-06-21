package com.gdb.transactions.security;

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

    public static void checkAdminOrManagerRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        if (!ROLE_ADMIN.equals(role) && !ROLE_MANAGER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkStaffRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        // Teller and Admin can perform transactions
        if (!ROLE_ADMIN.equals(role) && !ROLE_TELLER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkManagerRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        // Only Manager, Admin and Teller can view transaction logs
        if (!ROLE_ADMIN.equals(role) && !ROLE_MANAGER.equals(role) && !ROLE_TELLER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }
}
