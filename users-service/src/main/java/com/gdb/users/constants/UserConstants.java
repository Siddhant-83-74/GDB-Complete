package com.gdb.users.constants;

public class UserConstants {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_TELLER = "TELLER";

    public static final int BCRYPT_SALT_ROUNDS = 12;

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_ACTIVATE = "ACTIVATE";
    public static final String ACTION_INACTIVATE = "INACTIVATE";

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_STATE = "INVALID_STATE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
