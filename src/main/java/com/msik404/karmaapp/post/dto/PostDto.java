package com.msik404.karmaapp.post.dto;

import com.msik404.karmaapp.post.Visibility;
import com.msik404.karmaapp.post.comparator.ComparablePost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostDto implements ComparablePost {

    private Long id;

    private Long userId;

    private String username;

    private String headline;

    private String text;

    private Long karmaScore;

    private Visibility visibility;

}
