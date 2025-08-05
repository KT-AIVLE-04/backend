package kt.aivle.content.dto.common;

public enum SortOrder {
    ASC("asc"),
    DESC("desc");

    private final String value;

    SortOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SortOrder fromString(String value) {
        for (SortOrder order : SortOrder.values()) {
            if (order.value.equalsIgnoreCase(value)) {
                return order;
            }
        }
        return DESC; // 기본값
    }
}