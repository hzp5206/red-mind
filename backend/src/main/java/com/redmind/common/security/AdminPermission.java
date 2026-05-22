package com.redmind.common.security;

public final class AdminPermission {

    public static final String DASHBOARD_VIEW = "dashboard:view";
    public static final String TEMPLATE_MANAGE = "template:manage";
    public static final String CATEGORY_MANAGE = "category:manage";
    public static final String SENSITIVE_WORD_MANAGE = "sensitive_word:manage";
    public static final String USER_MANAGE = "user:manage";
    public static final String ROLE_MANAGE = "role:manage";
    public static final String GENERATION_LOG_VIEW = "generation_log:view";
    public static final String OPERATION_LOG_VIEW = "operation_log:view";

    private AdminPermission() {
    }
}
