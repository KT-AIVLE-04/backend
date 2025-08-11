package kt.aivle.shorts.application.port.out.s3;

public record UploadImageResponse(
        String url,
        String presignedUrl,
        String s3Key,
        String originalName,
        String contentType) {
}