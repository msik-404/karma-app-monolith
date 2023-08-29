package com.msik404.karmaapp.post.repository;

import java.util.List;
import java.util.Set;

import com.msik404.karmaapp.TestingDataCreator;
import com.msik404.karmaapp.constraintExceptions.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraintExceptions.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraintExceptions.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.user.repository.UserCriteriaUpdater;
import com.msik404.karmaapp.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({
        BCryptPasswordEncoder.class,
        UserCriteriaUpdater.class,
        ConstraintExceptionsHandler.class,
        ConstraintViolationExceptionErrorMessageExtractionStrategy.class,
        RoundBraceErrorMassageParseStrategy.class,
})
class PostRepositoryCustomImplTest {

    private final TestingDataCreator dataCreator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    PostRepositoryCustomImplTest() {

        this.dataCreator = new TestingDataCreator();
    }

    @BeforeEach
    void setUp() {

        userRepository.saveAll(dataCreator.getUsersForTesting());
        postRepository.saveAll(dataCreator.getPostsForTesting());
    }

    @AfterEach
    void tearDown() {

        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findTopTenActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> results = postRepository.findTopNPosts(size, visibilities);

        assertEquals(8, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), results.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopFiveActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int size = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> results = postRepository.findTopNPosts(size, visibilities);

        assertEquals(5, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), results.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopOneActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int size = 1;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> results = postRepository.findTopNPosts(size, visibilities);

        assertEquals(1, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), results.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopZeroActive() {

        final int size = 0;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> results = postRepository.findTopNPosts(size, visibilities);

        assertEquals(0, results.size());
    }

    @Test
    void findNextTwoActiveAfterTopTwoActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int topSize = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        assertEquals(2, topResults.size());

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        final long lastPostScore = topResults.get(topResults.size()-1).getKarmaScore();

        final int nextSize = 2;

        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, lastPostScore);

        assertEquals(2, nextResults.size());

        // Posts that should be returned to pass the test
        final int endBound = Math.min(topSize + nextSize, allTopPersistedPosts.size());
        final List<Post> groundTruth = allTopPersistedPosts.subList(topSize, endBound);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruth.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruth.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextZeroActiveAfterTopTwoActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int topSize = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        assertEquals(2, topResults.size());

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        final long lastPostScore = topResults.get(topResults.size()-1).getKarmaScore();

