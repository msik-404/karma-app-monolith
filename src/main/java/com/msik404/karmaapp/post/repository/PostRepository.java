package com.msik404.karmaapp.post.repository;

import java.util.Optional;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.post.PostImageDataProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<PostImageDataProjection> findImageById(long postId);

}
