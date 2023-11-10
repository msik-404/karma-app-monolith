package com.msik404.karmaappmonolith.post.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.msik404.karmaappmonolith.TestingDataCreator;
import com.msik404.karmaappmonolith.exception.constraint.ConstraintExceptionsHandler;
import com.msik404.karmaappmonolith.exception.constraint.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaappmonolith.exception.constraint.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaappmonolith.karma.KarmaScoreRepository;
import com.msik404.karmaappmonolith.position.ScrollPosition;
import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.post.Visibility;
import com.msik404.karmaappmonolith.post.dto.PostDto;
import com.msik404.karmaappmonolith.post.dto.PostRatingResponse;
import com.msik404.karmaappmonolith.user.User;
import com.msik404.karmaappmonolith.user.repository.UserCriteriaUpdater;
import com.msik404.karmaappmonolith.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostRepositoryCustomImplTest.DataSourceInitializer.class)
@Import({
        BCryptPasswordEncoder.class,
        UserCriteriaUpdater.class,
        ConstraintExceptionsHandler.class,
        ConstraintViolationExceptionErrorMessageExtractionStrategy.class,
        RoundBraceErrorMassageParseStrategy.class,
})
class PostRepositoryCustomImplTest {

    private final TestingDataCreator dataCreator;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final KarmaScoreRepository karmaScoreRepository;

    private final TransactionTemplate transactionTemplate;

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:alpine");

    static {
        POSTGRESQL_CONTAINER.start();
    }

