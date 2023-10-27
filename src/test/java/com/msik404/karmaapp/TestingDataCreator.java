package com.msik404.karmaapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.karma.KarmaScoreRepository;
import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.comparator.PostComparator;
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

    public static List<Post> getTopPosts(@NonNull List<Post> posts, @NonNull Set<Visibility> visibilities) {

        return posts.stream()
                .filter(post -> visibilities.contains(post.getVisibility()))
                .sorted(new PostComparator().reversed())
                .collect(Collectors.toList());
    }

    public static List<Post> getTopUsersPosts(
            @NonNull List<Post> posts, @NonNull String username, @NonNull Set<Visibility> visibilities) {

        return posts.stream()
                .filter(post -> post.getUser().getUsername().equals(username))
                .filter(post -> visibilities.contains(post.getVisibility()))
                .sorted(new PostComparator().reversed())
                .collect(Collectors.toList());
    }

    public static List<PostRatingResponse> getTopPostRatingsOfUser(
            @NonNull List<Post> topPosts,
            @NonNull List<KarmaScore> karmaScores,
            long userId) {

        Map<Long, Boolean> userRatingsMap = karmaScores.stream()
                .filter(karmaScore -> karmaScore.getId().getUserId().equals(userId))
                .collect(Collectors.toMap(karmaScore -> karmaScore.getId().getPostId(), KarmaScore::getIsPositive));

        return topPosts.stream().map(post -> new PostRatingResponse(
                post.getId(),
                userRatingsMap.getOrDefault(post.getId(), null)
        )).toList();
    }

    public static String getTestingUsername(long userId) {
        return String.format("username_%d", userId);
    }

    public static String getTestingEmail(@NonNull String username) {
        return String.format("%s@mail.com", username);
    }

    public static User getUserForTesting(long userId, @NonNull Role role) {

        String username = getTestingUsername(userId);

        // I ignore fields which will not be useful for query testing.
        return User.builder()
                .username(username)
                .email(getTestingEmail(username))
                .role(role)
                .build();
    }

    @NonNull
    private static Post getPostForTesting(@NonNull Visibility visibility, @NonNull User user, long karmaScore) {

        // I ignore fields which will not be useful for query testing.
        return new Post(
                null,
                null,
                null,
                karmaScore,
                visibility,
                user,
                null,
                null
        );
    }

    private static KarmaScore getKarmaScoreForTesting(@NonNull Post post, @NonNull User user, boolean isPositive) {

        return com.msik404.karmaapp.karma.KarmaScore.builder()
                .id(new KarmaKey(user.getId(), post.getId()))
                .post(post)
                .user(user)
                .isPositive(isPositive)
                .build();
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
            karmaScores.add(getKarmaScoreForTesting(savedPostOne, savedUsers.get(i), true));
        }

        Post savedPostTwo = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserOne, 3)); // karmaScore = 3;
        for (int i = 0; i < 3; i++) {
            karmaScores.add(getKarmaScoreForTesting(savedPostTwo, savedUsers.get(i), true));
        }

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserOne, 2)); // karmaScore = 2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(getKarmaScoreForTesting(savedPostThree, savedUsers.get(i), true));
        }

        postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserOne, 0)); // karmaScore = 0;

        Post savedPostFive = postRepository.save(getPostForTesting(Visibility.DELETED, savedUserOne, -2)); // karmaScore = -2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(getKarmaScoreForTesting(savedPostFive, savedUsers.get(i), false));
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
            karmaScores.add(getKarmaScoreForTesting(savedPostOne, user, true));
        }

        Post savedPostTwo = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedUserTwo, 4)); // karmaScore = 4;
        for (int i = 0; i < 4; i++) {
            karmaScores.add(getKarmaScoreForTesting(savedPostTwo, savedUserTwo, true));
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
            karmaScores.add(getKarmaScoreForTesting(savedPostOne, savedUsers.get(i), true));
        }

        postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserThree, 0)); // karmaScore = 0;

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserThree, -1)); // karmaScore = -1
        karmaScores.add(getKarmaScoreForTesting(savedPostThree, savedUsers.get(0), false));

        Post savedPostFour = postRepository.save(getPostForTesting(Visibility.HIDDEN, savedUserThree, -1)); // karmaScore = -1;
        karmaScores.add(getKarmaScoreForTesting(savedPostFour, savedUsers.get(0), false));

        Post savedPostFive = postRepository.save(getPostForTesting(Visibility.DELETED, savedUserThree, -2)); // karmaScore = -2;
        for (int i = 0; i < 2; i++) {
            karmaScores.add(getKarmaScoreForTesting(savedPostFive, savedUsers.get(i), false));
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
            karmaScores.add(getKarmaScoreForTesting(savedPostTwo, savedUsers.get(i), false));
        }

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.DELETED, savedModUserOne, -4)); // karmaScore = -4;
        for (int i = 0; i < 4; i++) {
            karmaScores.add(getKarmaScoreForTesting(savedPostThree, savedUsers.get(i), false));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }

    private void createFirstAdminPostsData(@NonNull List<User> savedUsers) {

        final int userId = 4;
        User savedAdminUserOne = savedUsers.get(userId);

        final int karmaScoresAmount = 6;
        final List<KarmaScore> karmaScores = new ArrayList<>(karmaScoresAmount);

        Post savedPostOne = postRepository.save(getPostForTesting(Visibility.ACTIVE, savedAdminUserOne, 1)); // karmaScore = 1;
        karmaScores.add(getKarmaScoreForTesting(savedPostOne, savedUsers.get(0), true));

        postRepository.save(getPostForTesting(Visibility.HIDDEN, savedAdminUserOne, 0)); // karmaScore = 0;

        Post savedPostThree = postRepository.save(getPostForTesting(Visibility.DELETED, savedAdminUserOne, -5)); // karmaScore = min = -5;
        for (User user : savedUsers) {
            karmaScores.add(getKarmaScoreForTesting(savedPostThree, user, false));
        }

        karmaScoreRepository.saveAll(karmaScores);
    }
}
