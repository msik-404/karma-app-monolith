package com.msik404.karmaappmonolith.karma;

import com.msik404.karmaappmonolith.post.Post;
import com.msik404.karmaappmonolith.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Table(name = "karma_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KarmaScore {

    @EmbeddedId
    private KarmaKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    private boolean isPositive;

    public KarmaScore(@NonNull User user, @NonNull Post post, boolean isPositive) {

        this.id = new KarmaKey(user.getId(), post.getId());
        this.user = user;
        this.post = post;
        this.isPositive = isPositive;
    }

}
