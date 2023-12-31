package com.msik404.karmaappmonolith.post.repository;

import java.util.Optional;

import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.post.dto.ImageOnlyDto;
import com.msik404.karmaappmonolith.post.dto.PostWithImageDataDto;
import com.msik404.karmaappmonolith.post.dto.VisibilityOnlyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<ImageOnlyDto> findImageById(long postId);

    Optional<VisibilityOnlyDto> findVisibilityById(long postId);

    @Query("SELECT NEW com.msik404.karmaappmonolith.post.dto.PostWithImageDataDto(p.id, p.user.id, u.username, p.headline, p.text, p.karmaScore, p.visibility, p.imageData) " +
            "FROM Post p JOIN p.user u WHERE p.id = :postId")
    Optional<PostWithImageDataDto> findPostDtoWithImageDataById(@Param("postId") long postId);

    @Query("SELECT NEW com.msik404.karmaappmonolith.post.dto.PostWithImageDataDto(p.id, p.user.id, u.username, p.headline, p.text, p.karmaScore, p.visibility, p.imageData) " +
            "FROM Post p JOIN p.user u WHERE p.id = :postId AND p.user.id = :userId")
    Optional<PostWithImageDataDto> findPostDtoWithImageDataByIdAndUserId(@Param("postId") long postId, @Param("userId") long userId);

}
