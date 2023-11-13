package com.msik404.karmaappmonolith.init;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.post.Visibility;
import com.msik404.karmaappmonolith.post.repository.PostRepository;
import com.msik404.karmaappmonolith.user.Role;
import com.msik404.karmaappmonolith.user.User;
import com.msik404.karmaappmonolith.user.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(DataInit.class);

    private static final int USER_AMOUNT = 300;
    private static final int MAX_SINGLE_INSERT_SIZE = 100;
    private static final int MAX_POSTS_PER_USER = 10;
    private static final int MIN_POSTS_PER_USER = 3;
    private static final Visibility[] VISIBILITY_OPTIONS = Visibility.class.getEnumConstants();

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String MOD_USERNAME = "MOD";

    @NonNull
    private static String getUsername(long id) {
        return String.format("username_%d", id);
    }

    @NonNull
    private static String getEmail(@NonNull String username) {
        return String.format("%s@mail.com", username);
    }

    @NonNull
    private User getUserForInserting(@NonNull String username, @NonNull Role role) {

        return new User(
                null,
                username,
                username,
                username,
                getEmail(username),
                bCryptPasswordEncoder.encode(username),
                role,
                null,
                null
        );
    }

    @Value("${initialize.data}")
    private String shouldInitData;

    @Override
    public void run(String... args) {

        if (!shouldInitData.equalsIgnoreCase("true")) {
            return;
        }

        Optional<User> optionalAdmin = userRepository.findByUsername(ADMIN_USERNAME);
        if (optionalAdmin.isPresent()) {
            logger.info("Data initialization has been already performed.");
            return;
        }

        userRepository.save(getUserForInserting(ADMIN_USERNAME, Role.ADMIN));
        userRepository.save(getUserForInserting(MOD_USERNAME, Role.MOD));

        var random = new Random();

        int insertsAmount = Math.ceilDiv(USER_AMOUNT, MAX_SINGLE_INSERT_SIZE);
        for (int insertionIdx = 0; insertionIdx < insertsAmount; insertionIdx++) {
            List<User> usersToSave = new ArrayList<>();
            List<Post> postsToSave = new ArrayList<>();
            int currLow = insertionIdx * MAX_SINGLE_INSERT_SIZE;
            int currHigh = Math.min((insertionIdx + 1) * MAX_SINGLE_INSERT_SIZE, USER_AMOUNT);
            for (int userId = currLow; userId < currHigh; userId++) {
                var user = getUserForInserting(getUsername(userId), Role.USER);

                var maxPosts = random.nextInt(MAX_POSTS_PER_USER - MIN_POSTS_PER_USER) + MIN_POSTS_PER_USER;

                for (int postId = 0; postId < maxPosts; postId++) {

                    var post = new Post(
                            String.format("Example headline: %d of user: %d", postId, userId),
                            String.format("Example text: %d of user: %d", postId, userId),
                            user,
                            null
                    );
                    post.setVisibility(VISIBILITY_OPTIONS[random.nextInt(VISIBILITY_OPTIONS.length)]);

                    postsToSave.add(post);
                }
                usersToSave.add(user);
            }
            userRepository.saveAll(usersToSave); // Save users in batch
            postRepository.saveAll(postsToSave); // Save posts in batch
        }
        logger.info("Data initialization is done.");
    }

}
