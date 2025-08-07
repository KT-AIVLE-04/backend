package kt.aivle.sns.domain.model;

public class PostUploadRequest {
    private SnsType snsType;
    private String title;
    private String description;
    private String contentPath;
    private String[] tags;
    private Object detail; // SNS별 세부정보

    public PostUploadRequest() {}

    public void setSnsType(SnsType snsType) {
        this.snsType = snsType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public SnsType getSnsType() {
        return snsType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getContentPath() {
        return contentPath;
    }

    public String[] getTags() {
        return tags;
    }

    public Object getDetail() {
        return detail;
    }
}
