package com.msik404.karmaapp.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostRepository;
import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private static final int USER_AMOUNT = 100;
    private static final int MAX_KARMA_SCORE = 10000;
    private static final int MAX_POSTS_PER_USER = 10;
    private static final int MIN_POSTS_PER_USER = 3;
    private static final PostVisibility[] VISIBILITY_OPTIONS = PostVisibility.class.getEnumConstants();

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    User userBuilder(String username) {
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

        for (int t = 0; t < 10; t++) {
            List<User> usersToSave = new ArrayList<>(USER_AMOUNT);
            List<Post> postsToSave = new ArrayList<>();

            for (int i = 0; i < USER_AMOUNT; i++) {
                var user = userBuilder(String.format("username_%d-%d", t, i));

                var maxPosts = random.nextInt(MAX_POSTS_PER_USER - MIN_POSTS_PER_USER) + MIN_POSTS_PER_USER;

                for (int j = MIN_POSTS_PER_USER; j < maxPosts; j++) {
                    var post = Post.builder()
                            .text(String.format("Example text: (%d, %d, %d)", t, i, j))
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
