package kt.aivle.snspost.domain.model;

public enum ContentType {
    IMAGE("image"),
    VIDEO("video");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ContentType fromString(String text) {
        for (ContentType type : ContentType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + text);
    }
} 