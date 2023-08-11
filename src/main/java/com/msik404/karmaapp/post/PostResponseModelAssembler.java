package com.msik404.karmaapp.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.msik404.karmaapp.post.dto.PostJoinedDto;
import com.msik404.karmaapp.post.dto.PostResponse;
import com.msik404.karmaapp.user.Role;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PostResponseModelAssembler implements RepresentationModelAssembler<PostJoinedDto, EntityModel<PostResponse>> {

    private PostResponse postResponseBuilder(@NonNull PostJoinedDto postJoinedDto) {

        return PostResponse.builder()
                .id(postJoinedDto.getId())
                .username(postJoinedDto.getUsername())
                .headline(postJoinedDto.getHeadline())
                .text(postJoinedDto.getText())
                .karmaScore(postJoinedDto.getKarmaScore())
                .visibility(postJoinedDto.getVisibility())
                .build();
    }

    @Override
    public EntityModel<PostResponse> toModel(PostJoinedDto postJoinedDto) {

        final Long postId = postJoinedDto.getId();
        final var postResponse = postResponseBuilder(postJoinedDto);

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(PostController.class).findImageById(postId)).withSelfRel());

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return EntityModel.of(postResponse, links.toArray(new Link[0]));
        }

        final var userId = (long) authentication.getPrincipal();
        Set<String> grantedAuthorities = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Boolean wasRatedByAuthenticatedUserPositively = postJoinedDto.getWasRatedByAuthenticatedUserPositively();
        if (wasRatedByAuthenticatedUserPositively != null) {
            if (wasRatedByAuthenticatedUserPositively.equals(true)) {
                links.add(linkTo(methodOn(PostController.class).rate(postId, false)).withSelfRel());
            } else {
                links.add(linkTo(methodOn(PostController.class).rate(postId, true)).withSelfRel());
            }
            links.add(linkTo(methodOn(PostController.class).unrate(postId)).withSelfRel());
        } else {
            links.add(linkTo(methodOn(PostController.class).rate(postId, false)).withSelfRel());
            links.add(linkTo(methodOn(PostController.class).rate(postId, true)).withSelfRel());
        }

        PostVisibility visibility = postJoinedDto.getVisibility();
        if (postJoinedDto.getUserId().equals(userId)) {
            if (visibility == PostVisibility.HIDDEN) {
                links.add(linkTo(methodOn(PostController.class).unhideByUser(postId)).withSelfRel());
            } else {
                links.add(linkTo(methodOn(PostController.class).hideByUser(postId)).withSelfRel());
            }
            if (visibility != PostVisibility.DELETED) {
                links.add(linkTo(methodOn(PostController.class).deleteByUser(postId)).withSelfRel());
            }
        } else {
            if (grantedAuthorities.contains(Role.ADMIN.toString())) {
                if (visibility != PostVisibility.ACTIVE) {
                    links.add(linkTo(methodOn(PostController.class).activateByAdmin(postId)).withSelfRel());
                }
                if (visibility != PostVisibility.DELETED) {
                    links.add(linkTo(methodOn(PostController.class).deleteByAdmin(postId)).withSelfRel());
                }
            }
            if (grantedAuthorities.contains(Role.MOD.toString()) ||
                    grantedAuthorities.contains(Role.ADMIN.toString())) {
                if (visibility != PostVisibility.HIDDEN) {
                    links.add(linkTo(methodOn(PostController.class).hideByMod(postId)).withSelfRel());
                }
            }
        }

        return EntityModel.of(postResponse, links.toArray(new Link[0]));
    }
}
