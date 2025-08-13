package kt.aivle.snspost.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Post extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long storeId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SnsPlatform snsPlatform;

    @Column(nullable = false)
    private String businessType;

    @Column(columnDefinition = "TEXT")
    private String contentData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Column(columnDefinition = "TEXT")
    private String userKeywords;

    @Column(nullable = false)
    private boolean isPublic = true;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Builder
    public Post(Long userId, Long storeId, String title, String content, String location,
                SnsPlatform snsPlatform, String businessType, String contentData,
                ContentType contentType, String userKeywords, boolean isPublic) {
        this.userId = userId;
        this.storeId = storeId;
        this.title = title;
        this.content = content;
        this.location = location;
        this.snsPlatform = snsPlatform;
        this.businessType = businessType;
        this.contentData = contentData;
        this.contentType = contentType;
        this.userKeywords = userKeywords;
        this.isPublic = isPublic;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updateVisibility(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void addHashtag(PostHashtag postHashtag) {
        this.postHashtags.add(postHashtag);
        postHashtag.setPost(this);
    }

    public void removeHashtag(PostHashtag postHashtag) {
        this.postHashtags.remove(postHashtag);
        postHashtag.setPost(null);
    }
} 