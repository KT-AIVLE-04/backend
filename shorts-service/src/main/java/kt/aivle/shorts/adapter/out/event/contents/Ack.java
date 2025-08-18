package kt.aivle.shorts.adapter.out.event.contents;

public record Ack(boolean ok, String message) {
    public static Ack success() {
        return new Ack(true, null);
    }

    public static Ack fail(String msg) {
        return new Ack(false, msg);
    }
}
