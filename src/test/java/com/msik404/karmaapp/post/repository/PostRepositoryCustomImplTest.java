package com.msik404.karmaapp.post.repository;

import java.util.List;

import com.msik404.karmaapp.TestingDataCreator;
import com.msik404.karmaapp.constraintExceptions.ConstraintExceptionsHandler;
import com.msik404.karmaapp.constraintExceptions.strategy.ConstraintViolationExceptionErrorMessageExtractionStrategy;
import com.msik404.karmaapp.constraintExceptions.strategy.RoundBraceErrorMassageParseStrategy;
import com.msik404.karmaapp.post.PostVisibility;
import com.msik404.karmaapp.post.dto.PostJoined;
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

        final int size = 10;
        final List<PostVisibility> visibilities = List.of(PostVisibility.ACTIVE);
        final List<PostJoined> results = postRepository.findTopN(size, visibilities);

        assertEquals(8, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(dataCreator.getTopPosts().get(i).getId(), results.get(i).getId());
            assertEquals(dataCreator.getTopPosts().get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopFiveActive() {

        final int size = 5;
        final List<PostVisibility> visibilities = List.of(PostVisibility.ACTIVE);
        final List<PostJoined> results = postRepository.findTopN(size, visibilities);

        assertEquals(5, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(dataCreator.getTopPosts().get(i).getId(), results.get(i).getId());
            assertEquals(dataCreator.getTopPosts().get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

    @Test
    void findTopOneActive() {

        final int size = 1;
        final List<PostVisibility> visibilities = List.of(PostVisibility.ACTIVE);
        final List<PostJoined> results = postRepository.findTopN(size, visibilities);

        assertEquals(1, results.size());

        for (int i = 0; i < results.size(); i++) {
            assertEquals(dataCreator.getTopPosts().get(i).getId(), results.get(i).getId());
            assertEquals(dataCreator.getTopPosts().get(i).getKarmaScore(), results.get(i).getKarmaScore());
        }
    }

//    @Test
//    void findNextN() {
//    }
//
//    @Test
//    void findTopNWithUsername() {
//    }
//
//    @Test
//    void findNextNWithUsername() {
//    }
//
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