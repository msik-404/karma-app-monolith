package com.msik404.karmaapp.post.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.msik404.karmaapp.TestingDataCreator;
import com.msik404.karmaapp.constraintExceptions.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraintExceptions.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraintExceptions.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.karma.KarmaScoreRepository;
import com.msik404.karmaapp.pagin.Pagination;
import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostRatingResponse;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.repository.UserCriteriaUpdater;
import com.msik404.karmaapp.user.repository.UserRepository;
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
import org.testcontainers.junit.jupiter.Container;
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

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:alpine");

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
    void findTopTenActivePosts() {

        // given
        final int topSize = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(8, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopFiveActivePosts() {

        // given
        final int topSize = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopOneActivePosts() {

        // given
        final int topSize = 1;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopZeroActivePosts() {

        // given
        final int topSize = 0;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());
    }

    @Test
    void findNextTwoActivePostsAfterTopTwoActivePosts() {

        // given
        final int topSize = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        // given
        final int nextSize = 2;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(nextSize, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        final List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextZeroActivePostsAfterTopTwoActivePosts() {

        // given
        final int topSize = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        // given
        final int nextSize = 0;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(nextSize, nextResults.size());
    }

    @Test
    void findNextThreeActivePostsAfterTopFiveActivePosts() {

        // given
        final int topSize = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        // given
        final int nextSize = 3;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(nextSize, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        final List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextThreeActivePostsAfterTopSixActivePosts() {

        // given
        final int topSize = 6;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        // given
        final int nextSize = 3;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(2, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        final List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextThreeActivePostsAfterTopSevenActivePosts() {

        // given
        final int topSize = 7;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        // given
        final int nextSize = 3;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(1, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        final List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPosts.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextThreeActivePostsAfterTopTenActivePosts() {

        // given
        final int topSize = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        // then
        assertEquals(8, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        // given
        final int nextSize = 3;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, pagination);

        // then
        assertEquals(0, nextResults.size());
    }

    @Test
    void findTopFiveActivePostsWithUsername() {

        // given
        final int topSize = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final int userId = 1;
        final String username = TestingDataCreator.getTestingUsername(userId);

        // when
        final List<PostDto> topResults = postRepository.findTopNPostsWithUsername(
                topSize, visibilities, username);

        // then
        assertEquals(3, topResults.size());

        final List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopTwoActivePostsWithNonExistingUsername() {

        // given
        final int topSize = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final int userId = 404;

        // when
        final List<PostDto> topResults = postRepository.findTopNPostsWithUsername(
                topSize, visibilities, TestingDataCreator.getTestingUsername(userId));

        // then
        assertEquals(0, topResults.size());
    }

    @Test
    void findNextOneActivePostsAfterTopTwoActivePostsWithUsername() {

        // given
        final int topSize = 1;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final int userId = 1;
        final String username = TestingDataCreator.getTestingUsername(userId);

        // when
        final List<PostDto> topResults = postRepository.findTopNPostsWithUsername(
                topSize, visibilities, username);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        assertEquals(groundTruthTopPostsOfUserOne.get(0).getId(), topResults.get(0).getId());
        assertEquals(groundTruthTopPostsOfUserOne.get(0).getKarmaScore(), topResults.get(0).getKarmaScore());

        // given
        final int nextSize = 2;
        final PostDto lastPost = topResults.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNPostsWithUsername(
                nextSize, visibilities, pagination, username);

        // then
        assertEquals(nextSize, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPostsOfUserOne.size());
        final List<Post> groundTruthNextPostsOfUserOne = groundTruthTopPostsOfUserOne.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopFourActiveAndHiddenPostsWithUserId() {

        // given
        final int userId = 1;
        final String username = TestingDataCreator.getTestingUsername(userId);

        final Optional<User> optionalPersistedUserId = userRepository.findByUsername(username);

        assertTrue(optionalPersistedUserId.isPresent());

        final int topSize = 4;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN);
        final long persistedUserId = optionalPersistedUserId.get().getId();

        // when
        final List<PostDto> topResults = postRepository.findTopNWithUserId(topSize, visibilities, persistedUserId);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        for (int i = 0; i < topSize; i++) {
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextFourActiveAndHiddenPostsAfterTopOneActiveAndHiddenPostsWithUserId() {

        // given
        final int userId = 1;
        final String username = TestingDataCreator.getTestingUsername(userId);

        final Optional<User> optionalPersistedUserId = userRepository.findByUsername(username);

        assertTrue(optionalPersistedUserId.isPresent());

        final int topSize = 1;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN);
        final long persistedUserId = optionalPersistedUserId.get().getId();

        // when
        final List<PostDto> topResults = postRepository.findTopNWithUserId(topSize, visibilities, persistedUserId);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), username, new HashSet<>(visibilities));

        assertEquals(groundTruthTopPostsOfUserOne.get(0).getId(), topResults.get(0).getId());
        assertEquals(groundTruthTopPostsOfUserOne.get(0).getKarmaScore(), topResults.get(0).getKarmaScore());

        // given
        final int nextSize = 4;
        final int lastPostIdx = 0;
        Post lastPost = groundTruthTopPostsOfUserOne.get(lastPostIdx);
        var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostDto> nextResults = postRepository.findNextNWithUserId(
                nextSize, visibilities, persistedUserId, pagination);

        // then
        assertEquals(3, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPostsOfUserOne.size());
        final List<Post> groundTruthNextPostsOfUserOne = groundTruthTopPostsOfUserOne.subList(topSize, endBound);

        for (int i = 0; i < topSize; i++) {
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopEightActiveRatingsByUserOne() {

        // given
        final int userId = 1;
        final String username = TestingDataCreator.getTestingUsername(userId);

        final Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        final int topSize = 8;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final long persistedUserId = optionalUser.get().getId();

        // when
        final List<PostRatingResponse> topResults = postRepository.findTopNRatings(
                topSize, visibilities, persistedUserId);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        final List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPosts, karmaScoreRepository.findAll(), persistedUserId);

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopRatings.get(i), topResults.get(i));
        }
    }

    @Test
    void findNextTwoActiveRatingsAfterTopFourActiveRatings() {

        // given
        final int userId = 1;
        final String username = TestingDataCreator.getTestingUsername(userId);

        final Optional<User> optionalUser = userRepository.findByUsername(username);

        assertTrue(optionalUser.isPresent());

        final int topSize = 4;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final long persistedUserId = optionalUser.get().getId();

        // when
        final List<PostRatingResponse> topResults = postRepository.findTopNRatings(
                topSize, visibilities, persistedUserId);

        // then
        assertEquals(topSize, topResults.size());

        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        final List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPosts, karmaScoreRepository.findAll(), persistedUserId);

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(groundTruthTopPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(groundTruthTopRatings.get(i), topResults.get(i));
        }

        // given
        final int nextSize = 2;
        final Post lastPost = groundTruthTopPosts.get(topResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostRatingResponse> nextResults = postRepository.findNextNRatings(
                nextSize, visibilities, persistedUserId, pagination);

        // then
        assertEquals(nextSize, nextResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPosts.size());
        final List<Post> groundTruthNextPosts = groundTruthTopPosts.subList(topSize, endBound);
        final List<PostRatingResponse> groundTruthNextRatings = groundTruthTopRatings.subList(topSize, endBound);

        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruthNextPosts.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruthNextRatings.get(i), nextResults.get(i));
        }
    }

    @Test
    void findTopFiveAllVisibilityRatingsByUserOneWithUsername() {

        // given
        final int viewerUserId = 2;
        final String viewerUsername = TestingDataCreator.getTestingUsername(viewerUserId);

        final Optional<User> optionalUser = userRepository.findByUsername(viewerUsername);

        assertTrue(optionalUser.isPresent());

        final int topSize = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN, Visibility.DELETED);

        final long persistedViewerUserId = optionalUser.get().getId();

        final int creatorUserId = 1;
        final String creatorUsername = TestingDataCreator.getTestingUsername(creatorUserId);

        // when
        final List<PostRatingResponse> topCreatorsResults = postRepository.findTopNRatingsWithUsername(
                topSize, visibilities, persistedViewerUserId, creatorUsername);

        // then
        assertEquals(topSize, topCreatorsResults.size());

        final List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), creatorUsername, new HashSet<>(visibilities));

        final List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPostsOfUserOne, karmaScoreRepository.findAll(), persistedViewerUserId);

        for (int i = 0; i < topCreatorsResults.size(); i++) {
            assertEquals(groundTruthTopPostsOfUserOne.get(i).getId(), topCreatorsResults.get(i).getId());
            assertEquals(groundTruthTopRatings.get(i), topCreatorsResults.get(i));
        }
    }

    @Test
    void findNextFourAllVisibilityRatingsAfterTopOneAllVisibilityRatingsWithUsername() {

        // given
        final int viewerUserId = 2;
        final String viewerUsername = TestingDataCreator.getTestingUsername(viewerUserId);

        final Optional<User> optionalUser = userRepository.findByUsername(viewerUsername);

        assertTrue(optionalUser.isPresent());

        final int topSize = 1;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE, Visibility.HIDDEN, Visibility.DELETED);

        final long persistedViewerUserId = optionalUser.get().getId();

        final int creatorUserId = 1;
        final String creatorUsername = TestingDataCreator.getTestingUsername(creatorUserId);

        // when
        final List<PostRatingResponse> topCreatorsResults = postRepository.findTopNRatingsWithUsername(
                topSize, visibilities, persistedViewerUserId, creatorUsername);

        // then
        assertEquals(topSize, topCreatorsResults.size());

        final List<Post> groundTruthTopPostsOfUserOne = TestingDataCreator.getTopUsersPosts(
                postRepository.findAll(), creatorUsername, new HashSet<>(visibilities));

        final List<PostRatingResponse> groundTruthTopRatings = TestingDataCreator.getTopPostRatingsOfUser(
                groundTruthTopPostsOfUserOne, karmaScoreRepository.findAll(), persistedViewerUserId);

        assertEquals(groundTruthTopPostsOfUserOne.get(0).getId(), topCreatorsResults.get(0).getId());
        assertEquals(groundTruthTopRatings.get(0), topCreatorsResults.get(0));

        // given
        final int nextSize = 4;
        final Post lastPost = groundTruthTopPostsOfUserOne.get(topCreatorsResults.size() - 1);
        final var pagination = new Pagination(lastPost.getId(), lastPost.getKarmaScore());

        // when
        final List<PostRatingResponse> nextCreatorResults = postRepository.findNextNRatingsWithUsername(
                nextSize, visibilities, persistedViewerUserId, pagination, creatorUsername
        );

        // then
        assertEquals(nextSize, nextCreatorResults.size());

        final int endBound = Math.min(topSize + nextSize, groundTruthTopPostsOfUserOne.size());
        final List<Post> groundTruthNextPostsOfUserOne = groundTruthTopPostsOfUserOne.subList(topSize, endBound);
        final List<PostRatingResponse> groundTruthNextRatingsOfUserOne = groundTruthTopRatings.subList(topSize, endBound);

        for (int i = 0; i < nextCreatorResults.size(); i++) {
            assertEquals(groundTruthNextPostsOfUserOne.get(i).getId(), nextCreatorResults.get(i).getId());
            assertEquals(groundTruthNextRatingsOfUserOne.get(i), nextCreatorResults.get(i));
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void addKarmaScoreToPost() {

        // given
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
                postRepository.findAll(), new HashSet<>(visibilities));

        Post topPost = groundTruthTopPosts.get(0);

        final int scoreToAdd = 10;

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
    void changeVisibilityById() {

        // given
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<Post> groundTruthTopPosts = TestingDataCreator.getTopPosts(
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