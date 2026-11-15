package com.chetan.dms.config;

import com.chetan.dms.user.UserAccount;
import com.chetan.dms.user.UserRepository;
import com.chetan.dms.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String displayName;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          @Value("${dms.security.bootstrap-admin.email:}") String email,
                          @Value("${dms.security.bootstrap-admin.password:}") String password,
                          @Value("${dms.security.bootstrap-admin.display-name:Administrator}") String displayName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        this.password = password == null ? "" : password;
        this.displayName = displayName;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() && password.isBlank()) {
            return;
        }
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Set both bootstrap admin email and a password of at least 12 characters.");
        }
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(new UserAccount(email, displayName,
                    passwordEncoder.encode(password), UserRole.ADMIN));
        }
    }
}
