package kt.aivle.shorts.adapter.out.s3.dto;

public record UploadedImageInfo(String url, String presignedUrl, String s3Key, String originalName,
                                String contentType) {
    public static UploadedImageInfo from(String url, String presignedUrl, String s3Key, String originalName, String contentType) {
        return new UploadedImageInfo(url, presignedUrl, s3Key, originalName, contentType);
    }
}