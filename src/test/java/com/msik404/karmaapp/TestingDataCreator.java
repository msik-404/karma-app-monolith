package com.msik404.karmaapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public class TestingDataCreator {

    private final List<User> usersForTesting;
    private final List<Post> postsForTesting;

    /**
     * This comparator is made so to mimic the desired sort order, that is ascending id and descending score.
     */
    static class PostComparator implements Comparator<Post> {

        @Override
        public int compare(Post postOne, Post postTwo) {
            if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
                return - postOne.getId().compareTo(postTwo.getId());
            }
            return postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
        }

    }

    public TestingDataCreator() {

        usersForTesting = new ArrayList<>();
        postsForTesting = new ArrayList<>();

        this.createFirstUserData();
        this.createSecondUserData();
        this.createThirdUserData();
        this.createFirstModData();
        this.createFirstAdminData();
    }

    public List<Post> getTopPosts(@NonNull List<Post> posts, @NonNull Set<PostVisibility> visibilities) {

        return posts.stream()
                .filter(post -> visibilities.contains(post.getVisibility()))
                .sorted(new PostComparator().reversed())
                .collect(Collectors.toList());
    }

    public List<Post> getTopUsersPosts(@NonNull List<Post> posts, long userId, @NonNull Set<PostVisibility> visibilities) {

        return posts.stream()
                .filter(post -> post.getUser().getUsername().equals(getTestingUsername(userId)))
                .filter(post -> visibilities.contains(post.getVisibility()))
                .sorted(new PostComparator().reversed())
                .collect(Collectors.toList());
    }

    public String getTestingUsername(long userId) {
        return String.format("username_%d", userId);
    }

    public String getTestingEmail(@NonNull String username) {
        return String.format("%s@mail.com", username);
    }

    private Post getPostForTesting(@NonNull PostVisibility visibility, long karmaScore, User user) {

        // I ignore fields which will not be useful for query testing.
        return Post.builder()
                .visibility(visibility)
                .karmaScore(karmaScore)
                .user(user)
                .build();
    }

    private User getUserForTesting(long userId, @NonNull Role role) {

        String username = getTestingUsername(userId);

        // I ignore fields which will not be useful for query testing.
        return User.builder()
                .username(username)
                .email(getTestingEmail(username))
                .role(role)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    private void createFirstUserData() {

        User user = getUserForTesting(1, Role.USER);

        usersForTesting.add(user);

        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 11, user));
        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 11, user));
        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 10, user));

        postsForTesting.add(getPostForTesting(PostVisibility.HIDDEN, -11, user));

        postsForTesting.add(getPostForTesting(PostVisibility.DELETED, -100, user));
    }

    private void createSecondUserData() {

        User user = getUserForTesting(2, Role.USER);

        usersForTesting.add(user);

        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 100, user));

        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 111, user));
    }

    private void createThirdUserData() {

        User user = getUserForTesting(3, Role.USER);

        usersForTesting.add(user);

        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 30, user));

        postsForTesting.add(getPostForTesting(PostVisibility.HIDDEN, -11, user));
        postsForTesting.add(getPostForTesting(PostVisibility.HIDDEN, -31, user));
        postsForTesting.add(getPostForTesting(PostVisibility.HIDDEN, -43, user));

        postsForTesting.add(getPostForTesting(PostVisibility.DELETED, -58, user));
    }

    private void createFirstModData() {

        User user = getUserForTesting(4, Role.MOD);

        usersForTesting.add(user);

        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 77, user));

        postsForTesting.add(getPostForTesting(PostVisibility.HIDDEN, -23, user));

        postsForTesting.add(getPostForTesting(PostVisibility.DELETED, -4, user));
    }

    private void createFirstAdminData() {

        User user = getUserForTesting(5, Role.ADMIN);

        usersForTesting.add(user);

        postsForTesting.add(getPostForTesting(PostVisibility.ACTIVE, 777, user));

        postsForTesting.add(getPostForTesting(PostVisibility.HIDDEN, -83, user));

        postsForTesting.add(getPostForTesting(PostVisibility.DELETED, -9, user));
    }
}