    public static class DataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                    "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword()
            );
        }
    }

    @Autowired
    PostRepositoryCustomImplTest(
            UserRepository userRepository,
            PostRepository postRepository,
            KarmaScoreRepository karmaScoreRepository,
            TransactionTemplate transactionTemplate) {

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.karmaScoreRepository = karmaScoreRepository;

        this.dataCreator = new TestingDataCreator(userRepository, postRepository, karmaScoreRepository);

        this.transactionTemplate = transactionTemplate;
    }

    @BeforeEach
    void setUp() {
        dataCreator.prepareData();
    }

    @AfterEach
    void tearDown() {

        karmaScoreRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findTopNPosts_SizeIsTenAndVisibilityIsActive_EightActive() {

        // given
        int topSize = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(8, topResults.size());

        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopNPosts_SizeIsTenAndVisibilityIsActive_FiveActiveFound() {

        // given
        int topSize = 5;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopNPosts_SizeIsOneAndVisibilityIsActive_OneActiveFound() {

        // given
        int topSize = 1;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        assertEquals(groundTruthTopPosts.get(0).getId(), topResults.get(0).getId());
        assertEquals(groundTruthTopPosts.get(0).getKarmaScore(), topResults.get(0).getKarmaScore());
    }

    @Test
    void findTopNPosts_SizeIsZeroAndVisibilityIsActive_zeroActiveFound() {

        // given
        int topSize = 0;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());
    }

    @Test
    void findNextNPosts_NextSizeIsTwoAndTopSizeIsTwoAndVisibilityIsActive_TwoActiveAfterTopTwoActiveFound() {

        // given
        int nextSize = 2;
        int topSize = 2;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPosts.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(nextSize, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextNPosts_NextSizeIsZeroAndTopSizeIsTwoAndVisibilityIsActive_ZeroFound() {

        // given
        int nextSize = 0;
        int topSize = 2;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPosts.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(nextSize, nextResults.size());
    }

    @Test
    void findNextNPosts_NextSizeIsThreeAndTopSizeIsFiveAndVisibilityIsActive_ThreeActiveAfterTopSixActiveFound() {

        // given
        int nextSize = 3;
        int topSize = 5;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPosts.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(nextSize, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextNPosts_NextSizeIsThreeAndTopSizeIsSixAndVisibilityIsActive_TwoActiveAfterTopSixActiveFound() {

        // given
        int nextSize = 3;
        int topSize = 6;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        Post lastPost = groundTruthTopPosts.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(2, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextNPosts_NextSizeIsThreeAndTopSizeIsSevenAndVisibilityIsActive_OneActiveAfterTopSevenActiveFound() {

        // given
        int nextSize = 3;
        int topSize = 7;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPosts.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(1, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextNPosts_NextSizeIsThreeAndTopSizeIsTenAndVisibilityIsActive_ZeroFound() {

        // given
        int nextSize = 3;
        int topSize = 10;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        int lastIdx = Math.min(topSize - 1, groundTruthTopPosts.size() - 1);
        Post lastPost = groundTruthTopPosts.get(lastIdx);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(0, nextResults.size());
    }

    @Test
    void findTopNPostsWithUsername_SizeIsFiveAndVisibilityIsActiveAndUsernameIsOne_ThreeActiveFound() {

        // given
        int topSize = 5;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        int userId = 1;
        String username = TestingDataCreator.getTestingUsername(userId);

        // when
        List<PostDto> topResults = postRepository.findTopNPostsWithUsername(
                topSize, visibilities, username);

        // then
        assertEquals(3, topResults.size());

        List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopNPostsWithUsername_SizeIsTwoAndVisibilityIsActiveAndUsernameIsNonExisting_ZeroActiveFound() {

        // given
        int topSize = 2;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        int userId = 404;

        // when
        List<PostDto> topResults = postRepository.findTopNPostsWithUsername(
                topSize, visibilities, TestingDataCreator.getTestingUsername(userId));

        // then
        assertEquals(0, topResults.size());
    }

    @Test
    void findTopNPostsWithUsername_NextSizeIsTwoAndTopSizeIsOneAndVisibilityIsActiveAndUsernameIsOne_TwoActiveFound() {

        // given
        int nextSize = 2;
        int topSize = 1;

        int userId = 1;
        String username = TestingDataCreator.getTestingUsername(userId);

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPostsOfUserOne.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNPostsWithUsername(
                nextSize, visibilities, pagination, username);

        // then
        assertEquals(nextSize, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPostsOfUserOne.size());
        List<Post> groundTruthNextPostsOfUserOne = groundTruthTopPostsOfUserOne.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopNWithUserId_SizeIsFourVisibilityIsActiveOrIsHiddenAndUserIdIsOne_FourActiveFound() {

        // given
        int userId = 1;
        String username = TestingDataCreator.getTestingUsername(userId);
        Optional<User> optionalPersistedUserId = userRepository.findByUsername(username);
        assertTrue(optionalPersistedUserId.isPresent());
        long persistedUserId = optionalPersistedUserId.get().getId();

        int topSize = 4;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN);

        // when
        List<PostDto> topResults = postRepository.findTopNWithUserId(topSize, visibilities, persistedUserId);

        // then
        assertEquals(topSize, topResults.size());

        List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        for (int i = 0; i < topSize; i++) {
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextNWithUserId_NextSizeIsFourAndTopSizeIsOneAndVisibilityIsActiveOrIsHiddenAndUserIdIsOne_ThreeActiveFound() {

        // given
        int userId = 1;
        String username = TestingDataCreator.getTestingUsername(userId);
        Optional<User> optionalPersistedUserId = userRepository.findByUsername(username);
        assertTrue(optionalPersistedUserId.isPresent());
        long persistedUserId = optionalPersistedUserId.get().getId();

        int nextSize = 4;
        int topSize = 1;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN);

        List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPostsOfUserOne.get(topSize - 1);
        var position = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostDto> nextResults = postRepository.findNextNWithUserId(
                nextSize, visibilities, persistedUserId, position);

        // then
        assertEquals(3, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPostsOfUserOne.size());
        List<Post> groundTruthNextPostsOfUserOne = groundTruthTopPostsOfUserOne.subList(topSize, endBound);

        for (int i = 0; i < topSize; i++) {
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopNRatings_SizeIsEightAndVisibilityIsActiveAndUsernameIsOne_EightActiveFound() {

        // given
        int userId = 1;
        String username = TestingDataCreator.getTestingUsername(userId);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        assertTrue(optionalUser.isPresent());
        long persistedUserId = optionalUser.get().getId();

        int topSize = 8;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        List<PostRatingResponse> topResults = postRepository.findTopNRatings(
                topSize, visibilities, persistedUserId);

        // then
        assertEquals(topSize, topResults.size());

        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPosts, karmaScoreRepository.findAll(), persistedUserId);

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).id());
            assertEquals(groundTruthTopRatings.get(i), topResults.get(i));
        }
    }

    @Test
    void findNextNRatings_NextSizeIsTwoAndTopSizeIsFourAndVisibilityIsActiveAndUserIdIsOne_TwoActiveFound() {

        // given
        int userId = 1;
        String username = TestingDataCreator.getTestingUsername(userId);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        assertTrue(optionalUser.isPresent());
        long persistedUserId = optionalUser.get().getId();

        int nextSize = 2;
        int topSize = 4;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPosts.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostRatingResponse> nextResults = postRepository.findNextNRatings(
                nextSize, visibilities, persistedUserId, pagination);

        // then
        assertEquals(nextSize, nextResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPosts, karmaScoreRepository.findAll(), persistedUserId);

        List<PostRatingResponse> groundTruthNextRatings = groundTruthTopRatings.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).id());
            assertEquals(groundTruthNextRatings.get(i), nextResults.get(i));
        }
    }

    @Test
    void findTopNRatingsWithUsername_SizeIsFiveAndVisibilityIsActiveOrIsHiddenOrIsDeletedAndUserIdIsOneAndUsernameIsTwo_FiveActiveFound() {

        // given
        int viewerUserId = 2;
        String viewerUsername = TestingDataCreator.getTestingUsername(viewerUserId);
        Optional<User> optionalUser = userRepository.findByUsername(viewerUsername);
        assertTrue(optionalUser.isPresent());
        long persistedViewerUserId = optionalUser.get().getId();

        int topSize = 5;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN, Visibility.DELETED);

        int creatorUserId = 1;
        String creatorUsername = TestingDataCreator.getTestingUsername(creatorUserId);

        // when
        List<PostRatingResponse> topCreatorsResults = postRepository.findTopNRatingsWithUsername(
                topSize, visibilities, persistedViewerUserId, creatorUsername);

        // then
        assertEquals(topSize, topCreatorsResults.size());

        List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), creatorUsername, new HashSet<>(visibilities));

        List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPostsOfUserOne, karmaScoreRepository.findAll(), persistedViewerUserId);

        for (int i = 0; i < topCreatorsResults.size(); i++) {
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getId(), topCreatorsResults.get(i).id());
            assertEquals(groundTruthTopRatings.get(i), topCreatorsResults.get(i));
        }
    }

    @Test
    void findNextNRatingsWithUsername_NextSizeIsFourAndTopSizeIsOneAndVisibilityIsActiveOrIsHiddenOrIsDeletedAndUserIdIsOneAndUsernameIsTwo_FourActiveFound() {

        // given
        int viewerUserId = 2;
        String viewerUsername = TestingDataCreator.getTestingUsername(viewerUserId);
        Optional<User> optionalUser = userRepository.findByUsername(viewerUsername);
        assertTrue(optionalUser.isPresent());
        long persistedViewerUserId = optionalUser.get().getId();

        int nextSize = 4;
        int topSize = 1;

        List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN, Visibility.DELETED);

        int creatorUserId = 1;
        String creatorUsername = TestingDataCreator.getTestingUsername(creatorUserId);

        List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), creatorUsername, new HashSet<>(visibilities));

        Post lastPost = groundTruthTopPostsOfUserOne.get(topSize - 1);
        var pagination = new ScrollPosition(lastPost.getId(), lastPost.getKarmaScore());

        // when
        List<PostRatingResponse> nextCreatorResults = postRepository.findNextNRatingsWithUsername(
                nextSize, visibilities, persistedViewerUserId, pagination, creatorUsername
        );

        // then
        assertEquals(nextSize, nextCreatorResults.size());

        int endBound = Math.min(topSize + nextSize, groundTruthTopPostsOfUserOne.size());
        List<Post> groundTruthNextPostsOfUserOne = groundTruthTopPostsOfUserOne.subList(topSize, endBound);

        List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPostsOfUserOne, karmaScoreRepository.findAll(), persistedViewerUserId);

        List<PostRatingResponse> groundTruthNextRatingsOfUserOne = groundTruthTopRatings.subList(topSize, endBound);

        for (int i = 0; i < nextCreatorResults.size(); i++) {
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getId(), nextCreatorResults.get(i).id());
            assertEquals(groundTruthNextRatingsOfUserOne.get(i), nextCreatorResults.get(i));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void addKarmaScoreToPost_PostIdIsTopAndScoreIsTenAndVisibilityIsActive_ScoreIsIncreased() {

        // given
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post topPost = groundTruthTopPosts.get(0);

        int scoreToAdd = 10;

        // when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
                postRepository.addKarmaScoreToPost(topPost.getId(), scoreToAdd);
            }
        });

        // then
        Optional<Post> optionalUpdatedPost = postRepository.findById(topPost.getId());

        assertTrue(optionalUpdatedPost.isPresent());

        Post updatedPost = optionalUpdatedPost.get();

        assertEquals(topPost.getKarmaScore() + scoreToAdd, updatedPost.getKarmaScore());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void changeVisibilityById_PostIdIsTopAndVisibilityIsActiveAndNewVisibilityIsDeleted_VisibilityIsDeleted() {

        // given
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post topPost = groundTruthTopPosts.get(0);

        Visibility newVisibility = Visibility.DELETED;

        // when
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus transactionStatus) {
                postRepository.changeVisibilityById(topPost.getId(), newVisibility);
            }
        });

        // then
        Optional<Post> optionalUpdatedPost = postRepository.findById(topPost.getId());

        assertTrue(optionalUpdatedPost.isPresent());

        Post updatedPost = optionalUpdatedPost.get();

        assertEquals(newVisibility, updatedPost.getVisibility());
    }
}