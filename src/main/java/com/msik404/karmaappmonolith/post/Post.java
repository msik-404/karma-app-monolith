package com.msik404.karmaappmonolith.post;


import java.util.Set;

import com.msik404.karmaappmonolith.karma.KarmaScore;
import com.msik404.karmaappmonolith.post.comparator.ComparablePost;
import com.msik404.karmaappmonolith.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Entity
// I chose this index because of required sorting in this method of pagination.
// I am not sure if this index is perfect, because karmaScore will often change and also visibility is not indexed
// and is used in where clause
@Table(name = "posts", indexes = @Index(name = "posts_keyset_pagination", columnList = "karmaScore DESC, id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post implements ComparablePost {

    @Id
    @GeneratedValue
    private Long id;

    private String headline;

    private String text;

    // This will be updated in transaction with KarmaScore, so that these values will be kept in sync.
    // Doing so we won't have to scan KarmaScore table each time we need to get the score of a post.
    private Long karmaScore;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post")
    private Set<KarmaScore> karmaScores;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] imageData;

    public Post(@Nullable String headline, @Nullable String text, @NonNull User user, @Nullable byte[] imageData) {

        this.headline = headline;
        this.text = text;
        this.karmaScore = 0L;
        this.visibility = Visibility.ACTIVE;
        this.user = user;
        this.imageData = imageData;
    }

}
