package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;

import com.msik404.karmaapp.post.dto.PostDto;
import com.msik404.karmaapp.post.dto.PostResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PostResponseModelAssembler implements RepresentationModelAssembler<PostDto, EntityModel<PostResponse>> {

    @NonNull
    private PostResponse postResponseBuilder(@NonNull PostDto postDto) {

        return new PostResponse(
                postDto.getId(),
                postDto.getUsername(),
                postDto.getHeadline(),
                postDto.getText(),
                postDto.getKarmaScore(),
                postDto.getVisibility()
        );
    }

    @Override
    public EntityModel<PostResponse> toModel(@NonNull PostDto postDto) {

        final Long postId = postDto.getId();
        final var postResponse = postResponseBuilder(postDto);

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
