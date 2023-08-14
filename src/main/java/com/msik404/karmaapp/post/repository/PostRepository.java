package com.msik404.karmaapp.post.repository;

import com.msik404.karmaapp.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

}
