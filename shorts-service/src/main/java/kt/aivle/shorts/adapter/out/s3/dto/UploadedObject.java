package kt.aivle.shorts.adapter.out.s3.dto;

public record UploadedObject(String url, String presignedUrl, String s3Key, String originalName,
                             String contentType) {
    public static UploadedObject from(String url, String presignedUrl, String s3Key, String originalName, String contentType) {
        return new UploadedObject(url, presignedUrl, s3Key, originalName, contentType);
    }
}