        final int nextSize = 0;

        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, lastPostScore);

        assertEquals(0, nextResults.size());
    }

    @Test
    void findNextThreeActiveAfterTopFiveActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int topSize = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        assertEquals(5, topResults.size());

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        final long lastPostScore = topResults.get(topResults.size()-1).getKarmaScore();

        final int nextSize = 3;

        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, lastPostScore);

        assertEquals(3, nextResults.size());

        // Posts that should be returned to pass the test
        final int endBound = Math.min(topSize + nextSize, allTopPersistedPosts.size());
        final List<Post> groundTruth = allTopPersistedPosts.subList(topSize, endBound);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruth.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruth.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextThreeActiveAfterTopSixActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int topSize = 6;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        assertEquals(6, topResults.size());

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        final long lastPostScore = topResults.get(topResults.size()-1).getKarmaScore();

        final int nextSize = 3;

        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, lastPostScore);

        assertEquals(2, nextResults.size());

        // Posts that should be returned to pass the test
        final int endBound = Math.min(topSize + nextSize, allTopPersistedPosts.size());
        final List<Post> groundTruth = allTopPersistedPosts.subList(topSize, endBound);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruth.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruth.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextThreeActiveAfterTopSevenActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int topSize = 7;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> topResults = postRepository.findTopNPosts(topSize, visibilities);

        assertEquals(7, topResults.size());

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        final long lastPostScore = topResults.get(topResults.size()-1).getKarmaScore();

        final int nextSize = 3;

        final List<PostDto> nextResults = postRepository.findNextNPosts(nextSize, visibilities, lastPostScore);

        assertEquals(1, nextResults.size());

        // Posts that should be returned to pass the test
        final int endBound = Math.min(topSize + nextSize, allTopPersistedPosts.size());
        final List<Post> groundTruth = allTopPersistedPosts.subList(topSize, endBound);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruth.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruth.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

    @Test
    void findNextThreeActiveAfterTopTenActive() {

        // with auto generated ids
        final List<Post> allTopPersistedPosts = dataCreator.getTopPosts(
                postRepository.findAll(), Set.of(Visibility.ACTIVE));

        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final List<PostDto> topResults = postRepository.findTopNPosts(size, visibilities);

        assertEquals(8, topResults.size());

        for (int i = 0; i < topResults.size(); i++) {
            assertEquals(allTopPersistedPosts.get(i).getId(), topResults.get(i).getId());
            assertEquals(allTopPersistedPosts.get(i).getKarmaScore(), topResults.get(i).getKarmaScore());
        }

        final long lastPostScore = topResults.get(topResults.size()-1).getKarmaScore();
        final List<PostDto> nextResults = postRepository.findNextNPosts(size, visibilities, lastPostScore);

        assertEquals(0, nextResults.size());
    }

    @Test
    void findTopThreeWithUsername() {

        // with auto generated ids
        final List<Post> allTopPersistedPostsOfUserOne = dataCreator.getTopUsersPosts(
                postRepository.findAll(), 1, Set.of(Visibility.ACTIVE));

        final int size = 3;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final List<PostDto> results = postRepository.findTopNPostsWithUsername(
                size, visibilities, dataCreator.getTestingUsername(1));

        assertEquals(3, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(allTopPersistedPostsOfUserOne.get(i).getId(), results.get(i).getId());
            assertEquals(allTopPersistedPostsOfUserOne.get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopFiveWithUsername() {

        // with auto generated ids
        final List<Post> allTopPersistedPostsOfUserOne = dataCreator.getTopUsersPosts(
                postRepository.findAll(), 1, Set.of(Visibility.ACTIVE));

        final int size = 5;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final List<PostDto> results = postRepository.findTopNPostsWithUsername(
                size, visibilities, dataCreator.getTestingUsername(1));

        assertEquals(3, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(allTopPersistedPostsOfUserOne.get(i).getId(), results.get(i).getId());
            assertEquals(allTopPersistedPostsOfUserOne.get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopTwoWithUsername() {

        // with auto generated ids
        final List<Post> allTopPersistedPostsOfUserOne = dataCreator.getTopUsersPosts(
                postRepository.findAll(), 1, Set.of(Visibility.ACTIVE));

        final int size = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final List<PostDto> results = postRepository.findTopNPostsWithUsername(
                size, visibilities, dataCreator.getTestingUsername(1));

        assertEquals(2, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(allTopPersistedPostsOfUserOne.get(i).getId(), results.get(i).getId());
            assertEquals(allTopPersistedPostsOfUserOne.get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopZeroWithUsername() {

        final int size = 0;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final List<PostDto> results = postRepository.findTopNPostsWithUsername(
                size, visibilities, dataCreator.getTestingUsername(1));

        assertEquals(0, results.size());
    }

    @Test
    void findTopTwoWithNonExistingUsername() {

        final int size = 2;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        final List<PostDto> results = postRepository.findTopNPostsWithUsername(
                size, visibilities, dataCreator.getTestingUsername(404));

        assertEquals(0, results.size());
    }

    @Test
    void findNextOneActiveAfterTopTwoActiveWithUsername() {

        // with auto generated ids
        final List<Post> allTopPersistedPostsOfUserOne = dataCreator.getTopUsersPosts(
                postRepository.findAll(), 1, Set.of(Visibility.ACTIVE));

        final int topSize = 1;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);
        final String username = dataCreator.getTestingUsername(1);

        final List<PostDto> topResults = postRepository.findTopNPostsWithUsername(
                topSize, visibilities, username);

        assertEquals(1, topResults.size());

        assertEquals(allTopPersistedPostsOfUserOne.get(0).getId(), topResults.get(0).getId());
        assertEquals(allTopPersistedPostsOfUserOne.get(0).getKarmaScore(), topResults.get(0).getKarmaScore());

        final int nextSize = 2;

        final long lastPostScore = topResults.get(0).getKarmaScore();
        final List<PostDto> nextResults = postRepository.findNextNPostsWithUsername(
                nextSize, visibilities, lastPostScore, username);

        final int endBound = Math.min(topSize + nextSize, allTopPersistedPostsOfUserOne.size());
        final List<Post> groundTruth = allTopPersistedPostsOfUserOne.subList(topSize, endBound);

        assertEquals(2, nextResults.size());

        // Posts that should be returned to pass the test
//        final List<Post> groundTruth = dataCreator.getTopPosts().subList(topSize, topSize + nextSize);
        for (int i = 0; i < nextResults.size(); i++) {
            assertEquals(groundTruth.get(i).getId(), nextResults.get(i).getId());
            assertEquals(groundTruth.get(i).getKarmaScore(), nextResults.get(i).getKarmaScore());
        }
    }

//    @Test
//    void testFindTopN() {
//    }
//
//    @Test
//    void testFindNextN() {
//    }
//
//    @Test
//    void testFindTopNWithUsername() {
//    }
//
//    @Test
//    void testFindNextNWithUsername() {
//    }
//
//    @Test
//    void addKarmaScoreToPost() {
//    }
//
//    @Test
//    void changeVisibilityById() {
//    }
//
//    @Test
//    void findTopNWithUserId() {
//    }
//
//    @Test
//    void findNextNWithUserId() {
//    }
}