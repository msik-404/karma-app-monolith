package com.msik404.karmaapp.init;

import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffUserInit implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String MOD_USERNAME = "MOD";

    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    User getUserForInserting(@NonNull String username, @NonNull Role role) {

        return new User(
                username,
                String.format("karma-app.%s@gmail.com", username),
                bCryptPasswordEncoder.encode(username),
                role,
                username,
                username
        );
    }

    @Override
    public void run(String... args) {

        if (repository.findByUsername(ADMIN_USERNAME).isEmpty()) {
            repository.save(getUserForInserting(ADMIN_USERNAME, Role.ADMIN));
        }
        if (repository.findByUsername(MOD_USERNAME).isEmpty()) {
            repository.save(getUserForInserting(MOD_USERNAME, Role.MOD));
        }
    }
}
