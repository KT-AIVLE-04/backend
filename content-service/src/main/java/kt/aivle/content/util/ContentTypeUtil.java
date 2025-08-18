package kt.aivle.content.util;

public final class ContentTypeUtil {
    private ContentTypeUtil() {
    }

    public static boolean isImage(String ct) {
        return ct != null && ct.startsWith("image");
    }

    public static String extFromContentType(String ct) {
        if (ct == null) return ".bin";
        String c = ct.toLowerCase();
        if (c.contains("mp4")) return ".mp4";
        if (c.contains("png")) return ".png";
        if (c.contains("jpeg") || c.contains("jpg")) return ".jpg";
        return ".bin";
    }

    public static String guessFromKey(String key) {
        String k = key.toLowerCase();
        if (k.endsWith(".mp4")) return "video/mp4";
        if (k.endsWith(".png")) return "image/png";
        if (k.endsWith(".jpg") || k.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    public static String fileName(String key) {
        int idx = key.lastIndexOf('/');
        return (idx >= 0) ? key.substring(idx + 1) : key;
    }
}

