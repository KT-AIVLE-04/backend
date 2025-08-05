package kt.aivle.content.dto.common;

public enum ContentType {
    VIDEO("영상"),
    IMAGE("이미지"),
    ALL("전체");

    private final String description;

    ContentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}