package com.msik404.karmaapp.karma;

import com.msik404.karmaapp.post.Post;
import com.msik404.karmaapp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Table(name = "karma_scores")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KarmaScore {

    @EmbeddedId
    private KarmaKey id;

    @ManyToOne @MapsId("userId") @JoinColumn(name="user_id")
    private User user;

    @ManyToOne @MapsId("postId") @JoinColumn(name="post_id")
    private Post post;

    private Boolean isPositive;

}
