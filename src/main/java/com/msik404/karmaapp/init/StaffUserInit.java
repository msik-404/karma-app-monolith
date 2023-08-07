package com.msik404.karmaapp.init;

import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffUserInit implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String MOD_USERNAME = "MOD";

    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    User staffBuilder(String username, Role role) {
        return User.builder()
                .username(username)
                .email(String.format("karma-app.%s@gmail.com", username))
                .password(bCryptPasswordEncoder.encode(username))
                .role(role)
                .firstName(username)
                .lastName(username)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    @Override
    public void run(String... args) throws Exception {

        if (repository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            repository.save(staffBuilder(ADMIN_USERNAME, Role.ADMIN));
        }
        if (repository.findByUsername(MOD_USERNAME).isEmpty()) {
            repository.save(staffBuilder(MOD_USERNAME, Role.MOD));
        }
    }
}