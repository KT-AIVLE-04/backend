package kt.aivle.shortsmvc.service;

import kt.aivle.common.exception.BusinessException;
import kt.aivle.shortsmvc.dto.CreateShortsRequest;
import kt.aivle.shortsmvc.dto.JobStatusResponse;
import kt.aivle.shortsmvc.model.JobStatus;
import kt.aivle.shortsmvc.storage.JobStore;
import kt.aivle.shortsmvc.storage.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static kt.aivle.common.code.CommonResponseCode.BAD_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsService {

    private final S3Service s3Service;
    private final JobStore jobStore;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public String startCreateShorts(CreateShortsRequest request, List<MultipartFile> images) {
        String jobId = request.sessionId();
        jobStore.init(jobId);
        
        // 비동기 처리 시작
        processCreateShortsAsync(request, images);
        
        return jobId;
    }

    @Async
    public void processCreateShortsAsync(CreateShortsRequest request, List<MultipartFile> images) {
        String jobId = request.sessionId();
        
        try {
            jobStore.start(jobId);
            
            // 1. S3에 이미지 업로드 (동기식)
            List<String> uploadedUrls = uploadImages(images);
            jobStore.updateProgress(jobId, 50);
            
            // 2. Mock AI 처리 (실제로는 빠른 처리)
            String resultKey = mockAiProcessing(request, uploadedUrls);
            jobStore.updateProgress(jobId, 90);
            
            // 3. 이벤트 발행 
            publishContentEvent(request, resultKey);
            
            // 4. 작업 완료
            jobStore.success(jobId, resultKey);
            
        } catch (Exception e) {
            log.error("Shorts creation failed for job {}: {}", jobId, e.getMessage(), e);
            jobStore.fail(jobId, e.getMessage());
        }
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(s3Service::uploadTempImage)
                .toList();
    }

    private String mockAiProcessing(CreateShortsRequest request, List<String> imageUrls) {
        // Mock AI 처리 - 실제로는 S3에 결과 파일 업로드
        try {
            Thread.sleep(2000); // AI 처리 시뮬레이션 (2초)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "shorts/result/" + request.sessionId() + ".mp4";
    }

    private void publishContentEvent(CreateShortsRequest request, String resultKey) {
        // 이벤트 발행
        var event = new ContentCreatedEvent(
                request.sessionId(),
                request.title(),
                request.content(),
                resultKey
        );
        
        kafkaTemplate.send("content.request", event);
    }

    public JobStatusResponse getJobStatus(String jobId) {
        JobStatus jobStatus = jobStore.find(jobId);
        if (jobStatus == null) {
            throw new BusinessException(BAD_REQUEST, "존재하지 않는 작업입니다.");
        }
        
        String downloadUrl = null;
        if (jobStatus.getStatus() == JobStatus.Status.SUCCEEDED && jobStatus.getResultKey() != null) {
            downloadUrl = s3Service.getPresignedUrl(jobStatus.getResultKey());
        }
        
        return new JobStatusResponse(
                jobStatus.getJobId(),
                jobStatus.getStatus().name(),
                jobStatus.getProgress(),
                downloadUrl,
                jobStatus.getResultKey(),
                jobStatus.getError()
        );
    }

    // Inner class for event
    public record ContentCreatedEvent(
            String sessionId,
            String title,
            String content,
            String resultKey
    ) {}
}