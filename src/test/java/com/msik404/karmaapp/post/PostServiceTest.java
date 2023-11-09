package com.msik404.karmaapp.post;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

import com.msik404.karmaapp.TestingImageDataCreator;
import com.msik404.karmaapp.auth.exception.InsufficientRoleException;
import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.karma.KarmaScoreRepository;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaapp.position.ScrollPosition;
import com.msik404.karmaapp.post.cache.PostRedisCache;
import com.msik404.karmaapp.post.cache.PostRedisCacheHandlerService;
import com.msik404.karmaapp.post.dto.ImageOnlyDto;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostWithImageDataDto;
import com.msik404.karmaapp.post.exception.FileProcessingException;
import com.msik404.karmaapp.post.exception.ImageNotFoundException;
import com.msik404.karmaapp.post.exception.PostNotFoundException;
import com.msik404.karmaapp.post.exception.PostNotFoundOrClientIsNotOwnerException;
import com.msik404.karmaapp.post.repository.PostRepository;
import com.msik404.karmaapp.user.Role;
import com.msik404.karmaapp.user.User;
import com.msik404.karmaapp.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @Mock
    private UserRepository userRepository;


    @Mock
    private KarmaScoreRepository karmaScoreRepository;

    @Mock
    private KarmaScoreService karmaScoreService;

    @Mock
    private PostRedisCache cache;

    @Mock
    private PostRedisCacheHandlerService cacheHandler;

    @InjectMocks
    private PostService postService;

    @Test
    void findPaginatedPosts_PaginationIsNullAndUsernameIsNull_CacheHandlerFindTopNHandlerCalled() {

        // given
        ScrollPosition position = null;
        String username = null;

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        postService.findPaginatedPosts(size, visibilities, position, username);

        // then
        verify(cacheHandler).findTopNHandler(size, visibilities);
        verify(repository, never()).findNextNPostsWithUsername(size, visibilities, position, username);
        verify(cacheHandler, never()).findNextNHandler(size, visibilities, position);
        verify(repository, never()).findTopNPostsWithUsername(size, visibilities, username);
    }

    @Test
    void findPaginatedPosts_PaginationIsNonNullAndUsernameIsNonNull_RepositoryFindNextNPostsWithUsernameCalled() {

        // given
        var pagination = new ScrollPosition(0, 0);
        String username = "username";

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        postService.findPaginatedPosts(size, visibilities, pagination, username);

        // then
        verify(cacheHandler, never()).findTopNHandler(size, visibilities);
        verify(repository).findNextNPostsWithUsername(size, visibilities, pagination, username);
        verify(cacheHandler, never()).findNextNHandler(size, visibilities, pagination);
        verify(repository, never()).findTopNPostsWithUsername(size, visibilities, username);
    }

    @Test
    void findPaginatedPosts_PaginationIsNonNullAndUsernameIsNull_CacheHandlerFindNextNHandlerCalled() {

        // given
        var pagination = new ScrollPosition(0, 0);
        String username = null;

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        postService.findPaginatedPosts(size, visibilities, pagination, username);

        // then
        verify(cacheHandler, never()).findTopNHandler(size, visibilities);
        verify(repository, never()).findNextNPostsWithUsername(size, visibilities, pagination, username);
        verify(cacheHandler).findNextNHandler(size, visibilities, pagination);
        verify(repository, never()).findTopNPostsWithUsername(size, visibilities, username);
    }

    @Test
    void findPaginatedPosts_PaginationIsNullAndUsernameIsNonNull_RepositoryFindTopNPostsWithUsernameCalled() {

        // given
        ScrollPosition position = null;
        String username = "username";

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        postService.findPaginatedPosts(size, visibilities, position, username);

        // then
        verify(cacheHandler, never()).findTopNHandler(size, visibilities);
        verify(repository, never()).findNextNPostsWithUsername(size, visibilities, position, username);
        verify(cacheHandler, never()).findNextNHandler(size, visibilities, position);
        verify(repository).findTopNPostsWithUsername(size, visibilities, username);
    }

    @Test
    void findPaginatedOwnedPosts_PaginationIsNull_FindTopNWithUserIdCalled() {

        // given
        ScrollPosition position = null;

        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedOwnedPosts(size, visibilities, position);

        // then
        verify(repository).findTopNWithUserId(size, visibilities, userId);
        verify(repository, never()).findNextNWithUserId(size, visibilities, userId, position);
    }

    @Test
    void findPaginatedOwnedPosts_PaginationIsNonNull_FindNextNWithUserIdCalled() {

        // given
        var pagination = new ScrollPosition(0, 0);

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedOwnedPosts(size, visibilities, pagination);

        // then
        verify(repository, never()).findTopNWithUserId(size, visibilities, userId);
        verify(repository).findNextNWithUserId(size, visibilities, userId, pagination);
    }

    @Test
    void findPaginatedPostRatings_PaginationIsNullAndUsernameIsNull_RepositoryFindTopNRatingsCalled() {

        // given
        ScrollPosition position = null;
        String username = null;

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedPostRatings(size, visibilities, position, username);

        // then
        verify(repository).findTopNRatings(size, visibilities, userId);
        verify(repository, never()).findNextNRatingsWithUsername(size, visibilities, userId, position, username);
        verify(repository, never()).findNextNRatings(size, visibilities, userId, position);
        verify(repository, never()).findTopNRatingsWithUsername(size, visibilities, userId, username);
    }

    @Test
    void findPaginatedPostRatings_PaginationIsNonNullAndUsernameIsNonNull_RepositoryFindNextNRatingsWithUsernameCalled() {

        // given
        var pagination = new ScrollPosition(0, 0);
        String username = "username";

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedPostRatings(size, visibilities, pagination, username);

        // then
        verify(repository, never()).findTopNRatings(size, visibilities, userId);
        verify(repository).findNextNRatingsWithUsername(size, visibilities, userId, pagination, username);
        verify(repository, never()).findNextNRatings(size, visibilities, userId, pagination);
        verify(repository, never()).findTopNRatingsWithUsername(size, visibilities, userId, username);
    }

    @Test
    void findPaginatedPostRatings_PaginationIsNonNullAndUsernameIsNull_RepositoryFindNextNRatingsCalled() {

        // given
        var pagination = new ScrollPosition(0, 0);
        String username = null;

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedPostRatings(size, visibilities, pagination, username);

        // then
        verify(repository, never()).findTopNRatings(size, visibilities, userId);
        verify(repository, never()).findNextNRatingsWithUsername(size, visibilities, userId, pagination, username);
        verify(repository).findNextNRatings(size, visibilities, userId, pagination);
        verify(repository, never()).findTopNRatingsWithUsername(size, visibilities, userId, username);
    }

    @Test
    void findPaginatedPostRatings_PaginationIsNullAndUsernameIsNonNull_RepositoryFindTopNRatingsWithUsernameCalled() {

        // given
        ScrollPosition position = null;
        String username = "username";

        // these values won't influence results of these test
        int size = 10;
        List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedPostRatings(size, visibilities, position, username);

        // then
        verify(repository, never()).findTopNRatings(size, visibilities, userId);
        verify(repository, never()).findNextNRatingsWithUsername(size, visibilities, userId, position, username);
        verify(repository, never()).findNextNRatings(size, visibilities, userId, position);
        verify(repository).findTopNRatingsWithUsername(size, visibilities, userId, username);
    }

    @Test
    void findImageByPostId_CacheHasRequestedImage_RepositoryShouldNotBeCalled() {

        // given
        long postId = 10;
        byte[] data = new byte[0];

        when(cache.getCachedImage(postId)).thenReturn(Optional.of(data));

        // when
        postService.findImageByPostId(postId);

        // then
        verify(cache).getCachedImage(postId);
        verify(repository, never()).findImageById(postId);
        verify(cache, never()).cacheImage(eq(postId), any(byte[].class));
    }

    @Test
    void findImageByPostId_CacheDoesNotHaveRequestedImage_RepositoryShouldBeCalled() {

        // given
        long postId = 10;

        when(cache.getCachedImage(postId)).thenReturn(Optional.empty());
        when(repository.findImageById(postId)).thenReturn(Optional.of(new ImageOnlyDto(new byte[1])));

        // when
        postService.findImageByPostId(postId);

        // then
        verify(cache).getCachedImage(postId);
        verify(repository).findImageById(postId);
        verify(cache).cacheImage(eq(postId), any(byte[].class));
    }

    @Test
    void findImageByPostId_CacheDoesNotHaveRequestedImageNorRepository_ImageNotFoundExceptionShouldBeThrown() {

        // given
        long postId = 10;

        when(cache.getCachedImage(postId)).thenReturn(Optional.empty());
        when(repository.findImageById(postId)).thenReturn(Optional.empty());

        // when
        assertThrows(ImageNotFoundException.class, () -> postService.findImageByPostId(postId));

        // then
        verify(cache).getCachedImage(postId);
        verify(repository).findImageById(postId);
        verify(cache, never()).cacheImage(eq(postId), any(byte[].class));
    }

    @Test
    void findImageByPostId_CacheHasRequestedImageButItsDataIsEmpty_ImageNotFoundExceptionShouldBeThrown() {

        // given
        long postId = 10;

        when(cache.getCachedImage(postId)).thenReturn(Optional.empty());
        when(repository.findImageById(postId)).thenReturn(Optional.of(new ImageOnlyDto(new byte[0])));

        // when
        assertThrows(ImageNotFoundException.class, () -> postService.findImageByPostId(postId));

        // then
        verify(cache).getCachedImage(postId);
        verify(repository).findImageById(postId);
        verify(cache, never()).cacheImage(eq(postId), any(byte[].class));
    }

    @Test
    void create_ImageIsNotEmpty_PostWithImageShouldBeSaved() {

        // given
        var request = new PostCreationRequest("headline", "text");

        // mock image
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        byte[] imageData = TestingImageDataCreator.getTestingImage();
        var inputStream = new ByteArrayInputStream(imageData);

        try {
            when(image.getInputStream()).thenReturn(inputStream);
        } catch (IOException ex) {
            // this will never be called
        }

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var groundTruthUser = new User(
                10L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var groundTruthPost = new Post(
                11L,
                request.headline(),
                request.text(),
                0L,
                Visibility.ACTIVE,
                groundTruthUser,
                null,
                TestingImageDataCreator.jpegCompress(imageData)
        );

        when(userRepository.getReferenceById(userId)).thenReturn(groundTruthUser);

        // groundTruthPost already has id to mimic jpa id auto-generation
        when(repository.save(any(Post.class))).thenReturn(groundTruthPost);

        var postCaptor = ArgumentCaptor.forClass(Post.class);

        // when
        postService.create(request, image);

        // then
        verify(repository).save(postCaptor.capture());

        Post postCaptorValue = postCaptor.getValue();
        // post before repository save should have null id
        groundTruthPost.setId(null);
        assertEquals(groundTruthPost, postCaptorValue);
    }

    @Test
    void create_ImageIsNotEmptyButInvalid_FileProcessingExceptionShouldBeThrown() {

        // given
        var request = new PostCreationRequest(null, null);

        // mock image
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        byte[] imageData = "sampleImageData".getBytes();
        var inputStream = new ByteArrayInputStream(imageData);

        try {
            when(image.getInputStream()).thenReturn(inputStream);
        } catch (IOException ex) {
            // this will never be called
        }

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var groundTruthUser = new User(
                10L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // when
        assertThrows(FileProcessingException.class, () -> postService.create(request, image));

        // then
        verify(repository, never()).save(any(Post.class));
    }

    @Test
    void create_ImageIsEmpty_PostWithoutImageShouldBeSavedAndImageShouldNotBeCached() {

        // given
        var request = new PostCreationRequest("headline", "text");

        // mock image
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var groundTruthUser = new User(
                10L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var groundTruthPost = new Post(
                11L,
                request.headline(),
                request.text(),
                0L,
                Visibility.ACTIVE,
                groundTruthUser,
                null,
                null
        );

        when(userRepository.getReferenceById(userId)).thenReturn(groundTruthUser);
        when(repository.save(any(Post.class))).thenReturn(groundTruthPost);

        var postCaptor = ArgumentCaptor.forClass(Post.class);

        // when
        postService.create(request, image);

        // then
        verify(repository).save(postCaptor.capture());
        verify(cache, never()).cacheImage(groundTruthPost.getId(), groundTruthPost.getImageData());

        Post postCaptorValue = postCaptor.getValue();
        // post before repository save should have null id
        groundTruthPost.setId(null);
        assertEquals(groundTruthPost, postCaptorValue);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNegativeAndWasNotCached_DeltaIsTwoAndShouldTryToLoadToCache() {

        // given
        long postId = 1;
        boolean isNewRatingPositive = true;
        boolean isOldRatingPositive = false;
        long delta = 2;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = new KarmaScore(karmaKey, null, null, isOldRatingPositive);

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        when(cacheHandler.loadPostDataToCacheIfPossible(postId)).thenReturn(true);

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService, never()).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsFalseAndOldRatingIsPositiveAndWasNotCached_DeltaIsMinusTwoAndShouldTryToLoadToCache() {

        // given
        long postId = 1;
        boolean isNewRatingPositive = false;
        boolean isOldRatingPositive = true;
        long delta = -2;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = new KarmaScore(karmaKey, null, null, isOldRatingPositive);

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        when(cacheHandler.loadPostDataToCacheIfPossible(postId)).thenReturn(true);

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService, never()).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNullAndWasNotCached_DeltaIsOneAndShouldTryToLoadToCache() {

        // given
        long postId = 1;
        boolean isNewRatingPositive = true;
        long delta = 1;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        when(cacheHandler.loadPostDataToCacheIfPossible(postId)).thenReturn(true);

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNullAndWasNotCachedAndPostIsNotFound_ShouldThrowPostNotFoundException() {

        // given
        long postId = 1;
        boolean isNewRatingPositive = true;
        long delta = 1;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        int rowsAffected = 0;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        // when
        assertThrows(PostNotFoundException.class, () -> postService.rate(postId, isNewRatingPositive));

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache, never()).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler, never()).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsPositiveAndWasNotCached_ShouldEarlyReturn() {

        // given
        long postId = 1;
        boolean isNewRatingPositive = true;
        boolean isOldRatingPositive = true;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = new KarmaScore(karmaKey, null, null, isOldRatingPositive);

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        // when
        assertDoesNotThrow(() -> postService.rate(postId, isNewRatingPositive));

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService, never()).create(userId, postId, isNewRatingPositive);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNullAndIsCached_DeltaIsOneAndShouldNotTryToLoadToCache() {

        // given
        long postId = 1;
        boolean isNewRatingPositive = true;
        long delta = 1;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.of(1));

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler, never()).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void unrate_PostExistsAndWasRatedPositivelyAndIsCached_KarmaScoreShouldBeFoundAndDeletedPostKarmaScoreShouldBeUpdatedAndCachedKarmaScoreShouldBeUpdatedAndShouldNotTryToLoadToCache() {

        // given
        long postId = 1;
        boolean isPositive = true;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = new KarmaScore(karmaKey, null, null, isPositive);

        when(karmaScoreRepository.findById(karmaKey)).thenReturn(Optional.of(karmaScore));

        long delta = -1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(1);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.of(1));

        // when
        postService.unrate(postId);

        // then
        verify(karmaScoreRepository).findById(karmaKey);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(karmaScoreService).deleteById(karmaKey);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler, never()).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void unrate_PostExistsAndWasRatedPositivelyAndIsNotCached_KarmaScoreShouldBeFoundAndDeletedPostKarmaScoreShouldBeUpdatedAndShouldTryToLoadToCache() {

        // given
        long postId = 1;
        boolean isPositive = true;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = new KarmaScore(karmaKey, null, null, isPositive);

        when(karmaScoreRepository.findById(karmaKey)).thenReturn(Optional.of(karmaScore));

        long delta = -1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(1);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        // when
        postService.unrate(postId);

        // then
        verify(karmaScoreRepository).findById(karmaKey);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(karmaScoreService).deleteById(karmaKey);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void unrate_PostWasRatedPositivelyButPostWasNotFound_PostNotFoundExceptionShouldBeThrown() {

        // given
        long postId = 1;
        boolean isPositive = true;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        var karmaKey = new KarmaKey(userId, postId);
        var karmaScore = new KarmaScore(karmaKey, null, null, isPositive);

        when(karmaScoreRepository.findById(karmaKey)).thenReturn(Optional.of(karmaScore));

        long delta = -1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(0);

        // then                                         // when
        assertThrows(PostNotFoundException.class, () -> postService.unrate(postId));
    }

    @Test
    void changeVisibility_PostExistsAndNewVisibilityIsHidden_PostVisibilityInRepositoryShouldBeUpdatedAndPostShouldBeDeletedFromCacheIfPresent() {

        // given
        int postId = 1;
        var visibility = Visibility.HIDDEN;

        when(repository.changeVisibilityById(postId, visibility)).thenReturn(1);
        when(cache.deletePostFromCache(postId)).thenReturn(true);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        var authorities = Set.of(new SimpleGrantedAuthority(Role.ADMIN.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.changeVisibility(postId, visibility);

        // then
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cache).deletePostFromCache(postId);
        verify(cacheHandler, never()).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void changeVisibility_PostExistsAndNewVisibilityIsActive_PostVisibilityInRepositoryShouldBeUpdatedAndPostShouldBeLoadedToCacheIfNotPresent() {

        // given
        int postId = 1;
        var visibility = Visibility.ACTIVE;

        when(repository.changeVisibilityById(postId, visibility)).thenReturn(1);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        var authorities = Set.of(new SimpleGrantedAuthority(Role.ADMIN.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.changeVisibility(postId, visibility);

        // then
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cache, never()).deletePostFromCache(postId);
        verify(cacheHandler).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void changeVisibility_PostDoesNotExistAndNewVisibilityIsDeleted_PostNotFoundExceptionShouldBeThrown() {

        // given
        int postId = 1;
        var visibility = Visibility.DELETED;

        when(repository.changeVisibilityById(postId, visibility)).thenReturn(0);

        // when
        assertThrows(PostNotFoundException.class, () -> postService.changeVisibility(postId, visibility));

        // then
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cache, never()).deletePostFromCache(postId);
        verify(cacheHandler, never()).loadPostDataToCacheIfPossible(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsDeletedAndOldVisibilityIsActiveAndUserRoleIsAdmin_VisibilityShouldUpdatedAndPostShouldBeDeletedFromCache() {

        // given
        var postId = 1;
        var visibility = Visibility.DELETED;
        var postDto = new PostDto(null, null, null, null, null, null, Visibility.ACTIVE);
        var post = new PostWithImageDataDto(postDto, null);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);

        var authorities = Set.of(new SimpleGrantedAuthority(Role.ADMIN.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        Optional<PostWithImageDataDto> result = Optional.of(post);
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        postService.changeOwnedPostVisibility(postId, visibility);

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cacheHandler, never()).loadToCacheIfPossible(same(result.get()));
        verify(cache).deletePostFromCache(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsActiveAndOldVisibilityIsDeletedAndUserRoleIsAdmin_VisibilityShouldUpdatedAndPostShouldBeDeletedFromCache() {

        // given
        var postId = 1;
        var visibility = Visibility.ACTIVE;
        var postDto = new PostDto(null, null, null, null, null, null, Visibility.ACTIVE);
        var post = new PostWithImageDataDto(postDto, null);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);

        var authorities = Set.of(new SimpleGrantedAuthority(Role.ADMIN.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        Optional<PostWithImageDataDto> result = Optional.of(post);
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        postService.changeOwnedPostVisibility(postId, visibility);

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cacheHandler).loadToCacheIfPossible(same(result.get()));
        verify(cache, never()).deletePostFromCache(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsActiveAndOldVisibilityIsDeletedAndUserRoleIsUser_ShouldThrowInsufficientRoleException() {

        // given
        var postId = 1;
        var visibility = Visibility.ACTIVE;
        var postDto = new PostDto(
                null,
                null,
                null,
                null,
                null,
                null,
                Visibility.DELETED
        );
        var post = new PostWithImageDataDto(postDto, null);

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);

        var authorities = Set.of(new SimpleGrantedAuthority(Role.USER.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        Optional<PostWithImageDataDto> result = Optional.of(post);
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        assertThrows(InsufficientRoleException.class, () -> postService.changeOwnedPostVisibility(postId, visibility));

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository, never()).changeVisibilityById(postId, visibility);
        verify(cacheHandler, never()).loadToCacheIfPossible(same(result.get()));
        verify(cache, never()).deletePostFromCache(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsActiveAndOldVisibilityIsDeletedAndUserRoleIsUser_PostNotFoundOrClientIsNotOwnerException() {

        // given
        var postId = 1;
        var visibility = Visibility.ACTIVE;

        // mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        Optional<PostWithImageDataDto> result = Optional.empty();
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        assertThrows(PostNotFoundOrClientIsNotOwnerException.class, () -> postService.changeOwnedPostVisibility(postId, visibility));

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository, never()).changeVisibilityById(postId, visibility);
        verify(cacheHandler, never()).loadToCacheIfPossible(any(PostWithImageDataDto.class));
        verify(cache, never()).deletePostFromCache(postId);
    }
}