package com.msik404.karmaapp.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostVisibility;
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

    private static final int USER_AMOUNT = 3_000;
    private static final int MAX_SINGLE_INSERT_SIZE = 100;
    private static final int MAX_KARMA_SCORE = 10_000;
    private static final int MAX_POSTS_PER_USER = 10;
    private static final int MIN_POSTS_PER_USER = 3;
    private static final PostVisibility[] VISIBILITY_OPTIONS = PostVisibility.class.getEnumConstants();

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    User getUserForInserting(@NonNull String username) {
        return User.builder()
                .username(username)
                .email(String.format("karma-app.%s@gmail.com", username))
                .password(bCryptPasswordEncoder.encode(username))
                .role(Role.USER)
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

        var random = new Random();

        final int insertsAmount = Math.ceilDiv(USER_AMOUNT, MAX_SINGLE_INSERT_SIZE);
        for (int insertionIdx = 0; insertionIdx < insertsAmount; insertionIdx++) {
            List<User> usersToSave = new ArrayList<>();
            List<Post> postsToSave = new ArrayList<>();
            final int currLow = insertionIdx * MAX_SINGLE_INSERT_SIZE;
            final int currHigh = Math.min((insertionIdx+1) * MAX_SINGLE_INSERT_SIZE, USER_AMOUNT);
            for (int userId = currLow; userId < currHigh; userId++) {
                var user = getUserForInserting(String.format("username_%d", userId));

                var maxPosts = random.nextInt(MAX_POSTS_PER_USER - MIN_POSTS_PER_USER) + MIN_POSTS_PER_USER;

                for (int postId = 0; postId < maxPosts; postId++) {
                    var post = Post.builder()
                            .headline(String.format("Example headline: %d of user: %d", postId, userId))
                            .text(String.format("Example text: %d of user: %d", postId, userId))
                            .karmaScore(random.nextLong(MAX_KARMA_SCORE) - random.nextLong(MAX_KARMA_SCORE))
                            .visibility(VISIBILITY_OPTIONS[random.nextInt(VISIBILITY_OPTIONS.length)])
                            .user(user)
                            .build();

                    postsToSave.add(post);
                }
                usersToSave.add(user);
            }
            userRepository.saveAll(usersToSave); // Save users in batch
            postRepository.saveAll(postsToSave); // Save posts in batch
        }
    }
}
