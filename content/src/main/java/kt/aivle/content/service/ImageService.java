package kt.aivle.content.service;

import kt.aivle.content.config.AwsConfig;
import kt.aivle.content.dto.ImageDto;
import kt.aivle.content.entity.Image;
import kt.aivle.content.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.base-url}")
    private String s3BaseUrl;

    // 지원 파일 형식
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 이미지 업로드
     */
    public ImageDto uploadImage(MultipartFile file, String title, String keywords) {
        validateFile(file);

        try {
            // S3에 파일 업로드
            String s3Key = generateS3Key(file.getOriginalFilename());
            String imageUrl = uploadToS3(file, s3Key);

            // 썸네일 생성 (실제로는 리사이징 로직 필요)
            String thumbnailUrl = generateThumbnail(file, s3Key);

            // DB에 메타데이터 저장
            Image image = Image.builder()
                    .title(title)
                    .keywords(keywords)
                    .originalFilename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .imageUrl(imageUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .s3Key(s3Key)
                    .contentType(file.getContentType())
                    .createdAt(LocalDateTime.now())
                    .build();

            Image savedImage = imageRepository.save(image);
            log.info("이미지 업로드 완료: {}", savedImage.getId());

            return convertToDto(savedImage);

        } catch (Exception e) {
            log.error("이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 이미지 목록 조회 (페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<ImageDto> getImageList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Image> imagePage = imageRepository.findAll(pageable);

        return imagePage.map(this::convertToDto);
    }

    /**
     * 이미지 상세 조회
     */
    @Transactional(readOnly = true)
    public ImageDto getImageDetail(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        return convertToDto(image);
    }

    /**
     * 이미지 삭제 (Hard Delete)
     */
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

        try {
            // S3에서 파일 삭제
            deleteFromS3(image.getS3Key());

            // 썸네일도 삭제
            if (image.getThumbnailUrl() != null) {
                String thumbnailKey = extractS3KeyFromUrl(image.getThumbnailUrl());
                deleteFromS3(thumbnailKey);
            }

            // DB에서 삭제
            imageRepository.delete(image);
            log.info("이미지 삭제 완료: {}", imageId);

        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 키워드로 이미지 검색
     */
    @Transactional(readOnly = true)
    public Page<ImageDto> searchImagesByKeywords(String keywords, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Image> imagePage = imageRepository.findByKeywordsContainingIgnoreCase(keywords, pageable);

        return imagePage.map(this::convertToDto);
    }

    // === Private Methods ===

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. (최대 10MB)");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (지원: JPG, PNG, WebP)");
        }
    }

    /**
     * S3 키 생성
     */
    private String generateS3Key(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return "images/" + LocalDateTime.now().toLocalDate() + "/" + uuid + "." + extension;
    }

    /**
     * S3에 파일 업로드
     */
    private String uploadToS3(MultipartFile file, String s3Key) throws IOException {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return s3BaseUrl + "/" + s3Key;
    }

    /**
     * 썸네일 생성 (실제로는 이미지 리사이징 라이브러리 필요)
     */
    private String generateThumbnail(MultipartFile file, String originalS3Key) throws IOException {
        // 실제 구현에서는 이미지 리사이징 로직 필요
        // 여기서는 원본을 썸네일로 사용 (데모용)
        String thumbnailKey = originalS3Key.replace("images/", "thumbnails/");

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(thumbnailKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return s3BaseUrl + "/" + thumbnailKey;
    }

    /**
     * S3에서 파일 삭제
     */
    private void deleteFromS3(String s3Key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    /**
     * URL에서 S3 키 추출
     */
    private String extractS3KeyFromUrl(String url) {
        return url.replace(s3BaseUrl + "/", "");
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    /**
     * Entity를 DTO로 변환
     */
    private ImageDto convertToDto(Image image) {
        return ImageDto.builder()
                .id(image.getId())
                .title(image.getTitle())
                .keywords(image.getKeywords())
                .originalFilename(image.getOriginalFilename())
                .fileSize(image.getFileSize())
                .imageUrl(image.getImageUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .contentType(image.getContentType())
                .createdAt(image.getCreatedAt())
                .build();
    }
}