package kt.aivle.shorts.application.port.out.s3;

public record UploadedObjectResponse(
        String url,
        String presignedUrl,
        String s3Key,
        String originalName,
        String contentType) {
}