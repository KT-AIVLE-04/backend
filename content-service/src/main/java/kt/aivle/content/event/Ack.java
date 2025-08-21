package kt.aivle.content.event;

public record Ack(boolean ok, String message) {
    public static Ack success() {
        return new Ack(true, null);
    }

    public static Ack fail(String msg) {
        return new Ack(false, msg);
    }
}
