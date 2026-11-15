package com.chetan.dms.auth;

import com.chetan.dms.user.UserAccount;
import com.chetan.dms.user.UserRole;

public record UserSummary(Long id, String email, String displayName, UserRole role) {
    public static UserSummary from(UserAccount user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
    }
}
