package kt.aivle.snspost.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "hashtags")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Hashtag extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int postCount = 0;

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Builder
    public Hashtag(String name) {
        this.name = name;
    }

    public void incrementPostCount() {
        this.postCount++;
    }

    public void decrementPostCount() {
        if (this.postCount > 0) {
            this.postCount--;
        }
    }

    public void addPostHashtag(PostHashtag postHashtag) {
        this.postHashtags.add(postHashtag);
        postHashtag.setHashtag(this);
    }

    public void removePostHashtag(PostHashtag postHashtag) {
        this.postHashtags.remove(postHashtag);
        postHashtag.setHashtag(null);
    }
} 