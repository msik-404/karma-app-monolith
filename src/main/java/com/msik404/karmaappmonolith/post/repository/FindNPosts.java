package com.msik404.karmaappmonolith.post.repository;

import com.msik404.karmaappmonolith.post.dto.PostDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

public class FindNPosts extends FindNPostPaginated<PostDto> {

    public FindNPosts(EntityManager entityManager, CriteriaBuilder cb) {
        super(entityManager, cb, PostDto.class);
    }

    @Override
    void selectMethod(CriteriaBuilder cb) {

        criteriaQuery.select(
                cb.construct(
                        PostDto.class,
                        postRoot.get("id"),
                        postRoot.get("user").get("id"),
                        userJoin.get("username"),
                        postRoot.get("headline"),
                        postRoot.get("text"),
                        postRoot.get("karmaScore"),
                        postRoot.get("visibility")
                )
        );
    }

}
