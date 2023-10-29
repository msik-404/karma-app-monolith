package com.msik404.karmaapp.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.repository.PostRepository;
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
public class UserAndPostInit implements CommandLineRunner {

    private static final int USER_AMOUNT = 300;
    private static final int MAX_SINGLE_INSERT_SIZE = 100;
    private static final int MAX_KARMA_SCORE = 100;
    private static final int MAX_POSTS_PER_USER = 10;
    private static final int MIN_POSTS_PER_USER = 3;
    private static final Visibility[] VISIBILITY_OPTIONS = Visibility.class.getEnumConstants();

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    User getUserForInserting(@NonNull String username) {

        return new User(
                null,
                username,
                username,
                username,
                String.format("karma-app.%s@gmail.com", username),
                bCryptPasswordEncoder.encode(username),
                Role.USER,
                null,
                null
        );
    }

    @Override
    public void run(String... args) {

        var random = new Random();

        int insertsAmount = Math.ceilDiv(USER_AMOUNT, MAX_SINGLE_INSERT_SIZE);
        for (int insertionIdx = 0; insertionIdx < insertsAmount; insertionIdx++) {
            List<User> usersToSave = new ArrayList<>();
            List<Post> postsToSave = new ArrayList<>();
            int currLow = insertionIdx * MAX_SINGLE_INSERT_SIZE;
            int currHigh = Math.min((insertionIdx + 1) * MAX_SINGLE_INSERT_SIZE, USER_AMOUNT);
            for (int userId = currLow; userId < currHigh; userId++) {
                var user = getUserForInserting(String.format("username_%d", userId));

                var maxPosts = random.nextInt(MAX_POSTS_PER_USER - MIN_POSTS_PER_USER) + MIN_POSTS_PER_USER;

                for (int postId = 0; postId < maxPosts; postId++) {

                    var post = new Post(
                            String.format("Example headline: %d of user: %d", postId, userId),
                            String.format("Example text: %d of user: %d", postId, userId),
                            user,
                            null
                    );
                    post.setKarmaScore(random.nextLong(MAX_KARMA_SCORE) - random.nextLong(MAX_KARMA_SCORE));
                    post.setVisibility(VISIBILITY_OPTIONS[random.nextInt(VISIBILITY_OPTIONS.length)]);

                    postsToSave.add(post);
                }
                usersToSave.add(user);
            }
            userRepository.saveAll(usersToSave); // Save users in batch
            postRepository.saveAll(postsToSave); // Save posts in batch
        }

        System.out.println("DONE");
    }
}
