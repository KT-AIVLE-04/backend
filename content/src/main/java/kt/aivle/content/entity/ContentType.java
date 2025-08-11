package kt.aivle.content.entity;

public enum ContentType {
    IMAGE("이미지"),
    VIDEO("영상");

    private final String description;

    ContentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}