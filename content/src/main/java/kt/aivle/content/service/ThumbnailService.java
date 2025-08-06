package kt.aivle.content.service;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ThumbnailService {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    @Value("${media.processing.thumbnail.width}")
    private int thumbnailWidth;

    @Value("${media.processing.thumbnail.height}")
    private int thumbnailHeight;

    @Value("${media.processing.thumbnail.quality}")
    private float thumbnailQuality;

    /**
     * 이미지 썸네일 생성
     */
    public ThumbnailResult createImageThumbnail(InputStream imageStream) {
        try {
            BufferedImage originalImage = ImageIO.read(imageStream);
            if (originalImage == null) {
                return ThumbnailResult.failure("이미지를 읽을 수 없습니다.");
            }

            // 원본 이미지 정보 로깅
            logger.debug("원본 이미지 크기: {}x{}", originalImage.getWidth(), originalImage.getHeight());

            // 썸네일 생성
            BufferedImage thumbnail = createThumbnail(originalImage, thumbnailWidth, thumbnailHeight);

            // JPEG 바이트 배열로 변환
            byte[] thumbnailBytes = convertToJpegBytes(thumbnail, thumbnailQuality);

            ThumbnailInfo info = new ThumbnailInfo(
                    thumbnail.getWidth(),
                    thumbnail.getHeight(),
                    thumbnailBytes.length,
                    originalImage.getWidth(),
                    originalImage.getHeight()
            );

            logger.info("이미지 썸네일 생성 완료: {}x{} -> {}x{} (크기: {}KB)",
                    originalImage.getWidth(), originalImage.getHeight(),
                    thumbnail.getWidth(), thumbnail.getHeight(),
                    thumbnailBytes.length / 1024);

            return ThumbnailResult.success(thumbnailBytes, info);

        } catch (IOException e) {
            logger.error("이미지 썸네일 생성 실패", e);
            return ThumbnailResult.failure("썸네일 생성 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 비디오 썸네일 생성 (첫 번째 프레임)
     * 실제 구현에서는 FFmpeg 등을 사용해야 함
     */
    public ThumbnailResult createVideoThumbnail(String videoFilePath, int timeOffset) {
        try {
            // 실제 구현시에는 FFmpeg를 사용하여 비디오의 특정 시간 프레임을 추출
            // 여기서는 데모용으로 기본 이미지 생성
            BufferedImage placeholderImage = createVideoPlaceholder();

            byte[] thumbnailBytes = convertToJpegBytes(placeholderImage, thumbnailQuality);

            ThumbnailInfo info = new ThumbnailInfo(
                    placeholderImage.getWidth(),
                    placeholderImage.getHeight(),
                    thumbnailBytes.length,
                    1920, // 가정된 원본 비디오 해상도
                    1080
            );

            logger.info("비디오 썸네일 생성 완료 (플레이스홀더): {}x{}",
                    placeholderImage.getWidth(), placeholderImage.getHeight());

            return ThumbnailResult.success(thumbnailBytes, info);

        } catch (Exception e) {
            logger.error("비디오 썸네일 생성 실패", e);
            return ThumbnailResult.failure("비디오 썸네일 생성 실패: " + e.getMessage());
        }
    }

    /**
     * FFmpeg를 이용한 실제 비디오 썸네일 생성 (미구현 - 예시)
     */
    private ThumbnailResult createVideoThumbnailWithFFmpeg(String videoFilePath, int timeOffset) {
        // TODO: FFmpeg 연동 구현
        // 예시:
        // FFmpeg ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
        // FFprobe ffprobe = new FFprobe("/usr/local/bin/ffprobe");
        //
        // FFmpegBuilder builder = new FFmpegBuilder()
        //     .setInput(videoFilePath)
        //     .overrideOutputFiles(true)
        //     .addOutput("thumbnail.jpg")
        //     .setFrames(1)
        //     .setVideoFilter("select='gte(t," + timeOffset + ")'")
        //     .done();

        return ThumbnailResult.failure("FFmpeg 연동이 필요합니다.");
    }

    /**
     * 썸네일 크기 최적화
     */
    private BufferedImage createThumbnail(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // 원본 비율 계산
        double originalRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        int newWidth, newHeight;

        if (originalRatio > targetRatio) {
            // 가로가 더 긴 경우 - 세로에 맞춰서 크롭
            newWidth = targetWidth;
            newHeight = (int) (targetWidth / originalRatio);
        } else {
            // 세로가 더 긴 경우 - 가로에 맞춰서 크롭
            newWidth = (int) (targetHeight * originalRatio);
            newHeight = targetHeight;
        }

        // 고품질 리사이징 사용
        BufferedImage resizedImage = Scalr.resize(originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_EXACT,
                newWidth, newHeight,
                Scalr.OP_ANTIALIAS);

        // 목표 크기에 맞게 중앙 크롭
        if (resizedImage.getWidth() != targetWidth || resizedImage.getHeight() != targetHeight) {
            resizedImage = Scalr.crop(resizedImage,
                    (resizedImage.getWidth() - targetWidth) / 2,
                    (resizedImage.getHeight() - targetHeight) / 2,
                    targetWidth, targetHeight);
        }

        return resizedImage;
    }

    /**
     * 비디오 플레이스홀더 이미지 생성 (실제 FFmpeg 구현 전까지)
     */
    private BufferedImage createVideoPlaceholder() {
        BufferedImage placeholder = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);

        // 간단한 그라데이션 배경 생성
        for (int y = 0; y < thumbnailHeight; y++) {
            for (int x = 0; x < thumbnailWidth; x++) {
                int gray = (int) (128 + 64 * Math.sin(x * 0.1) * Math.cos(y * 0.1));
                int rgb = (gray << 16) | (gray << 8) | gray;
                placeholder.setRGB(x, y, rgb);
            }
        }

        return placeholder;
    }

    /**
     * BufferedImage를 JPEG 바이트 배열로 변환
     */
    private byte[] convertToJpegBytes(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // JPEG 품질 설정
        var writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("JPEG writer를 찾을 수 없습니다.");
        }

        var writer = writers.next();
        var writeParam = writer.getDefaultWriteParam();
        writeParam.setCompressionMode(writeParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(quality);

        writer.setOutput(ImageIO.createImageOutputStream(outputStream));
        writer.write(null, new javax.imageio.IIOImage(image, null, null), writeParam);

        writer.dispose();

        return outputStream.toByteArray();
    }

    /**
     * 이미지 포맷 감지
     */
    public String detectImageFormat(InputStream inputStream) {
        try {
            // 스트림 시작 부분의 바이트를 읽어서 포맷 감지
            inputStream.mark(10);
            byte[] header = inputStream.readNBytes(10);
            inputStream.reset();

            if (header.length >= 3 &&
                    (header[0] & 0xFF) == 0xFF &&
                    (header[1] & 0xFF) == 0xD8 &&
                    (header[2] & 0xFF) == 0xFF) {
                return "JPEG";
            }

            if (header.length >= 8 &&
                    (header[0] & 0xFF) == 0x89 &&
                    (header[1] & 0xFF) == 0x50 &&
                    (header[2] & 0xFF) == 0x4E &&
                    (header[3] & 0xFF) == 0x47) {
                return "PNG";
            }

            if (header.length >= 4 &&
                    (header[0] & 0xFF) == 0x52 &&
                    (header[1] & 0xFF) == 0x49 &&
                    (header[2] & 0xFF) == 0x46 &&
                    (header[3] & 0xFF) == 0x46) {
                return "WEBP";
            }

            return "UNKNOWN";

        } catch (Exception e) {
            logger.error("이미지 포맷 감지 실패", e);
            return "UNKNOWN";
        }
    }

    /**
     * 이미지 압축 (용량 최적화)
     */
    public byte[] compressImage(InputStream imageStream, float compressionRatio) {
        try {
            BufferedImage originalImage = ImageIO.read(imageStream);
            if (originalImage == null) {
                return null;
            }

            return convertToJpegBytes(originalImage, compressionRatio);

        } catch (Exception e) {
            logger.error("이미지 압축 실패", e);
            return null;
        }
    }

    // === 결과 및 정보 클래스 ===

    public static class ThumbnailResult {
        private final boolean success;
        private final byte[] thumbnailData;
        private final ThumbnailInfo thumbnailInfo;
        private final String errorMessage;

        private ThumbnailResult(boolean success, byte[] thumbnailData, ThumbnailInfo thumbnailInfo, String errorMessage) {
            this.success = success;
            this.thumbnailData = thumbnailData;
            this.thumbnailInfo = thumbnailInfo;
            this.errorMessage = errorMessage;
        }

        public static ThumbnailResult success(byte[] thumbnailData, ThumbnailInfo thumbnailInfo) {
            return new ThumbnailResult(true, thumbnailData, thumbnailInfo, null);
        }

        public static ThumbnailResult failure(String errorMessage) {
            return new ThumbnailResult(false, null, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public byte[] getThumbnailData() { return thumbnailData; }
        public ThumbnailInfo getThumbnailInfo() { return thumbnailInfo; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class ThumbnailInfo {
        private final int width;
        private final int height;
        private final long fileSize;
        private final int originalWidth;
        private final int originalHeight;

        public ThumbnailInfo(int width, int height, long fileSize, int originalWidth, int originalHeight) {
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getFileSize() { return fileSize; }
        public int getOriginalWidth() { return originalWidth; }
        public int getOriginalHeight() { return originalHeight; }
        public double getCompressionRatio() {
            long originalEstimatedSize = (long) originalWidth * originalHeight * 3; // RGB 추정
            return originalEstimatedSize > 0 ? (double) fileSize / originalEstimatedSize : 0.0;
        }
    }
}