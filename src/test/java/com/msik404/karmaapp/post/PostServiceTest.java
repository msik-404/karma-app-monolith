package com.msik404.karmaapp.post;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

import com.msik404.karmaapp.TestingImageDataCreator;
import com.msik404.karmaapp.karma.KarmaKey;
import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.karma.KarmaScoreService;
import com.msik404.karmaapp.karma.exception.KarmaScoreAlreadyExistsException;
import com.msik404.karmaapp.karma.exception.KarmaScoreNotFoundException;
import com.msik404.karmaapp.pagin.Pagination;
import com.msik404.karmaapp.post.cache.PostRedisCache;
import com.msik404.karmaapp.post.cache.PostRedisCacheHandlerService;
import com.msik404.karmaapp.post.dto.PostCreationRequest;
import com.msik404.karmaapp.post.dto.PostDtoWithImageData;
import com.msik404.karmaapp.post.exception.FileProcessingException;
import com.msik404.karmaapp.post.exception.ImageNotFoundException;
import com.msik404.karmaapp.post.exception.PostNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @Mock
    private UserRepository userRepository;

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
        final Pagination pagination = null;
        final String username = null;

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        postService.findPaginatedPosts(size, visibilities, pagination, username);

        // then
        verify(cacheHandler).findTopNHandler(size, visibilities);
        verify(repository, never()).findNextNPostsWithUsername(size, visibilities, pagination, username);
        verify(cacheHandler, never()).findNextNHandler(size, visibilities, pagination);
        verify(repository, never()).findTopNPostsWithUsername(size, visibilities, username);
    }

    @Test
    void findPaginatedPosts_PaginationIsNonNullAndUsernameIsNonNull_RepositoryFindNextNPostsWithUsernameCalled() {

        // given
        final var pagination = new Pagination(0, 0);
        final String username = "username";

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

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
        final var pagination = new Pagination(0, 0);
        final String username = null;

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

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
        final Pagination pagination = null;
        final String username = "username";

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // when
        postService.findPaginatedPosts(size, visibilities, pagination, username);

        // then
        verify(cacheHandler, never()).findTopNHandler(size, visibilities);
        verify(repository, never()).findNextNPostsWithUsername(size, visibilities, pagination, username);
        verify(cacheHandler, never()).findNextNHandler(size, visibilities, pagination);
        verify(repository).findTopNPostsWithUsername(size, visibilities, username);
    }

    @Test
    void findPaginatedOwnedPosts_PaginationIsNull_FindTopNWithUserIdCalled() {

        // given
        final Pagination pagination = null;

        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedOwnedPosts(size, visibilities, pagination);

        // then
        verify(repository).findTopNWithUserId(size, visibilities, userId);
        verify(repository, never()).findNextNWithUserId(size, visibilities, userId, pagination);
    }

    @Test
    void findPaginatedOwnedPosts_PaginationIsNonNull_FindNextNWithUserIdCalled() {

        // given
        final var pagination = new Pagination(0, 0);

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
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
        final Pagination pagination = null;
        final String username = null;

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedPostRatings(size, visibilities, pagination, username);

        // then
        verify(repository).findTopNRatings(size, visibilities, userId);
        verify(repository, never()).findNextNRatingsWithUsername(size, visibilities, userId, pagination, username);
        verify(repository, never()).findNextNRatings(size, visibilities, userId, pagination);
        verify(repository, never()).findTopNRatingsWithUsername(size, visibilities, userId, username);
    }

    @Test
    void findPaginatedPostRatings_PaginationIsNonNullAndUsernameIsNonNull_RepositoryFindNextNRatingsWithUsernameCalled() {

        // given
        final var pagination = new Pagination(0, 0);
        final String username = "username";

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
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
        final var pagination = new Pagination(0, 0);
        final String username = null;

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
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
        final Pagination pagination = null;
        final String username = "username";

        // these values won't influence results of these test
        final int size = 10;
        final List<Visibility> visibilities = List.of(Visibility.ACTIVE);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // when
        postService.findPaginatedPostRatings(size, visibilities, pagination, username);

        // then
        verify(repository, never()).findTopNRatings(size, visibilities, userId);
        verify(repository, never()).findNextNRatingsWithUsername(size, visibilities, userId, pagination, username);
        verify(repository, never()).findNextNRatings(size, visibilities, userId, pagination);
        verify(repository).findTopNRatingsWithUsername(size, visibilities, userId, username);
    }

    @Test
    void findImageByPostId_CacheHasRequestedImage_RepositoryShouldNotBeCalled() {

        // given
        final long postId = 10;
        final byte[] data = new byte[0];

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
        final long postId = 10;

        when(cache.getCachedImage(postId)).thenReturn(Optional.empty());
        when(repository.findImageById(postId)).thenReturn(Optional.of(() -> new byte[1]));

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
        final long postId = 10;

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
        final long postId = 10;

        when(cache.getCachedImage(postId)).thenReturn(Optional.empty());
        when(repository.findImageById(postId)).thenReturn(Optional.of(() -> new byte[0]));

        // when
        assertThrows(ImageNotFoundException.class, () -> postService.findImageByPostId(postId));

        // then
        verify(cache).getCachedImage(postId);
        verify(repository).findImageById(postId);
        verify(cache, never()).cacheImage(eq(postId), any(byte[].class));
    }

    @Test
    void create_ImageIsNotEmpty_PostWithImageShouldBeSavedAndImageShouldBeCached() {

        // given
        var request = new PostCreationRequest();
        request.setHeadline("headline");
        request.setText("text");

        // mock image
        final MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        final byte[] imageData = TestingImageDataCreator.getTestingImage();
        final var inputStream = new ByteArrayInputStream(imageData);

        try {
            when(image.getInputStream()).thenReturn(inputStream);
        } catch (IOException ex) {
            // this will never be called
        }

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        User groundTruthUser = User.builder().id(10L).build();

        Post groundTruthPost = Post.builder()
                .id(11L)
                .headline(request.getHeadline())
                .text(request.getText())
                .karmaScore(0L)
                .visibility(Visibility.ACTIVE)
                .user(groundTruthUser)
                .imageData(TestingImageDataCreator.jpegCompress(imageData))
                .build();

        when(userRepository.getReferenceById(userId)).thenReturn(groundTruthUser);

        // groundTruthPost already has id to mimic jpa id auto-generation
        when(repository.save(any(Post.class))).thenReturn(groundTruthPost);

        var postCaptor = ArgumentCaptor.forClass(Post.class);

        // when
        postService.create(request, image);

        // then
        verify(repository).save(postCaptor.capture());
        verify(cache).cacheImage(groundTruthPost.getId(), groundTruthPost.getImageData());

        Post postCaptorValue = postCaptor.getValue();
        // post before repository save should have null id
        groundTruthPost.setId(null);
        assertEquals(groundTruthPost, postCaptorValue);
    }

    @Test
    void create_ImageIsNotEmptyButInvalid_FileProcessingExceptionShouldBeThrown() {

        // given
        final var request = new PostCreationRequest();

        // mock image
        final MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        final byte[] imageData = "sampleImageData".getBytes();
        final var inputStream = new ByteArrayInputStream(imageData);

        try {
            when(image.getInputStream()).thenReturn(inputStream);
        } catch (IOException ex) {
            // this will never be called
        }

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        User groundTruthUser = User.builder().id(10L).build();

        Post groundTruthPost = Post.builder()
                .id(11L)
                .headline(request.getHeadline())
                .text(request.getText())
                .karmaScore(0L)
                .visibility(Visibility.ACTIVE)
                .user(groundTruthUser)
                .build();

        when(userRepository.getReferenceById(userId)).thenReturn(groundTruthUser);

        // when
        assertThrows(FileProcessingException.class, () -> postService.create(request, image));

        // then
        verify(repository, never()).save(any(Post.class));
        verify(cache, never()).cacheImage(groundTruthPost.getId(), groundTruthPost.getImageData());
    }

    @Test
    void create_ImageIsEmpty_PostWithoutImageShouldBeSavedAndImageShouldNotBeCached() {

        // given
        final var request = new PostCreationRequest();

        // mock image
        final MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        User groundTruthUser = User.builder().id(10L).build();

        Post groundTruthPost = Post.builder()
                .id(11L)
                .headline(request.getHeadline())
                .text(request.getText())
                .karmaScore(0L)
                .visibility(Visibility.ACTIVE)
                .user(groundTruthUser)
                .build();

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
        final long postId = 1;
        final boolean isNewRatingPositive = true;
        final boolean isOldRatingPositive = false;
        final long delta = 2;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = KarmaScore.builder().id(karmaKey).isPositive(isOldRatingPositive).build();

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        final int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        when(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId)).thenReturn(true);

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService, never()).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsFalseAndOldRatingIsPositiveAndWasNotCached_DeltaIsMinusTwoAndShouldTryToLoadToCache() {

        // given
        final long postId = 1;
        final boolean isNewRatingPositive = false;
        final boolean isOldRatingPositive = true;
        final long delta = -2;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = KarmaScore.builder().id(karmaKey).isPositive(isOldRatingPositive).build();

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        final int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        when(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId)).thenReturn(true);

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService, never()).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNullAndWasNotCached_DeltaIsOneAndShouldTryToLoadToCache() {

        // given
        final long postId = 1;
        final boolean isNewRatingPositive = true;
        final long delta = 1;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        final int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        when(cacheHandler.loadPostDataToCacheIfKarmaScoreIsHighEnough(postId)).thenReturn(true);

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNullAndWasNotCachedAndPostIsNotFound_ShouldThrowPostNotFoundException() {

        // given
        final long postId = 1;
        final boolean isNewRatingPositive = true;
        final long delta = 1;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        final int rowsAffected = 0;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        // when
        assertThrows(PostNotFoundException.class, () -> postService.rate(postId, isNewRatingPositive));

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache, never()).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler, never()).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsPositiveAndWasNotCached_ShouldThrowKarmaScoreAlreadyExistsException() {

        // given
        final long postId = 1;
        final boolean isNewRatingPositive = true;
        final boolean isOldRatingPositive = true;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = KarmaScore.builder().id(karmaKey).isPositive(isOldRatingPositive).build();

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        // when
        assertThrows(KarmaScoreAlreadyExistsException.class, () -> postService.rate(postId, isNewRatingPositive));

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService, never()).create(userId, postId, isNewRatingPositive);
    }

    @Test
    void rate_IsNewRatingPositiveIsTrueAndOldRatingIsNullAndIsCached_DeltaIsOneAndShouldNotTryToLoadToCache() {

        // given
        final long postId = 1;
        final boolean isNewRatingPositive = true;
        final long delta = 1;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        final int rowsAffected = 1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(rowsAffected);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.of(1));

        // when
        postService.rate(postId, isNewRatingPositive);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(karmaScoreService).create(userId, postId, isNewRatingPositive);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler, never()).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void unrate_PostExistsAndWasRatedPositivelyAndIsCached_KarmaScoreShouldBeFoundAndDeletedPostKarmaScoreShouldBeUpdatedAndCachedKarmaScoreShouldBeUpdatedAndShouldNotTryToLoadToCache() {

        // given
        final long postId = 1;
        final boolean isPositive = true;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = KarmaScore.builder().id(karmaKey).isPositive(isPositive).build();

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        final long delta = -1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(1);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.of(1));

        // when
        postService.unrate(postId);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(karmaScoreService).deleteById(karmaKey);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler, never()).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void unrate_PostExistsAndWasRatedPositivelyAndIsNotCached_KarmaScoreShouldBeFoundAndDeletedPostKarmaScoreShouldBeUpdatedAndShouldTryToLoadToCache() {

        // given
        final long postId = 1;
        final boolean isPositive = true;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = KarmaScore.builder().id(karmaKey).isPositive(isPositive).build();

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        final long delta = -1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(1);

        when(cache.updateKarmaScoreIfPresent(postId, delta)).thenReturn(OptionalDouble.empty());

        // when
        postService.unrate(postId);

        // then
        verify(karmaScoreService).findById(karmaKey);
        verify(repository).addKarmaScoreToPost(postId, delta);
        verify(karmaScoreService).deleteById(karmaKey);
        verify(cache).updateKarmaScoreIfPresent(postId, (double) delta);
        verify(cacheHandler).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void unrate_PostWasNotRated_KarmaScoreNotFoundExceptionShouldBeThrown() {

        // given
        final long postId = 1;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);

        when(karmaScoreService.findById(karmaKey)).thenThrow(KarmaScoreNotFoundException.class);

        // then                                               // when
        assertThrows(KarmaScoreNotFoundException.class, () -> postService.unrate(postId));
    }

    @Test
    void unrate_PostWasRatedPositivelyButPostWasNotFound_PostNotFoundExceptionShouldBeThrown() {

        // given
        final long postId = 1;
        final boolean isPositive = true;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        final var karmaKey = new KarmaKey(userId, postId);
        final var karmaScore = KarmaScore.builder().id(karmaKey).isPositive(isPositive).build();

        when(karmaScoreService.findById(karmaKey)).thenReturn(karmaScore);

        final long delta = -1;
        when(repository.addKarmaScoreToPost(postId, delta)).thenReturn(0);

        // then                                         // when
        assertThrows(PostNotFoundException.class, () -> postService.unrate(postId));
    }

    @Test
    void changeVisibility_PostExistsAndNewVisibilityIsHidden_PostVisibilityInRepositoryShouldBeUpdatedAndPostShouldBeDeletedFromCacheIfPresent() {

        // given
        final int postId = 1;
        final var visibility = Visibility.HIDDEN;

        when(repository.changeVisibilityById(postId, visibility)).thenReturn(1);
        when(cache.deletePostFromCache(postId)).thenReturn(true);

        // when
        postService.changeVisibility(postId, visibility);

        // then
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cache).deletePostFromCache(postId);
        verify(cacheHandler, never()).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void changeVisibility_PostExistsAndNewVisibilityIsActive_PostVisibilityInRepositoryShouldBeUpdatedAndPostShouldBeLoadedToCacheIfNotPresent() {

        // given
        final int postId = 1;
        final var visibility = Visibility.ACTIVE;

        when(repository.changeVisibilityById(postId, visibility)).thenReturn(1);

        // when
        postService.changeVisibility(postId, visibility);

        // then
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cache, never()).deletePostFromCache(postId);
        verify(cacheHandler).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void changeVisibility_PostDoesNotExistAndNewVisibilityIsDeleted_PostNotFoundExceptionShouldBeThrown() {

        // given
        final int postId = 1;
        final var visibility = Visibility.DELETED;

        when(repository.changeVisibilityById(postId, visibility)).thenReturn(0);

        // when
        assertThrows(PostNotFoundException.class, () -> postService.changeVisibility(postId, visibility));

        // then
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cache, never()).deletePostFromCache(postId);
        verify(cacheHandler, never()).loadPostDataToCacheIfKarmaScoreIsHighEnough(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsDeletedAndOldVisibilityIsActiveAndUserRoleIsAdmin_VisibilityShouldUpdatedAndPostShouldBeDeletedFromCache() {

        // given
        final var postId = 1;
        final var visibility = Visibility.DELETED;
        final var post = PostDtoWithImageData.builder().visibility(Visibility.ACTIVE).build();

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);

        final var authorities = Set.of(new SimpleGrantedAuthority(Role.ADMIN.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);
        
        Optional<PostDtoWithImageData> result = Optional.of(post);
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        postService.changeOwnedPostVisibility(postId, visibility);

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cacheHandler, never()).loadToCacheIfKarmaScoreIsHighEnough(same(result.get()));
        verify(cache).deletePostFromCache(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsActiveAndOldVisibilityIsDeletedAndUserRoleIsAdmin_VisibilityShouldUpdatedAndPostShouldBeDeletedFromCache() {

        // given
        final var postId = 1;
        final var visibility = Visibility.ACTIVE;
        final var post = PostDtoWithImageData.builder().visibility(Visibility.DELETED).build();

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);

        final var authorities = Set.of(new SimpleGrantedAuthority(Role.ADMIN.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        Optional<PostDtoWithImageData> result = Optional.of(post);
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        postService.changeOwnedPostVisibility(postId, visibility);

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository).changeVisibilityById(postId, visibility);
        verify(cacheHandler).loadToCacheIfKarmaScoreIsHighEnough(same(result.get()));
        verify(cache, never()).deletePostFromCache(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsActiveAndOldVisibilityIsDeletedAndUserRoleIsUser_ShouldThrowAccessDeniedException() {

        // given
        final var postId = 1;
        final var visibility = Visibility.ACTIVE;
        final var post = PostDtoWithImageData.builder().visibility(Visibility.DELETED).build();

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);

        final var authorities = Set.of(new SimpleGrantedAuthority(Role.USER.name()));
        doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        Optional<PostDtoWithImageData> result = Optional.of(post);
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        assertThrows(AccessDeniedException.class, () -> postService.changeOwnedPostVisibility(postId, visibility));

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository, never()).changeVisibilityById(postId, visibility);
        verify(cacheHandler, never()).loadToCacheIfKarmaScoreIsHighEnough(same(result.get()));
        verify(cache, never()).deletePostFromCache(postId);
    }

    @Test
    void changeOwnedPostVisibility_PostExistsAndNewVisibilityIsActiveAndOldVisibilityIsDeletedAndUserRoleIsUser_PostNotFoundException() {

        // given
        final var postId = 1;
        final var visibility = Visibility.ACTIVE;

        // mock authentication
        final Authentication authentication = mock(Authentication.class);
        final SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        final long userId = 10L;
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        Optional<PostDtoWithImageData> result = Optional.empty();
        when(repository.findPostDtoWithImageDataByIdAndUserId(postId, userId)).thenReturn(result);

        // when
        assertThrows(PostNotFoundException.class, () -> postService.changeOwnedPostVisibility(postId, visibility));

        // then
        verify(repository).findPostDtoWithImageDataByIdAndUserId(postId, userId);
        verify(repository, never()).changeVisibilityById(postId, visibility);
        verify(cacheHandler, never()).loadToCacheIfKarmaScoreIsHighEnough(any(PostDtoWithImageData.class));
        verify(cache, never()).deletePostFromCache(postId);
    }
}