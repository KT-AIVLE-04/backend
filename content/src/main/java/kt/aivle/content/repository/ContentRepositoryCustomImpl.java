// 구현체
package kt.aivle.content.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kt.aivle.content.domain.QImageContent;
import kt.aivle.content.domain.QVideoContent;
import kt.aivle.content.dto.common.ContentDto;
import kt.aivle.content.dto.common.ContentType;
import kt.aivle.content.dto.request.ContentSearchDto;
import kt.aivle.content.dto.response.ContentStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryCustomImpl implements ContentRepositoryCustom {

    private final kt.aivle.content.repository.JPAQueryFactory queryFactory;

    private static final QVideoContent video = QVideoContent.videoContent;
    private static final QImageContent image = QImageContent.imageContent;

    @Override
    public Page<ContentDto> findContentsWithFilter(ContentSearchDto searchDto, Pageable pageable) {
        List<ContentDto> results = new ArrayList<>();

        // 영상 조회
        if (searchDto.getContentType() == null || searchDto.getContentType() == ContentType.VIDEO ||
                searchDto.getContentType() == ContentType.ALL) {

            BooleanBuilder videoBuilder = new BooleanBuilder();
            videoBuilder.and(video.isDeleted.eq(false));

            if (searchDto.getIsAiGenerated() != null) {
                videoBuilder.and(video.isAiGenerated.eq(searchDto.getIsAiGenerated()));
            }

            if (searchDto.getStartDate() != null) {
                videoBuilder.and(video.createdDate.goe(searchDto.getStartDate().atStartOfDay()));
            }

            if (searchDto.getEndDate() != null) {
                videoBuilder.and(video.createdDate.loe(searchDto.getEndDate().atTime(23, 59, 59)));
            }

            List<ContentDto> videoResults = queryFactory
                    .select(Projections.constructor(ContentDto.class,
                            video.id,
                            video.title,
                            video.isAiGenerated,
                            video.createdDate,
                            video.fileSize,
                            video.thumbnailPath.as("previewPath"),
                            "VIDEO".as("contentType"),
                            video.videoFormat.as("format"),
                            video.resolution,
                            video.formattedFileSize,
                            video.duration,
                            video.isShorts,
                            null,
                            null))
                    .from(video)
                    .where(videoBuilder)
                    .orderBy(video.createdDate.desc())
                    .fetch();

            results.addAll(videoResults);
        }

        // 이미지 조회
        if (searchDto.getContentType() == null || searchDto.getContentType() == ContentType.IMAGE ||
                searchDto.getContentType() == ContentType.ALL) {

            BooleanBuilder imageBuilder = new BooleanBuilder();
            imageBuilder.and(image.isDeleted.eq(false));

            if (searchDto.getIsAiGenerated() != null) {
                imageBuilder.and(image.isAiGenerated.eq(searchDto.getIsAiGenerated()));
            }

            if (searchDto.getStartDate() != null) {
                imageBuilder.and(image.createdDate.goe(searchDto.getStartDate().atStartOfDay()));
            }

            if (searchDto.getEndDate() != null) {
                imageBuilder.and(image.createdDate.loe(searchDto.getEndDate().atTime(23, 59, 59)));
            }

            List<ContentDto> imageResults = queryFactory
                    .select(Projections.constructor(ContentDto.class,
                            image.id,
                            image.title,
                            image.isAiGenerated,
                            image.createdDate,
                            image.fileSize,
                            image.filePath.as("previewPath"),
                            "IMAGE".as("contentType"),
                            image.imageFormat.as("format"),
                            image.resolution,
                            image.formattedFileSize,
                            null,
                            null,
                            image.keywords,
                            image.isCompressed))
                    .from(image)
                    .where(imageBuilder)
                    .orderBy(image.createdDate.desc())
                    .fetch();

            results.addAll(imageResults);
        }

        // 결과 정렬 (생성일자 기준 내림차순)
        results.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));

        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());

        List<ContentDto> pagedResults = start >= results.size() ?
                new ArrayList<>() : results.subList(start, end);

        return new PageImpl<>(pagedResults, pageable, results.size());
    }

    @Override
    public Page<ContentDto> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable) {
        List<ContentDto> results = new ArrayList<>();

        // 영상에서 제목 검색
        List<ContentDto> videoResults = queryFactory
                .select(Projections.constructor(ContentDto.class,
                        video.id,
                        video.title,
                        video.isAiGenerated,
                        video.createdDate,
                        video.fileSize,
                        video.thumbnailPath.as("previewPath"),
                        "VIDEO".as("contentType"),
                        video.videoFormat.as("format"),
                        video.resolution,
                        video.formattedFileSize,
                        video.duration,
                        video.isShorts,
                        null,
                        null))
                .from(video)
                .where(video.isDeleted.eq(false)
                        .and(video.title.containsIgnoreCase(title)))
                .orderBy(video.createdDate.desc())
                .fetch();

        // 이미지에서 제목 검색
        List<ContentDto> imageResults = queryFactory
                .select(Projections.constructor(ContentDto.class,
                        image.id,
                        image.title,
                        image.isAiGenerated,
                        image.createdDate,
                        image.fileSize,
                        image.filePath.as("previewPath"),
                        "IMAGE".as("contentType"),
                        image.imageFormat.as("format"),
                        image.resolution,
                        image.formattedFileSize,
                        null,
                        null,
                        image.keywords,
                        image.isCompressed))
                .from(image)
                .where(image.isDeleted.eq(false)
                        .and(image.title.containsIgnoreCase(title)))
                .orderBy(image.createdDate.desc())
                .fetch();

        results.addAll(videoResults);
        results.addAll(imageResults);

        // 결과 정렬
        results.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));

        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());

        List<ContentDto> pagedResults = start >= results.size() ?
                new ArrayList<>() : results.subList(start, end);

        return new PageImpl<>(pagedResults, pageable, results.size());
    }

    @Override
    public ContentStatsDto getContentStats() {
        // 영상 통계
        Long totalVideos = queryFactory
                .select(video.count())
                .from(video)
                .where(video.isDeleted.eq(false))
                .fetchOne();

        Long aiGeneratedVideos = queryFactory
                .select(video.count())
                .from(video)
                .where(video.isDeleted.eq(false).and(video.isAiGenerated.eq(true)))
                .fetchOne();

        Long totalShorts = queryFactory
                .select(video.count())
                .from(video)
                .where(video.isDeleted.eq(false).and(video.isShorts.eq(true)))
                .fetchOne();

        // 이미지 통계
        Long totalImages = queryFactory
                .select(image.count())
                .from(image)
                .where(image.isDeleted.eq(false))
                .fetchOne();

        Long aiGeneratedImages = queryFactory
                .select(image.count())
                .from(image)
                .where(image.isDeleted.eq(false).and(image.isAiGenerated.eq(true)))
                .fetchOne();

        return ContentStatsDto.builder()
                .totalVideos(totalVideos != null ? totalVideos : 0L)
                .totalImages(totalImages != null ? totalImages : 0L)
                .aiGeneratedVideos(aiGeneratedVideos != null ? aiGeneratedVideos : 0L)
                .aiGeneratedImages(aiGeneratedImages != null ? aiGeneratedImages : 0L)
                .totalShorts(totalShorts != null ? totalShorts : 0L)
                .totalContents((totalVideos != null ? totalVideos : 0L) + (totalImages != null ? totalImages : 0L))
                .build();
    }

    @Override
    public Page<ContentDto> findPopularContents(Pageable pageable) {
        // 추후 다운로드/조회수 기능 구현 시 활용
        // 현재는 최신순으로 반환
        return findContentsWithFilter(new ContentSearchDto(), pageable);
    }
}