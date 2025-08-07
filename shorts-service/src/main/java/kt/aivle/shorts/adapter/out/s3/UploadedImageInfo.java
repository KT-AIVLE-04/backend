package kt.aivle.shorts.adapter.out.s3;

public record UploadedImageInfo(String url, String s3Key, String originalName, String contentType) {
    public static UploadedImageInfo from(String url, String s3Key, String originalName, String contentType) {
        return new UploadedImageInfo(url, s3Key, originalName, contentType);
    }
}
