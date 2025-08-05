// VideoUtils.java
package kt.aivle.content.util;

import kt.aivle.content.dto.common.FileMetadata;
import kt.aivle.content.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Component
@Slf4j
public class VideoUtils {

    /**
     * 영상에서 썸네일 추출 (FFmpeg 사용)
     */
    public void extractThumbnail(String videoPath, String thumbnailPath, int timeInSeconds) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-ss", String.valueOf(timeInSeconds),
                    "-vframes", "1",
                    "-y", // 덮어쓰기
                    thumbnailPath
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new FileProcessingException("썸네일 생성에 실패했습니다.");
            }

            log.info("Thumbnail extracted: {} -> {}", videoPath, thumbnailPath);

        } catch (IOException | InterruptedException e) {
            log.error("Failed to extract thumbnail", e);
            throw new FileProcessingException("썸네일 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 영상 메타데이터 추출 (FFprobe 사용)
     */
    public FileMetadata extractMetadata(String videoPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "quiet",
                    "-print_format", "json",
                    "-show_format",
                    "-show_streams",
                    videoPath
            );

            Process process = pb.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new FileProcessingException("영상 메타데이터 추출에 실패했습니다.");
            }

            return parseVideoMetadata(output.toString());

        } catch (IOException | InterruptedException e) {
            log.error("Failed to extract video metadata", e);
            throw new FileProcessingException("영상 메타데이터 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * FFprobe JSON 출력을 파싱하여 메타데이터 추출
     */
    private FileMetadata parseVideoMetadata(String jsonOutput) {
        // 실제 구현에서는 Jackson ObjectMapper 사용 권장
        // 여기서는 간단한 파싱 예시

        FileMetadata.FileMetadataBuilder builder = FileMetadata.builder();

        // JSON 파싱 로직 (실제로는 ObjectMapper 사용)
        if (jsonOutput.contains("\"duration\"")) {
            // duration 추출 로직
            String durationStr = extractJsonValue(jsonOutput, "duration");
            if (durationStr != null) {
                try {
                    Double duration = Double.parseDouble(durationStr);
                    builder.duration(duration.intValue());
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse duration: {}", durationStr);
                }
            }
        }

        // width, height 추출
        String widthStr = extractJsonValue(jsonOutput, "width");
        String heightStr = extractJsonValue(jsonOutput, "height");

        if (widthStr != null && heightStr != null) {
            try {
                builder.width(Integer.parseInt(widthStr));
                builder.height(Integer.parseInt(heightStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse video dimensions");
            }
        }

        // 기타 메타데이터 추출...

        return builder
                .uploadDate(LocalDateTime.now())
                .build();
    }

    /**
     * 간단한 JSON 값 추출 유틸리티
     */
    private String extractJsonValue(String json, String key) {
        String searchPattern = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) {
            // 숫자 값인 경우
            searchPattern = "\"" + key + "\":";
            startIndex = json.indexOf(searchPattern);
            if (startIndex == -1) {
                return null;
            }
            startIndex += searchPattern.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf("}", startIndex);
            }
            return json.substring(startIndex, endIndex).trim();
        } else {
            startIndex += searchPattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        }
    }

    public String generateVideoThumbnail(String filePath) {
        // 예시) 썸네일 이미지를 ffmpeg 등으로 추출한 뒤, 썸네일 경로를 반환
        String videoPath="D:\\AIVLE_BPSNS";
        String thumbnailPath = videoPath + "_thumbnail.jpg";
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-ss", "00:00:01",
                    "-vframes", "1",
                    "-q:v", "2",
                    "-y",
                    thumbnailPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return thumbnailPath;
            } else {
                throw new RuntimeException("썸네일 생성 실패(exit=" + exitCode + ")");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("썸네일 생성 과정에서 오류 발생: " + e.getMessage());
        }
    }
}