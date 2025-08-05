package kt.aivle.content.service;

import kt.aivle.content.domain.ImageContent;
import kt.aivle.content.domain.VideoContent;
import kt.aivle.content.dto.common.ContentDto;
import kt.aivle.content.dto.request.ContentSearchDto;
import kt.aivle.content.dto.request.ImageUploadDto;
import kt.aivle.content.dto.request.VideoUploadDto;
import kt.aivle.content.dto.response.*;
import kt.aivle.content.exception.ContentNotFoundException;
import kt.aivle.content.exception.FileProcessingException;
import kt.aivle.content.exception.InvalidFileException;
import kt.aivle.content.mapper.ContentMapper;
import kt.aivle.content.repository.ContentRepositoryCustom;
import kt.aivle.content.repository.ImageContentRepository;
import kt.aivle.content.repository.VideoContentRepository;
import kt.aivle.content.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ContentServiceImpl implements ContentService {

    private final VideoContentRepository videoRepository;
    private final ImageContentRepository imageRepository;
    private final ContentRepositoryCustom contentRepositoryCustom;
    private final FileStorageService fileStorageService;
    private final ContentMapper contentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<VideoContentDto> getVideoHistory(Pageable pageable) {
        log.debug("Fetching video history with page: {}", pageable);

        Page<VideoContent> videos = videoRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable);
        return videos.map(contentMapper::toVideoDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImageContentDto> getImageHistory(Pageable pageable) {
        log.debug("Fetching image history with page: {}", pageable);

        Page<ImageContent> images = imageRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable);
        return images.map(contentMapper::toImageDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentDto> getAllContents(ContentSearchDto searchDto, Pageable pageable) {
        log.debug("Fetching all contents with filter: {}", searchDto);

        return contentRepositoryCustom.findContentsWithFilter(searchDto, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentDto> searchContentsByTitle(String title, Pageable pageable) {
        log.debug("Searching contents by title: {}", title);

        return contentRepositoryCustom.findByTitleContainingIgnoreCaseAndIsDeletedFalse(title, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoContentDetailDto getVideoDetail(Long id) {
        log.debug("Fetching video detail for id: {}", id);

        VideoContent video = videoRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ContentNotFoundException("영상을 찾을 수 없습니다. ID: " + id));

        return contentMapper.toVideoDetailDto(video);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageContentDetailDto getImageDetail(Long id) {
        log.debug("Fetching image detail for id: {}", id);

        ImageContent image = imageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ContentNotFoundException("이미지를 찾을 수 없습니다. ID: " + id));

        return contentMapper.toImageDetailDto(image);
    }

    @Override
    public VideoContentDto uploadVideo(MultipartFile file, VideoUploadDto uploadDto) {
        log.info("Uploading video file: {}", file.getOriginalFilename());

        try {
            // 파일 저장
            UploadResultDto uploadResult = fileStorageService.saveVideoFile(file);

            // 썸네일 생성 (0:01초 캡처)
            String thumbnailPath = fileStorageService.generateVideoThumbnail(uploadResult.getFilePath());

            // 영상 메타데이터 추출
            var metadata = fileStorageService.extractVideoMetadata(uploadResult.getFilePath());

            // 엔티티 생성
            VideoContent video = VideoContent.builder()
                    .title(uploadDto.getTitle())
                    .scenario(uploadDto.getScenario())
                    .isAiGenerated(uploadDto.getIsAiGenerated())
                    .filePath(uploadResult.getFilePath())
                    .fileName(uploadResult.getFileName())
                    .fileSize(uploadResult.getFileSize())
                    .contentType(uploadResult.getContentType())
                    .thumbnailPath(thumbnailPath)
                    .videoFormat(FileUtils.getFileExtension(uploadResult.getFileName()).toUpperCase())
                    .width(metadata.getWidth())
                    .height(metadata.getHeight())
                    .duration(metadata.getDuration())
                    .bitrate(metadata.getBitrate())
                    .frameRate(metadata.getFrameRate())
                    .build();

            VideoContent savedVideo = videoRepository.save(video);

            log.info("Video uploaded successfully with id: {}", savedVideo.getId());
            return contentMapper.toVideoDto(savedVideo);

        } catch (Exception e) {
            log.error("Failed to upload video", e);
            throw new FileProcessingException("영상 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public ImageContentDto uploadImage(MultipartFile file, ImageUploadDto uploadDto) {
        log.info("Uploading image file: {}", file.getOriginalFilename());

        try {
            // 파일 저장 및 압축
            UploadResultDto uploadResult = fileStorageService.saveImageFile(
                    file,
                    uploadDto.getEnableCompression(),
                    uploadDto.getTargetWidth(),
                    uploadDto.getTargetHeight(),
                    uploadDto.getQuality()
            );

            // 이미지 메타데이터 추출
            var metadata = fileStorageService.extractImageMetadata(uploadResult.getFilePath());

            // 엔티티 생성
            ImageContent image = ImageContent.builder()
                    .title(uploadDto.getTitle())
                    .keywords(uploadDto.getKeywords())
                    .scenario(uploadDto.getScenario())
                    .isAiGenerated(uploadDto.getIsAiGenerated())
                    .filePath(uploadResult.getFilePath())
                    .fileName(uploadResult.getFileName())
                    .fileSize(uploadResult.getFileSize())
                    .contentType(uploadResult.getContentType())
                    .originalFileName(file.getOriginalFilename())
                    .imageFormat(FileUtils.getFileExtension(uploadResult.getFileName()).toUpperCase())
                    .width(metadata.getWidth())
                    .height(metadata.getHeight())
                    .colorSpace(metadata.getColorSpace())
                    .dpi(metadata.getDpi())
                    .isCompressed(uploadDto.getEnableCompression())
                    .build();

            ImageContent savedImage = imageRepository.save(image);

            log.info("Image uploaded successfully with id: {}", savedImage.getId());
            return contentMapper.toImageDto(savedImage);

        } catch (Exception e) {
            log.error("Failed to upload image", e);
            throw new FileProcessingException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    public void deleteVideo(Long id, boolean hardDelete) {
        log.info("Deleting video with id: {}, hardDelete: {}", id, hardDelete);

        VideoContent video = videoRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("영상을 찾을 수 없습니다. ID: " + id));

        if (hardDelete) {
            // 물리적 파일 삭제
            fileStorageService.deleteFile(video.getFilePath());
            if (video.getThumbnailPath() != null) {
                fileStorageService.deleteFile(video.getThumbnailPath());
            }
            videoRepository.delete(video);
            log.info("Video hard deleted: {}", id);
        } else {
            // 소프트 삭제
            video.delete();
            videoRepository.save(video);
            log.info("Video soft deleted: {}", id);
        }
    }

    @Override
    public void deleteImage(Long id, boolean hardDelete) {
        log.info("Deleting image with id: {}, hardDelete: {}", id, hardDelete);

        ImageContent image = imageRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("이미지를 찾을 수 없습니다. ID: " + id));

        if (hardDelete) {
            // 물리적 파일 삭제
            fileStorageService.deleteFile(image.getFilePath());
            imageRepository.delete(image);
            log.info("Image hard deleted: {}", id);
        } else {
            // 소프트 삭제
            image.delete();
            imageRepository.save(image);
            log.info("Image soft deleted: {}", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileResourceDto prepareVideoDownload(Long id) {
        log.debug("Preparing video download for id: {}", id);

        VideoContent video = videoRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ContentNotFoundException("영상을 찾을 수 없습니다. ID: " + id));

        try {
            Resource resource = new FileSystemResource(video.getFilePath());
            if (!resource.exists()) {
                throw new ContentNotFoundException("영상 파일이 존재하지 않습니다.");
            }

            return FileResourceDto.builder()
                    .resource(resource)
                    .fileName(video.getFileName())
                    .contentType(video.getContentType())
                    .fileSize(video.getFileSize())
                    .build();

        } catch (Exception e) {
            log.error("Failed to prepare video download", e);
            throw new FileProcessingException("영상 다운로드 준비에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileResourceDto prepareImageDownload(Long id) {
        log.debug("Preparing image download for id: {}", id);

        ImageContent image = imageRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ContentNotFoundException("이미지를 찾을 수 없습니다. ID: " + id));

        try {
            Resource resource = new FileSystemResource(image.getFilePath());
            if (!resource.exists()) {
                throw new ContentNotFoundException("이미지 파일이 존재하지 않습니다.");
            }

            return FileResourceDto.builder()
                    .resource(resource)
                    .fileName(image.getFileName())
                    .contentType(image.getContentType())
                    .fileSize(image.getFileSize())
                    .build();

        } catch (Exception e) {
            log.error("Failed to prepare image download", e);
            throw new FileProcessingException("이미지 다운로드 준비에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContentStatsDto getContentStatistics() {
        log.debug("Fetching content statistics");

        return contentRepositoryCustom.getContentStats();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentDto> getRecentContents(int size) {
        log.debug("Fetching recent contents, size: {}", size);

        Pageable pageable = PageRequest.of(0, size);
        return getAllContents(new ContentSearchDto(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentDto> getContentsByUser(String userId, Pageable pageable) {
        log.debug("Fetching contents by user: {}", userId);

        // 추후 사용자 기능 구현 시 활용
        // 현재는 전체 콘텐츠 반환
        return getAllContents(new ContentSearchDto(), pageable);
    }
}