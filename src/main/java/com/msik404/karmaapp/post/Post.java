package com.msik404.karmaapp.post;


import com.msik404.karmaapp.karma.KarmaScore;
import com.msik404.karmaapp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity  @Table(name = "posts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {

    @Id @GeneratedValue
    private Long id;

    private String text;

    @ManyToOne @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "post")
    private Set<KarmaScore> karmaScores;

}
