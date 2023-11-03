package com.valueconnect.security;

import com.valueconnect.domain.User;

public class SecurityUtils {

    public static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserIntegrationId() {
        return currentUser.getIntegrationId();
    }
    
}
