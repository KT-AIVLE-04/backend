package kt.aivle.content.service;

import kt.aivle.common.exception.BusinessException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static kt.aivle.content.exception.ContentErrorCode.FAIL_DECODE_IMAGE;
import static kt.aivle.content.exception.ContentErrorCode.FAIL_GET_METADATA;

public class MediaMetadataExtractor {

    public record MediaMeta(Integer width, Integer height, Integer durationSeconds) {
    }

    public static MediaMeta extractImageMeta(InputStream in) throws Exception {
        BufferedImage img = ImageIO.read(in);
        if (img == null) {
            throw new BusinessException(FAIL_DECODE_IMAGE);
        }
        return new MediaMeta(img.getWidth(), img.getHeight(), null);
    }

    public static MediaMeta extractVideoMeta(File videoFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height:format=duration",
                "-of", "default=noprint_wrappers=1:nokey=0",
                videoFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        Integer width = null, height = null;
        Integer durationSeconds = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("width=")) width = parseInt(line.substring(6));
                if (line.startsWith("height=")) height = parseInt(line.substring(7));
                if (line.startsWith("duration=")) {
                    Double d = parseDouble(line.substring(9));
                    if (d != null) durationSeconds = Math.toIntExact(Math.round(d));
                }
            }
        }
        if (p.waitFor() != 0) throw new BusinessException(FAIL_GET_METADATA);

        return new MediaMeta(width, height, durationSeconds);
    }

    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
