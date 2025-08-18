package kt.aivle.content.service;

import kt.aivle.common.exception.BusinessException;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import static kt.aivle.content.exception.ContentErrorCode.FAIL_DECODE_IMAGE;
import static kt.aivle.content.exception.ContentErrorCode.THUMBNAIL_GENERATION_ERROR;

public class ThumbnailGenerator {

    public static File createImageThumbnail(InputStream in, int width, int height) throws Exception {
        File tmp = Files.createTempFile("thumb-", ".jpg").toFile();
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
            throw new BusinessException(FAIL_DECODE_IMAGE);
        }
        Thumbnails.of(image)
                .size(width, height)
                .outputFormat("jpg")
                .toFile(tmp);
        return tmp;
    }

    public static File createVideoThumbnail(File videoFile, int width, int height) throws Exception {
        File tmp = Files.createTempFile("thumb-", ".jpg").toFile();
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-hide_banner", "-loglevel", "error",
                "-ss", "00:00:01",
                "-i", videoFile.getAbsolutePath(),
                "-vframes", "1",
                "-vf", "scale=" + width + ":" + height + ":force_original_aspect_ratio=decrease",
                tmp.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        if (p.waitFor() != 0) {
            throw new BusinessException(THUMBNAIL_GENERATION_ERROR);
        }
        return tmp;
    }
}
