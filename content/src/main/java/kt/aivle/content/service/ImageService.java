package kt.aivle.content.service;

import kt.aivle.content.entity.ContentType;
import kt.aivle.content.entity.Image;
import kt.aivle.content.repository.ImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final ContentService contentService;
    private final S3Service s3Service;

    public ImageService(ImageRepository imageRepository, ContentService contentService, S3Service s3Service) {
        this.imageRepository = imageRepository;
        this.contentService = contentService;
        this.s3Service = s3Service;
    }

    /**
     * 이미지 업로드 및 저장
     */
    @Transactional
    public Image uploadImage(MultipartFile file, String title, String userId) {
        // 파일 유효성 검증
        contentService.validateFile(file, ContentType.IMAGE);

        try {
            // S3에 원본 이미지 업로드
            S3Service.S3UploadResult uploadResult = s3Service.uploadFile(file, S3Service.FOLDER_IMAGES);

            // 이미지 메타데이터 추출
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Integer width = null;
            Integer height = null;

            if (bufferedImage != null) {
                width = bufferedImage.getWidth();
                height = bufferedImage.getHeight();
            }

            // Image 엔티티 생성
            Image image = Image.createImage(
                    title != null && !title.trim().isEmpty() ? title : getDefaultTitle(file.getOriginalFilename()),
                    file.getOriginalFilename(),
                    uploadResult.getS3Url(),
                    uploadResult.getS3Key(),
                    uploadResult.getFileSize(),
                    file.getContentType(),
                    userId
            );

            // 이미지 크기 정보 설정
            if (width != null && height != null) {
                image.updateDimensions(width, height);
            }

            // 썸네일 생성 및 업로드 (비동기로 처리할 수도 있음)
            try {
                createAndUploadThumbnail(image, bufferedImage);
            } catch (Exception e) {
                // 썸네일 생성 실패 시 로그만 남기고 계속 진행
                System.err.println("썸네일 생성 실패: " + e.getMessage());
            }

            return imageRepository.save(image);

        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자별 이미지 목록 조회
     */
    public Page<Image> getImagesByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return imageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 이미지 상세 조회
     */
    public Optional<Image> getImageById(Long id) {
        return imageRepository.findById(id);
    }

    /**
     * 사용자의 이미지 상세 조회 (권한 확인)
     */
    public Optional<Image> getImageByIdAndUser(Long id, String userId) {
        return imageRepository.findById(id)
                .filter(image -> image.getUserId().equals(userId));
    }

    /**
     * 해상도별 이미지 조회
     */
    public Page<Image> getImagesByResolution(String userId, Integer minWidth, Integer minHeight, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return imageRepository.findByUserIdAndMinResolution(userId, minWidth, minHeight, pageable);
    }

    /**
     * 가로세로 비율별 이미지 조회
     */
    public Page<Image> getImagesByAspectRatio(String userId, String aspectRatio, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return imageRepository.findByUserIdAndAspectRatio(userId, aspectRatio, pageable);
    }

    /**
     * 파일명으로 이미지 검색
     */
    public Page<Image> searchImagesByFilename(String userId, String filename, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return imageRepository.findByUserIdAndOriginalFilenameContaining(userId, filename, pageable);
    }

    /**
     * 최근 업로드된 이미지 조회 (미리보기용)
     */
    public List<Image> getRecentImages(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return imageRepository.findRecentImagesByUserId(userId, pageable);
    }

    /**
     * 해상도별 통계
     */
    public List<Object[]> getResolutionStats(String userId) {
        return imageRepository.getResolutionStats(userId);
    }

    /**
     * 이미지 삭제
     */
    @Transactional
    public void deleteImage(Long id, String userId) {
        contentService.deleteContent(id, userId);
    }

    /**
     * 썸네일이 없는 이미지들에 대해 썸네일 생성 (배치 작업)
     */
    @Transactional
    public void generateMissingThumbnails() {
        List<Image> imagesWithoutThumbnail = imageRepository.findImagesWithoutThumbnail();

        for (Image image : imagesWithoutThumbnail) {
            try {
                // S3에서 원본 이미지 다운로드는 실제 구현에서는 URL을 통해 처리
                // 여기서는 간단히 썸네일 URL을 원본 URL로 설정
                image.updateThumbnail(image.getS3Url(), image.getS3Key());
                imageRepository.save(image);
            } catch (Exception e) {
                System.err.println("이미지 " + image.getId() + " 썸네일 생성 실패: " + e.getMessage());
            }
        }
    }

    // === Private Helper Methods ===

    /**
     * 썸네일 생성 및 S3 업로드
     */
    private void createAndUploadThumbnail(Image image, BufferedImage originalImage) throws IOException {
        if (originalImage == null) return;

        // 썸네일 크기 설정 (최대 300x300)
        int thumbnailWidth = 300;
        int thumbnailHeight = 300;

        // 원본 비율 유지하면서 썸네일 크기 계산
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= thumbnailWidth && originalHeight <= thumbnailHeight) {
            // 원본이 썸네일 크기보다 작으면 썸네일을 원본으로 사용
            image.updateThumbnail(image.getS3Url(), image.getS3Key());
            return;
        }

        double ratio = Math.min((double) thumbnailWidth / originalWidth, (double) thumbnailHeight / originalHeight);
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // 썸네일 이미지 생성
        BufferedImage thumbnailImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        thumbnailImage.getGraphics().drawImage(
                originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH),
                0, 0, null
        );

        // 썸네일을 byte array로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", baos);
        byte[] thumbnailBytes = baos.toByteArray();

        // S3에 썸네일 업로드
        String thumbnailFilename = "thumb_" + System.currentTimeMillis() + ".jpg";
        S3Service.S3UploadResult thumbnailResult = s3Service.uploadInputStream(
                new ByteArrayInputStream(thumbnailBytes),
                "image/jpeg",
                thumbnailBytes.length,
                S3Service.FOLDER_THUMBNAILS,
                thumbnailFilename
        );

        // 이미지 엔티티에 썸네일 정보 업데이트
        image.updateThumbnail(thumbnailResult.getS3Url(), thumbnailResult.getS3Key());
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
}