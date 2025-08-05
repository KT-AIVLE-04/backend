// ImageUtils.java
package kt.aivle.content.util;

import kt.aivle.content.dto.common.FileMetadata;
import kt.aivle.content.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

@Component
@Slf4j
public class ImageUtils {

    /**
     * 이미지 리사이징 및 처리
     */
    public BufferedImage processImage(BufferedImage originalImage, Integer targetWidth, Integer targetHeight) {
        if (targetWidth == null && targetHeight == null) {
            return originalImage;
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 비율 유지하며 리사이징
        Dimension newDimension = calculateNewDimensions(
                originalWidth, originalHeight, targetWidth, targetHeight
        );

        BufferedImage resizedImage = new BufferedImage(
                newDimension.width, newDimension.height, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, newDimension.width, newDimension.height, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * 압축된 이미지 저장
     */
    public void saveCompressedImage(BufferedImage image, File outputFile, String format, float quality) {
        try {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                throw new FileProcessingException("지원하지 않는 이미지 형식입니다: " + format);
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(ios);
                writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
            }

            writer.dispose();

        } catch (IOException e) {
            log.error("Failed to save compressed image", e);
            throw new FileProcessingException("압축된 이미지 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 메타데이터 추출
     */
    public FileMetadata extractMetadata(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                throw new FileProcessingException("이미지 파일을 읽을 수 없습니다: " + imagePath);
            }

            return FileMetadata.builder()
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .colorSpace(getColorSpaceName(image.getColorModel().getColorSpace().getType()))
                    .uploadDate(LocalDateTime.now())
                    .build();

        } catch (IOException e) {
            log.error("Failed to extract image metadata", e);
            throw new FileProcessingException("이미지 메타데이터 추출에 실패했습니다: " + e.getMessage());
        }
    }

    private Dimension calculateNewDimensions(int originalWidth, int originalHeight,
                                             Integer targetWidth, Integer targetHeight) {
        // 원본 비율
        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (targetWidth != null && targetHeight != null) {
            // 둘 다 지정되면 한계 내에서 비율 맞추기
            double widthRatio = (double) targetWidth / originalWidth;
            double heightRatio = (double) targetHeight / originalHeight;
            double scale = Math.min(widthRatio, heightRatio);
            newWidth = (int) (originalWidth * scale);
            newHeight = (int) (originalHeight * scale);
        } else if (targetWidth != null) {
            newWidth = targetWidth;
            newHeight = (int) (targetWidth / aspectRatio);
        } else if (targetHeight != null) {
            newHeight = targetHeight;
            newWidth = (int) (targetHeight * aspectRatio);
        }

        return new Dimension(newWidth, newHeight);
    }
    private String getColorSpaceName(int colorSpaceType) {
        switch (colorSpaceType) {
            case ColorSpace.TYPE_RGB: return "RGB";
            case ColorSpace.TYPE_GRAY: return "GRAY";
            case ColorSpace.TYPE_CMYK: return "CMYK";
            case ColorSpace.TYPE_YCbCr: return "YCbCr";
            case ColorSpace.TYPE_HSV: return "HSV";
            case ColorSpace.TYPE_HLS: return "HLS";
            case ColorSpace.TYPE_XYZ: return "XYZ";
            case ColorSpace.TYPE_Lab: return "Lab";
            case ColorSpace.TYPE_Luv: return "Luv";
            case ColorSpace.TYPE_Yxy: return "Yxy";
            default: return "UNKNOWN";
        }
    }

}