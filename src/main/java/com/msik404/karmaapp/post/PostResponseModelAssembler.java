package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;

import com.msik404.karmaapp.post.dto.PostJoined;
import com.msik404.karmaapp.post.dto.PostResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PostResponseModelAssembler implements RepresentationModelAssembler<PostJoined, EntityModel<PostResponse>> {

    private PostResponse postResponseBuilder(@NonNull PostJoined postJoined) {

        return PostResponse.builder()
                .id(postJoined.getId())
                .username(postJoined.getUsername())
                .headline(postJoined.getHeadline())
                .text(postJoined.getText())
                .karmaScore(postJoined.getKarmaScore())
                .visibility(postJoined.getVisibility())
                .build();
    }

    @Override
    public EntityModel<PostResponse> toModel(@NonNull PostJoined postJoined) {

        final Long postId = postJoined.getId();
        final var postResponse = postResponseBuilder(postJoined);

        List<Link> links = new ArrayList<>();

        links.add(linkTo(methodOn(PostController.class).findImageById(postId)).withSelfRel());

        links.add(linkTo(methodOn(PostController.class).rate(postId, false)).withSelfRel());
        links.add(linkTo(methodOn(PostController.class).rate(postId, true)).withSelfRel());
        links.add(linkTo(methodOn(PostController.class).unrate(postId)).withSelfRel());

        links.add(linkTo(methodOn(PostController.class).hideByUser(postId)).withSelfRel());
        links.add(linkTo(methodOn(PostController.class).unhideByUser(postId)).withSelfRel());
        links.add(linkTo(methodOn(PostController.class).deleteByUser(postId)).withSelfRel());

        links.add(linkTo(methodOn(PostController.class).hideByMod(postId)).withSelfRel());

        links.add(linkTo(methodOn(PostController.class).activateByAdmin(postId)).withSelfRel());
        links.add(linkTo(methodOn(PostController.class).deleteByAdmin(postId)).withSelfRel());

        return EntityModel.of(postResponse, links.toArray(new Link[0]));
    }
}
