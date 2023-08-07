package com.msik404.karmaapp.post;


import java.util.Set;

import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
// visibility is the most restrictive, karmaScore and id is the same order as order in query
@Table(name = "posts", indexes = @Index(name = "posts_keyset_pagination", columnList = "visibility, karmaScore DESC, id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    private String text;

    // This will be updated in transaction with KarmaScore, so that these values will be kept in sync.
    // Doing so we won't have to scan KarmaScore table each time we need to get the score of a post.
    private Long karmaScore;

    @Enumerated(EnumType.STRING)
    private PostVisibility visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post")
    private Set<KarmaScore> karmaScores;

}
