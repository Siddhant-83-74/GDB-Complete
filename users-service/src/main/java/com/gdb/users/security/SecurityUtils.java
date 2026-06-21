package com.gdb.users.security;

import com.gdb.users.constants.UserConstants;

public class SecurityUtils {

    public static void checkAdminRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null || !UserConstants.ROLE_ADMIN.equals(context.getRole())) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    // TODO: MOD8-BUG-01: Mismatched operator denies access.
    // Trainee task: Notice that Admins and Tellers are blocked from accessing user profiles.
    // Examine why checkAdminOrTellerRole is throwing ACCESS_DENIED for all valid users.
    // Hint: Look closely at the logical check operator.
    public static void checkAdminOrTellerRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null)
            throw new RuntimeException("ACCESS_DENIED");

        String role = context.getRole();
        // MOD8-BUG-01 FIXED: deny only if the role is NEITHER admin NOR teller (&& not ||)
        if (!UserConstants.ROLE_ADMIN.equals(role) && !UserConstants.ROLE_TELLER.equals(role)) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }

    public static void checkAnyAuthorizedRole() {
        UserContext context = UserContextHolder.getContext();
        if (context == null) {
            throw new RuntimeException("ACCESS_DENIED");
        }
    }
}
