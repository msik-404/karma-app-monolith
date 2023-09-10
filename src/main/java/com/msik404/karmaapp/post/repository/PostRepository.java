package com.msik404.karmaapp.post.repository;

import java.util.Optional;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostImageDataProjection;
import com.msik404.karmaapp.post.dto.PostDtoWithImageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<PostImageDataProjection> findImageById(long postId);

    @Query("SELECT NEW com.msik404.karmaapp.post.dto.PostDtoWithImageData(p.id, p.user.id, u.username, p.headline, p.text, p.karmaScore, p.visibility, p.imageData) " +
            "FROM Post p JOIN p.user u WHERE p.id = :postId")
    Optional<PostDtoWithImageData> findPostDtoWithImageDataById(@Param("postId") Long postId);

}
