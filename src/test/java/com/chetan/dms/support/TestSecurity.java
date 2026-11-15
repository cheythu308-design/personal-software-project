package com.chetan.dms.support;

import com.chetan.dms.user.UserAccount;
import com.chetan.dms.user.UserRepository;
import com.chetan.dms.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

public final class TestSecurity {

    public static final String USER_EMAIL = "milestone3-user@example.test";

    private TestSecurity() {
    }

    public static void ensureUser(UserRepository users, PasswordEncoder encoder) {
        users.findByEmail(USER_EMAIL).orElseGet(() -> users.save(new UserAccount(
                USER_EMAIL, "Milestone Three Test User", encoder.encode("test-password-123"),
                UserRole.USER)));
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor user() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(USER_EMAIL))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor admin(String email) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(email))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
