package kt.aivle.snspost.domain.model;

public enum SnsPlatform {
    INSTAGRAM("instagram"),
    FACEBOOK("facebook"),
    YOUTUBE("youtube");

    private final String value;

    SnsPlatform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SnsPlatform fromString(String text) {
        for (SnsPlatform platform : SnsPlatform.values()) {
            if (platform.value.equalsIgnoreCase(text)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown SNS platform: " + text);
    }
} 