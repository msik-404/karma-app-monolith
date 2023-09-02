package com.msik404.karmaapp;

import java.util.*;
import java.util.stream.Collectors;

import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.karma.KarmaScoreRepository;
import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.post.repository.PostRepository;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.repository.UserRepository;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public class TestingDataCreator {

    private static final int USER_AMOUNT = 3;
    private static final int MOD_AMOUNT = 1;
    private static final int ADMIN_AMOUNT = 1;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final KarmaScoreRepository karmaScoreRepository;

    /**
     * This comparator is made so to mimic the desired sort order, that is ascending id and descending score.
     */
    static class PostComparator implements Comparator<Post> {

        @Override
        public int compare(Post postOne, Post postTwo) {
            if (postOne.getKarmaScore().equals(postTwo.getKarmaScore())) {
                return -postOne.getId().compareTo(postTwo.getId());
            }
            return postOne.getKarmaScore().compareTo(postTwo.getKarmaScore());
        }

    }

    public TestingDataCreator(UserRepository userRepository, PostRepository postRepository, KarmaScoreRepository karmaScoreRepository) {

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.karmaScoreRepository = karmaScoreRepository;
    }

    public void prepareData() {

        List<User> savedUsers = createUsers();

        createFirstUserPostsData(savedUsers);
        createSecondUserPostsData(savedUsers);
        createThirdUserPostsData(savedUsers);
        createFirstModPostsData(savedUsers);
        createFirstAdminPostsData(savedUsers);
    }

    public List<Post> getTopPosts(@NonNull List<Post> posts, @NonNull Set<Visibility> visibilities) {

        return posts.stream()
                .filter(post -> visibilities.contains(post.getVisibility()))
                .sorted(new PostComparator().reversed())
                .collect(Collectors.toList());
    }

    public List<Post> getTopUsersPosts(
            @NonNull List<Post> posts, @NonNull String username, @NonNull Set<Visibility> visibilities) {

        return posts.stream()
                .filter(post -> post.getUser().getUsername().equals(username))
                .filter(post -> visibilities.contains(post.getVisibility()))
                .sorted(new PostComparator().reversed())
                .collect(Collectors.toList());
    }

    public List<PostRatingResponse> getTopPostRatingsOfUser(
            @NonNull List<Post> topPosts,
            @NonNull List<KarmaScore> karmaScores,
            long userId) {

        Map<Long, Boolean> userRatingsMap = karmaScores.stream()
                .filter(karmaScore -> karmaScore.getId().getUserId().equals(userId))
                .collect(Collectors.toMap(karmaScore -> karmaScore.getId().getPostId(), KarmaScore::getIsPositive));

        return topPosts.stream().map(post ->
                PostRatingResponse.builder()
                        .id(post.getId())
                        .wasRatedPositively(userRatingsMap.getOrDefault(post.getId(), null))
                        .build()
        ).toList();
    }

    public String getTestingUsername(long userId) {
        return String.format("username_%d", userId);
    }

    public String getTestingEmail(@NonNull String username) {
        return String.format("%s@mail.com", username);
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

    private Post getPostForTesting(@NonNull Visibility visibility, @NonNull User user, long karmaScore) {

        // I ignore fields which will not be useful for query testing.
        return Post.builder()
                .visibility(visibility)
                .user(user)
                .karmaScore(karmaScore)
                .build();
    }

    private KarmaScore getKarmaScoreForTesting(@NonNull Post post, @NonNull User user, boolean isPositive) {

        return KarmaScore.builder()
                .id(new KarmaKey(user.getId(), post.getId()))
                .post(post)
                .user(user)
                .isPositive(isPositive)
                .build();
    }

    private KarmaScore ratePostByUser(@NonNull Post post, @NonNull User user, boolean isPositive) {
        return getKarmaScoreForTesting(post, user, isPositive);
    }

    private List<User> createUsers() {

        List<User> usersForTesting = new ArrayList<>(USER_AMOUNT + MOD_AMOUNT + ADMIN_AMOUNT);

        for (int i = 0; i < USER_AMOUNT; i++) {
            usersForTesting.add(getUserForTesting(i + 1, Role.USER));
        }
        usersForTesting.add(getUserForTesting(4, Role.MOD));
        usersForTesting.add(getUserForTesting(5, Role.ADMIN));

        return userRepository.saveAll(usersForTesting);
    }

    private void createFirstUserPostsData(@NonNull List<User> savedUsers) {

        final int userId = 0;
        User savedUserOne = savedUsers.get(userId);

        final int karmaScoresAmount = 10;
        List<KarmaScore> karmaScores = new ArrayList<>(karmaScoresAmount);

        Post savedPostOne = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserOne, 3)); // karmaScore = 3;
        for (int i = 0; i < 3; i++) {
            karmaScores.add(ratePostByUser(savedPostOne, savedUsers.get(i), true));
        }

        Post savedPostTwo = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserOne, 3)); // karmaScore = 3;
        for (int i = 0; i < 3; i++) {
            karmaScores.add(ratePostByUser(savedPostTwo, savedUsers.get(i), true));
        }

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserOne, 2)); // karmaScore = 2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(ratePostByUser(savedPostThree, savedUsers.get(i), true));
        }

        postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserOne, 0)); // karmaScore = 0;

        Post savedPostFive = postRepository.save(getPostForTesting(Visibility.DELETED, savedUserOne, -2)); // karmaScore = -2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(ratePostByUser(savedPostFive, savedUsers.get(i), false));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }

    private void createSecondUserPostsData(@NonNull List<User> savedUsers) {

        final int userId = 1;
        User savedUserTwo = savedUsers.get(userId);

        final int karmaScoresAmount = 9;
        List<KarmaScore> karmaScores = new ArrayList<>(karmaScoresAmount);

        Post savedPostOne = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserTwo, 5)); // karmaScore = max = 5;
        for (User user : savedUsers) {
            karmaScores.add(ratePostByUser(savedPostOne, user, true));
        }

        Post savedPostTwo = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserTwo, 4)); // karmaScore = 4;
        for (int i = 0; i < 4; i++) {
            karmaScores.add(ratePostByUser(savedPostTwo, savedUserTwo, true));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }

    private void createThirdUserPostsData(@NonNull List<User> savedUsers) {

        final int userId = 2;
        User savedUserThree = savedUsers.get(userId);

        final int karmaScoresAmount = 7;
        List<KarmaScore> karmaScores = new ArrayList<>(karmaScoresAmount);

        Post savedPostOne = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserThree, 3)); // karmaScore = 3;
        for (int i = 0; i < 3; i++) {
            karmaScores.add(ratePostByUser(savedPostOne, savedUsers.get(i), true));
        }

        postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserThree, 0)); // karmaScore = 0;

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserThree, -1)); // karmaScore = -1
        karmaScores.add(ratePostByUser(savedPostThree, savedUsers.get(0), false));

        Post savedPostFour = postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserThree, -1)); // karmaScore = -1;
        karmaScores.add(ratePostByUser(savedPostFour, savedUsers.get(0), false));

        Post savedPostFive = postRepository.save(getPostForTesting(Visibility.DELETED, savedUserThree, -2)); // karmaScore = -2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(ratePostByUser(savedPostFive, savedUsers.get(i), false));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }

    private void createFirstModPostsData(@NonNull List<User> savedUsers) {

        final int userId = 3;
        User savedModUserOne = savedUsers.get(userId);

        final int karmaScoresAmount = 6;
        List<KarmaScore> karmaScores = new ArrayList<>(karmaScoresAmount);

        postRepository.save(getPostForTesting(Visibility.ACTIVE, savedModUserOne, 0)); // karmaScore = 0;

        Post savedPostTwo = postRepository.save(getPostForTesting(Visibility.HIDDEN, savedModUserOne, -2)); // karmaScore = -2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(ratePostByUser(savedPostTwo, savedUsers.get(i), false));
        }

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.DELETED, savedModUserOne, -4)); // karmaScore = -4;
        for (int i = 0; i < 4; i++) {
            karmaScores.add(ratePostByUser(savedPostThree, savedUsers.get(i), false));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }

    private void createFirstAdminPostsData(@NonNull List<User> savedUsers) {

        final int userId = 4;
        User savedAdminUserOne = savedUsers.get(userId);

        final int karmaScoresAmount = 6;
        final List<KarmaScore> karmaScores = new ArrayList<>(karmaScoresAmount);

        Post savedPostOne = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedAdminUserOne, 1)); // karmaScore = 1;
        karmaScores.add(ratePostByUser(savedPostOne, savedUsers.get(0), true));

        postRepository.save(getPostForTesting(Visibility.HIDDEN, savedAdminUserOne, 0)); // karmaScore = 0;

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.DELETED, savedAdminUserOne, -5)); // karmaScore = min = -5;
        for (User user : savedUsers) {
            karmaScores.add(ratePostByUser(savedPostThree, user, false));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }
}
