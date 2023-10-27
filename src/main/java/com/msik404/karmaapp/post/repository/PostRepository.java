package com.msik404.karmaapp.post.repository;

import java.util.Optional;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.dto.ImageOnlyDto;
import com.msik404.karmaapp.post.dto.PostDtoWithImageData;
import com.msik404.karmaapp.post.dto.VisibilityOnlyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<ImageOnlyDto> findImageById(long postId);

    Optional<VisibilityOnlyDto> findVisibilityById(long postId);

    // todo: fix this
    @Query("SELECT NEW com.msik404.karmaapp.post.dto.PostDtoWithImageData(p.id, p.user.id, u.username, p.headline, p.text, p.karmaScore, p.visibility, p.imageData) " +
            "FROM Post p JOIN p.user u WHERE p.id = :postId")
    Optional<PostDtoWithImageData> findPostDtoWithImageDataById(@Param("postId") long postId);

    @Query("SELECT NEW com.msik404.karmaapp.post.dto.PostDtoWithImageData(p.id, p.user.id, u.username, p.headline, p.text, p.karmaScore, p.visibility, p.imageData) " +
            "FROM Post p JOIN p.user u WHERE p.id = :postId AND p.user.id = :userId")
    Optional<PostDtoWithImageData> findPostDtoWithImageDataByIdAndUserId(@Param("postId") long postId, @Param("userId") long userId);

}
