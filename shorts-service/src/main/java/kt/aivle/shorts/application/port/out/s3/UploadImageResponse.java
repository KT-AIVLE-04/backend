package kt.aivle.shorts.application.port.out.s3;

public record UploadImageResponse(
        String url,
        String s3Key,
        String originalName,
        String contentType) {
}