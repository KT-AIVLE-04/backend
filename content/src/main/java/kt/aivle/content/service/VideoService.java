package kt.aivle.content.service;

import kt.aivle.content.entity.ContentType;
import kt.aivle.content.entity.Video;
import kt.aivle.content.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VideoService {

    private final VideoRepository videoRepository;
    private final ContentService contentService;
    private final S3Service s3Service;

    public VideoService(VideoRepository videoRepository, ContentService contentService, S3Service s3Service) {
        this.videoRepository = videoRepository;
        this.contentService = contentService;
        this.s3Service = s3Service;
    }

    /**
     * 영상 업로드 및 저장 (썸네일 포함)
     */
    @Transactional
    public Video uploadVideo(MultipartFile file, String title, String userId, MultipartFile thumbnailFile) {
        // 파일 유효성 검증
        contentService.validateFile(file, ContentType.VIDEO);

        try {
            // S3에 영상 업로드
            S3Service.S3UploadResult uploadResult = s3Service.uploadFile(file, S3Service.FOLDER_VIDEOS);

            // Video 엔티티 생성
            Video video = Video.createVideo(
                    title != null && !title.trim().isEmpty() ? title : getDefaultTitle(file.getOriginalFilename()),
                    file.getOriginalFilename(),
                    uploadResult.getS3Url(),
                    uploadResult.getS3Key(),
                    uploadResult.getFileSize(),
                    file.getContentType(),
                    userId
            );

            // 영상 저장
            Video savedVideo = videoRepository.save(video);

            // 썸네일 처리
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                try {
                    // 사용자가 제공한 썸네일 업로드
                    uploadUserThumbnail(savedVideo, thumbnailFile);
                } catch (Exception e) {
                    System.err.println("사용자 썸네일 업로드 실패: " + e.getMessage());
                }
            } else {
                // FFmpeg로 자동 썸네일 생성 시도
                try {
                    generateVideoThumbnailWithFFmpeg(savedVideo);
                } catch (Exception e) {
                    System.err.println("FFmpeg 썸네일 생성 실패, 기본 썸네일 사용: " + e.getMessage());
                    setDefaultThumbnail(savedVideo);
                }
            }

            // 영상 메타데이터 추출 (비동기로 처리할 수도 있음)
            try {
                processVideoMetadata(savedVideo);
            } catch (Exception e) {
                // 메타데이터 처리 실패 시 로그만 남기고 계속 진행
                System.err.println("영상 메타데이터 처리 실패: " + e.getMessage());
            }

            return savedVideo;

        } catch (Exception e) {
            throw new RuntimeException("영상 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 영상 업로드 및 저장 (기존 호환성을 위한 오버로드 메소드)
     */
    @Transactional
    public Video uploadVideo(MultipartFile file, String title, String userId) {
        return uploadVideo(file, title, userId, null);
    }

    /**
     * 사용자별 영상 목록 조회
     */
    public Page<Video> getVideosByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자별 숏츠 영상 목록 조회
     */
    public Page<Video> getShortsByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndIsShortTrueOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 사용자별 일반 영상 목록 조회 (숏츠 제외)
     */
    public Page<Video> getRegularVideosByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndIsShortFalseOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 영상 상세 조회
     */
    public Optional<Video> getVideoById(Long id) {
        return videoRepository.findById(id);
    }

    /**
     * 사용자의 영상 상세 조회 (권한 확인)
     */
    public Optional<Video> getVideoByIdAndUser(Long id, String userId) {
        return videoRepository.findById(id)
                .filter(video -> video.getUserId().equals(userId));
    }

    /**
     * 영상 길이별 조회
     */
    public Page<Video> getVideosByDuration(String userId, Integer minDuration, Integer maxDuration, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndDurationRange(userId, minDuration, maxDuration, pageable);
    }

    /**
     * 해상도별 영상 조회
     */
    public Page<Video> getVideosByResolution(String userId, Integer minWidth, Integer minHeight, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndMinResolution(userId, minWidth, minHeight, pageable);
    }

    /**
     * 코덱별 영상 조회
     */
    public Page<Video> getVideosByCodec(String userId, String codec, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndCodecOrderByCreatedAtDesc(userId, codec, pageable);
    }

    /**
     * 비트레이트별 영상 조회
     */
    public Page<Video> getVideosByBitrate(String userId, Integer minBitrate, Integer maxBitrate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndBitrateRange(userId, minBitrate, maxBitrate, pageable);
    }

    /**
     * 파일명으로 영상 검색
     */
    public Page<Video> searchVideosByFilename(String userId, String filename, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return videoRepository.findByUserIdAndOriginalFilenameContaining(userId, filename, pageable);
    }

    /**
     * 최근 업로드된 영상 조회 (미리보기용)
     */
    public List<Video> getRecentVideos(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return videoRepository.findRecentVideosByUserId(userId, pageable);
    }

    /**
     * 해상도별 통계
     */
    public List<Object[]> getResolutionStats(String userId) {
        return videoRepository.getResolutionStats(userId);
    }

    /**
     * 영상 길이별 통계
     */
    public List<Object[]> getDurationStats(String userId) {
        return videoRepository.getDurationStats(userId);
    }

    /**
     * 사용자의 총 영상 재생 시간
     */
    public Long getTotalDuration(String userId) {
        return videoRepository.getTotalDurationByUserId(userId);
    }

    /**
     * 사용자의 총 영상 재생 시간 (포맷팅)
     */
    public String getFormattedTotalDuration(String userId) {
        Long totalSeconds = getTotalDuration(userId);
        if (totalSeconds == null || totalSeconds == 0) {
            return "0분";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }

    /**
     * 영상 삭제
     */
    @Transactional
    public void deleteVideo(Long id, String userId) {
        contentService.deleteContent(id, userId);
    }

    /**
     * 썸네일이 없는 영상들에 대해 썸네일 생성 (배치 작업)
     */
    @Transactional
    public void generateMissingThumbnails() {
        List<Video> videosWithoutThumbnail = videoRepository.findVideosWithoutThumbnail();

        for (Video video : videosWithoutThumbnail) {
            try {
                generateVideoThumbnail(video);
                videoRepository.save(video);
            } catch (Exception e) {
                System.err.println("영상 " + video.getId() + " 썸네일 생성 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 영상 압축 (실제 구현에서는 FFmpeg 등 사용)
     */
    @Transactional
    public Video compressVideo(Long videoId, String userId, String quality) {
        Video video = getVideoByIdAndUser(videoId, userId)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없거나 권한이 없습니다."));

        try {
            // 실제 구현에서는 FFmpeg를 사용하여 영상 압축
            // 여기서는 간단히 메타데이터만 업데이트
            String compressedS3Key = video.getS3Key().replace(".", "_compressed.");
            String compressedUrl = video.getS3Url().replace(".", "_compressed.");

            // 압축된 영상을 새로운 파일로 저장하는 로직이 들어갈 자리
            // 실제로는 압축 작업이 완료된 후 업데이트

            return video;

        } catch (Exception e) {
            throw new RuntimeException("영상 압축 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // === Private Helper Methods ===

    /**
     * 영상 메타데이터 처리 및 썸네일 생성
     */
    private void processVideoMetadata(Video video) {
        try {
            // FFmpeg로 영상 정보 추출
            VideoMetadata metadata = extractVideoMetadata(video.getS3Url());

            // 메타데이터 업데이트
            if (metadata != null) {
                video.updateMetadata(
                        metadata.getDurationSeconds(),
                        metadata.getWidth(),
                        metadata.getHeight(),
                        metadata.getBitrate(),
                        metadata.getFrameRate(),
                        metadata.getCodec()
                );
            }

            // 썸네일이 없으면 자동 생성
            if (video.getThumbnailUrl() == null || video.getThumbnailUrl().isEmpty()) {
                generateVideoThumbnailWithFFmpeg(video);
            }

            videoRepository.save(video);

        } catch (Exception e) {
            System.err.println("영상 메타데이터 처리 실패: " + e.getMessage());
        }
    }

    /**
     * FFmpeg를 사용한 영상 메타데이터 추출
     */
    private VideoMetadata extractVideoMetadata(String videoUrl) {
        try {
            // FFprobe 명령어로 메타데이터 추출
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffprobe",
                    "-v", "quiet",
                    "-print_format", "json",
                    "-show_format",
                    "-show_streams",
                    videoUrl
            );

            Process process = processBuilder.start();

            // JSON 결과 읽기
            StringBuilder jsonResult = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResult.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return parseVideoMetadata(jsonResult.toString());
            } else {
                System.err.println("FFprobe 실행 실패, exit code: " + exitCode);
                return null;
            }

        } catch (Exception e) {
            System.err.println("메타데이터 추출 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * FFmpeg로 썸네일 생성 (0.05초 지점)
     */
    /**
     * FFmpeg로 썸네일 생성 (0.05초 지점) - 고화질 버전
     */
    private void generateVideoThumbnailWithFFmpeg(Video video) {
        try {
            // 임시 썸네일 파일 경로
            String tempThumbnailPath = System.getProperty("java.io.tmpdir") +
                    "/thumbnail_" + video.getId() + "_" + System.currentTimeMillis() + ".jpg";

            // 고화질 FFmpeg 명령어
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", video.getS3Url(),        // 입력 영상 URL
                    "-ss", "0.05",                 // 0.05초 지점
                    "-vframes", "1",               // 1프레임만
                    "-vf", "scale=1280:720",       // 1280x720 고해상도로 변경
                    "-q:v", "2",                   // 고품질 설정 추가
                    "-y",                          // 기존 파일 덮어쓰기
                    tempThumbnailPath              // 출력 파일
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // 생성된 썸네일을 S3에 업로드
                uploadThumbnailToS3(video, tempThumbnailPath);
            } else {
                System.err.println("FFmpeg 썸네일 생성 실패, exit code: " + exitCode);
                // 실패 시 기본 썸네일 사용
                setDefaultThumbnail(video);
            }

            // 임시 파일 삭제
            java.io.File tempFile = new java.io.File(tempThumbnailPath);
            if (tempFile.exists()) {
                tempFile.delete();
            }

        } catch (Exception e) {
            System.err.println("FFmpeg 썸네일 생성 중 오류: " + e.getMessage());
            // 실패 시 기본 썸네일 사용
            setDefaultThumbnail(video);
        }
    }

    /**
     * 생성된 썸네일을 S3에 업로드
     */
    private void uploadThumbnailToS3(Video video, String thumbnailPath) throws IOException {
        java.io.File thumbnailFile = new java.io.File(thumbnailPath);
        if (!thumbnailFile.exists()) {
            throw new IOException("썸네일 파일이 존재하지 않습니다: " + thumbnailPath);
        }

        // 파일을 InputStream으로 읽기
        try (java.io.FileInputStream fis = new java.io.FileInputStream(thumbnailFile)) {
            String thumbnailFilename = "ffmpeg_thumb_" + video.getId() + "_" + System.currentTimeMillis() + ".jpg";

            S3Service.S3UploadResult result = s3Service.uploadInputStream(
                    fis,
                    "image/jpeg",
                    thumbnailFile.length(),
                    S3Service.FOLDER_THUMBNAILS,
                    thumbnailFilename
            );

            // 영상 엔티티에 썸네일 정보 업데이트
            video.updateThumbnail(result.getS3Url(), result.getS3Key());
        }
    }

    /**
     * JSON 메타데이터 파싱
     */
    private VideoMetadata parseVideoMetadata(String jsonData) {
        try {
            // 간단한 JSON 파싱 (실제로는 Jackson 라이브러리 사용 권장)
            VideoMetadata metadata = new VideoMetadata();

            // duration 추출 (format.duration)
            if (jsonData.contains("\"duration\"")) {
                String durationStr = extractJsonValue(jsonData, "duration");
                if (durationStr != null) {
                    double duration = Double.parseDouble(durationStr);
                    metadata.setDurationSeconds((int) Math.round(duration));
                }
            }

            // 비디오 스트림 정보 추출
            if (jsonData.contains("\"codec_type\":\"video\"")) {
                String width = extractVideoStreamValue(jsonData, "width");
                String height = extractVideoStreamValue(jsonData, "height");
                String codec = extractVideoStreamValue(jsonData, "codec_name");
                String frameRate = extractVideoStreamValue(jsonData, "avg_frame_rate");
                String bitRate = extractVideoStreamValue(jsonData, "bit_rate");

                if (width != null) metadata.setWidth(Integer.parseInt(width));
                if (height != null) metadata.setHeight(Integer.parseInt(height));
                if (codec != null) metadata.setCodec(codec);
                if (bitRate != null) metadata.setBitrate(Integer.parseInt(bitRate));
                if (frameRate != null) {
                    // frame rate는 "30/1" 형식일 수 있음
                    if (frameRate.contains("/")) {
                        String[] parts = frameRate.split("/");
                        if (parts.length == 2) {
                            double rate = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                            metadata.setFrameRate(rate);
                        }
                    } else {
                        metadata.setFrameRate(Double.parseDouble(frameRate));
                    }
                }
            }

            return metadata;

        } catch (Exception e) {
            System.err.println("메타데이터 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    // 간단한 JSON 값 추출 헬퍼 메소드들
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String extractVideoStreamValue(String json, String key) {
        // 비디오 스트림 섹션에서 값 추출
        String videoStreamPattern = "\"codec_type\"\\s*:\\s*\"video\"[^}]+";
        java.util.regex.Pattern streamPattern = java.util.regex.Pattern.compile(videoStreamPattern);
        java.util.regex.Matcher streamMatcher = streamPattern.matcher(json);

        if (streamMatcher.find()) {
            String videoStreamSection = streamMatcher.group();
            return extractJsonValue(videoStreamSection, key);
        }
        return null;
    }

    // VideoMetadata 내부 클래스
    private static class VideoMetadata {
        private Integer durationSeconds;
        private Integer width;
        private Integer height;
        private Integer bitrate;
        private Double frameRate;
        private String codec;

        // Getters and Setters
        public Integer getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }

        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }

        public Integer getBitrate() { return bitrate; }
        public void setBitrate(Integer bitrate) { this.bitrate = bitrate; }

        public Double getFrameRate() { return frameRate; }
        public void setFrameRate(Double frameRate) { this.frameRate = frameRate; }

        public String getCodec() { return codec; }
        public void setCodec(String codec) { this.codec = codec; }
    }

    /**
     * 사용자가 제공한 썸네일 업로드
     */
    private void uploadUserThumbnail(Video video, MultipartFile thumbnailFile) throws IOException {
        // 썸네일 파일 유효성 검증
        if (!isValidThumbnailFile(thumbnailFile)) {
            throw new RuntimeException("올바르지 않은 썸네일 파일입니다. JPG, PNG 파일만 업로드 가능합니다.");
        }

        // S3에 썸네일 업로드
        S3Service.S3UploadResult thumbnailResult = s3Service.uploadFile(thumbnailFile, S3Service.FOLDER_THUMBNAILS);

        // 영상 엔티티에 썸네일 정보 업데이트
        video.updateThumbnail(thumbnailResult.getS3Url(), thumbnailResult.getS3Key());
        videoRepository.save(video);
    }

    /**
     * 기본 썸네일 설정
     */
    private void setDefaultThumbnail(Video video) {
        // 기본 영상 아이콘 URL 설정 (S3에 미리 업로드해놓은 기본 이미지)
        String defaultThumbnailUrl = "https://aivle-image-test1.s3.ap-southeast-2.amazonaws.com/defaults/video-thumbnail.png";
        String defaultThumbnailKey = "defaults/video-thumbnail.png";

        video.updateThumbnail(defaultThumbnailUrl, defaultThumbnailKey);
        videoRepository.save(video);
    }

    /**
     * 썸네일 파일 유효성 검증
     */
    private boolean isValidThumbnailFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png");
    }

    // 기존 generateVideoThumbnail 메소드 수정
    /**
     * 기본 썸네일 생성 (FFmpeg 없이 기본 이미지 사용)
     */
    private void generateVideoThumbnail(Video video) {
        try {
            // FFmpeg 대신 기본 썸네일 사용
            setDefaultThumbnail(video);
        } catch (Exception e) {
            System.err.println("기본 썸네일 설정 실패: " + e.getMessage());
        }
    }

    /**
     * 기본 제목 생성
     */
    private String getDefaultTitle(String originalFilename) {
        if (originalFilename == null) return "제목 없음";

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return originalFilename.substring(0, lastDotIndex);
        }
        return originalFilename;
    }

    /**
     * 영상 길이를 사람이 읽기 쉬운 형태로 변환
     */
    public String formatDuration(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return "00:00";
        }

        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 변환
     */
    public String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return String.format("%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }
}