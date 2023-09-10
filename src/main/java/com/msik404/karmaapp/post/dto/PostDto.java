package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.ComparablePost;
import com.msik404.karmaapp.post.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class PostDto implements ComparablePost {

    private Long id;

    private Long userId;

    private String username;

    private String headline;

    private String text;

    private Long karmaScore;

    private Visibility visibility;

}
