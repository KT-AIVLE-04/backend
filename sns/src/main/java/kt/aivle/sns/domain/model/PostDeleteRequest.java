package kt.aivle.sns.domain.model;

public class PostDeleteRequest {
    private String postId;

    public PostDeleteRequest() {};

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
