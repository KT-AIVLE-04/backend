# Analytics Service

YouTube 및 기타 SNS 플랫폼의 콘텐츠 분석을 담당하는 마이크로서비스입니다.

## 📋 목차

- [API 엔드포인트](#api-엔드포인트)
- [이벤트 흐름](#이벤트-흐름)
- [데이터베이스 스키마](#데이터베이스-스키마)
- [외부 API 연동](#외부-api-연동)
- [스케줄링 작업](#스케줄링-작업)
- [테스트 방법](#테스트-방법)
- [발생 가능한 문제점](#발생-가능한-문제점)

## 🚀 API 엔드포인트

### 메트릭 수집
```http
POST /api/analytics/collect-metrics
Headers: X-USER-ID: {userId}
Query: snsType={snsType}
```

**응답:**
```json
{
  "code": "OK",
  "message": "Success",
  "data": null
}
```

### 대시보드 통계
```http
GET /api/analytics/dashboard
Headers: X-USER-ID: {userId}
Body: {
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

**응답:**
```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "totalViews": 15000,
    "totalLikes": 1200,
    "totalComments": 300,
    "averageEngagementRate": 8.5,
    "totalVideos": 25,
    "growthRate": 15.2
  }
}
```

### 비디오 메트릭
```http
GET /api/analytics/video-metrics
Headers: X-USER-ID: {userId}
Body: {
  "socialPostId": 123,
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

**응답:**
```json
{
  "code": "OK",
  "message": "Success",
  "data": [
    {
      "socialPostId": 123,
      "userId": "user123",
      "snsType": "YOUTUBE",
      "metricDate": "2024-01-15T00:00:00",
      "viewCount": 5000,
      "likeCount": 400,
      "commentCount": 50,
      "shareCount": 25,
      "subscriberCount": 1000,
      "engagementRate": 9.5
    }
  ]
}
```

### 인기 콘텐츠
```http
GET /api/analytics/top-content?limit=10
Headers: X-USER-ID: {userId}
```

### 감정 분석
```http
POST /api/analytics/analyze-sentiment?videoId={videoId}
Headers: X-USER-ID: {userId}
```

**응답:**
```json
{
  "code": "OK",
  "message": "Success",
  "data": {
    "analysisId": 456,
    "userId": "user123",
    "videoId": "video123",
    "snsType": "YOUTUBE",
    "analysisType": "SENTIMENT_ANALYSIS",
    "periodStart": "2024-01-15T10:00:00",
    "periodEnd": "2024-01-15T10:00:00",
    "score": 0.75,
    "summary": "긍정적인 반응이 우세합니다",
    "detailedAnalysis": "{\"positive\": 0.75, \"negative\": 0.15}",
    "recommendations": null
  }
}
```

### 트렌드 분석
```http
POST /api/analytics/analyze-trends
Headers: X-USER-ID: {userId}
Body: {
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "analysisType": "TREND_ANALYSIS"
}
```

### 최적 게시 시간 분석
```http
POST /api/analytics/analyze-optimal-time
Headers: X-USER-ID: {userId}
```

### 리포트 생성
```http
POST /api/analytics/generate-report
Headers: X-USER-ID: {userId}
Body: {
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "analysisType": "CONTENT_PERFORMANCE"
}
```

### 토큰 갱신
```http
POST /api/analytics/refresh-token?snsType={snsType}
Headers: X-USER-ID: {userId}
```

## 🔄 이벤트 흐름

### 헥사고날 아키텍처 기반 이벤트 구조

analytics-service는 헥사고날 아키텍처를 기반으로 이벤트를 다음과 같이 구성합니다:

```
adapter/
├── in/                    # Inbound Adapters (받는 것)
│   ├── event/
│   │   ├── dto/          # 수신 이벤트 DTO
│   │   │   └── SocialPostResponseEvent.java
│   │   └── consumer/     # 이벤트 소비자
│   │       └── SocialPostResponseEventConsumer.java
│   └── web/              # HTTP 요청 처리
└── out/                   # Outbound Adapters (보내는 것)
    ├── event/
    │   ├── dto/          # 발송 이벤트 DTO
    │   │   ├── SnsTokenRequestEvent.java
    │   │   └── SocialPostRequestEvent.java
    │   ├── producer/     # 이벤트 발송자
    │   │   ├── SnsTokenEventProducer.java
    │   │   └── SocialPostEventProducer.java
    │   └── SnsTokenResponseEvent.java
    ├── persistence/      # 데이터베이스 접근
    └── external/         # 외부 API 호출
```

### 수신 이벤트 (Inbound)

#### 1. 소셜 포스트 응답 이벤트
- **토픽**: `social-post.response`
- **소비자**: `SocialPostResponseEventConsumer`
- **처리**: `AnalyticsEventService.handleSocialPostResponse()`
- **목적**: SNS 서비스로부터 게시글 정보 수신 후 메트릭 수집

```json
{
  "requestId": "req_1234567890",
  "userId": "user123",
  "snsType": "youtube",
  "posts": [
    {
      "id": 123,
      "socialAccountId": 456,
      "postId": 789,
      "snsPostId": "video123",
      "status": "PUBLISHED",
      "postedAt": "2024-01-15T10:00:00",
      "title": "비디오 제목",
      "description": "비디오 설명",
      "thumbnailUrl": "https://..."
    }
  ]
}
```

#### 2. SNS 토큰 응답 이벤트
- **토픽**: `sns-token.response`
- **소비자**: `SnsTokenResponseEventConsumer` (구현 예정)
- **처리**: `AnalyticsEventService.handleSnsTokenResponse()`
- **목적**: SNS 서비스로부터 토큰 정보 수신

```json
{
  "requestId": "req_1234567890",
  "userId": "user123",
  "snsType": "youtube",
  "accessToken": "ya29.a0AfB_byC...",
  "refreshToken": "1//04dX...",
  "expiresAt": 1703123456789,
  "isExpired": false
}
```

### 발송 이벤트 (Outbound)

#### 1. SNS 토큰 요청 이벤트
- **토픽**: `sns-token.request`
- **발송자**: `SnsTokenEventProducer`
- **목적**: SNS 서비스에 토큰 정보 요청

```json
{
  "requestId": "req_1234567890",
  "userId": "user123",
  "snsType": "youtube"
}
```

#### 2. 소셜 포스트 요청 이벤트
- **토픽**: `social-post.request`
- **발송자**: `SocialPostEventProducer`
- **목적**: SNS 서비스에 게시글 정보 요청

```json
{
  "requestId": "req_1234567890",
  "userId": "user123",
  "snsType": "youtube",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

### 이벤트 흐름 시나리오

#### 1. 메트릭 수집 흐름
```
1. analytics-service → sns-service: social-post.request
2. sns-service → analytics-service: social-post.response
3. analytics-service → sns-service: sns-token.request
4. sns-service → analytics-service: sns-token.response
5. analytics-service: YouTube API 호출하여 메트릭 수집
6. analytics-service: DB에 메트릭 저장
```

#### 2. 토큰 갱신 흐름
```
1. analytics-service → sns-service: sns-token.request
2. sns-service → analytics-service: sns-token.response
3. analytics-service: 새로운 토큰으로 API 호출
```

## 🗄️ 데이터베이스 스키마

### PostMetric (게시글 메트릭)
```sql
CREATE TABLE post_metric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    social_post_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    sns_type VARCHAR(50) NOT NULL,
    metric_date DATE NOT NULL,
    view_count BIGINT,
    like_count BIGINT,
    comment_count BIGINT,
    share_count BIGINT,
    subscriber_count BIGINT,
    engagement_rate DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Comment (댓글)
```sql
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    social_post_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    sns_type VARCHAR(50) NOT NULL,
    content TEXT,
    crawled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### AnalysisResult (분석 결과)
```sql
CREATE TABLE analysis_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    video_id VARCHAR(255),
    sns_type VARCHAR(50) NOT NULL,
    analysis_type VARCHAR(50) NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    score DOUBLE,
    summary TEXT,
    detailed_analysis TEXT,
    recommendations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 🌐 외부 API 연동

### YouTube API

#### YouTube Analytics API
- **URL**: `https://youtubeanalytics.googleapis.com/v2/reports`
- **인증**: Bearer Token (OAuth 2.0)
- **응답 형식**:
```json
{
  "rows": [
    ["videoId", "views", "likes", "shares", "comments"]
  ]
}
```

#### YouTube Data API v3 (Fallback)
- **URL**: `https://www.googleapis.com/youtube/v3/videos`
- **인증**: Bearer Token (OAuth 2.0)
- **응답 형식**:
```json
{
  "items": [
    {
      "statistics": {
        "viewCount": "5000",
        "likeCount": "400",
        "commentCount": "50"
      }
    }
  ]
}
```

#### YouTube Comments API
- **URL**: `https://www.googleapis.com/youtube/v3/commentThreads`
- **인증**: Bearer Token (OAuth 2.0)
- **응답 형식**:
```json
{
  "items": [
    {
      "snippet": {
        "topLevelComment": {
          "id": "commentId",
          "snippet": {
            "textDisplay": "댓글 내용",
            "authorDisplayName": "작성자",
            "publishedAt": "2024-01-15T10:00:00Z"
          }
        }
      }
    }
  ]
}
```

#### 사양 근거 및 주의사항
- YouTube Analytics API 타겟 쿼리: `ids=channel==MINE`, `dimensions=video`, `filters=video=={VIDEO_ID}`, `metrics=views,likes,shares,comments` 형식 지원. 응답은 `columnHeaders` 순서로 `rows`에 [dimensions..., metrics...] 배열이 옴. 참고: [YouTube Analytics API 개요/타겟팅된 쿼리](https://developers.google.com/youtube/analytics?hl=ko)
- YouTube Data API v3 통계: `videos.list?part=statistics&id=...`로 `statistics.viewCount`, `statistics.likeCount`, `statistics.commentCount` 조회. 일부 설정에 따라 필드가 누락될 수 있음(예: 댓글 비활성화 시). 참고: [YouTube Data API v3](https://developers.google.com/youtube/v3)
- 댓글 스레드 구조: 최상위 댓글 ID는 `items[].snippet.topLevelComment.id`, 텍스트는 `items[].snippet.topLevelComment.snippet.textDisplay`. 본문 예시는 이에 맞춰 수정됨. 참고: [YouTube Data API v3](https://developers.google.com/youtube/v3)
- 채널 정보 `channels.list?part=...&mine=true`는 소유 채널 접근으로 OAuth 2.0 필요. API 키만으로는 불가. 참고: [YouTube Data API v3](https://developers.google.com/youtube/v3)

### 예시
```
GET https://youtubeanalytics.googleapis.com/v2/reports
  ?ids=channel==MINE
  &startDate=2024-01-01
  &endDate=2024-01-31
  &metrics=views,likes,shares,comments
  &dimensions=video
  &filters=video=={VIDEO_ID}
```
### AI 서버 API

#### 감정 분석
- **URL**: `${AI_SERVER_URL}/analyze/sentiment`
- **메서드**: POST
- **요청**:
```json
{
  "videoId": "video123",
  "comments": ["댓글1", "댓글2", "댓글3"]
}
```
- **응답**:
```json
{
  "positiveScore": 0.75,
  "negativeScore": 0.15,
  "neutralScore": 0.10,
  "summary": "긍정적인 반응이 우세합니다"
}
```

#### 트렌드 분석
- **URL**: `${AI_SERVER_URL}/analyze/trends`
- **메서드**: POST
- **요청**:
```json
{
  "userId": "user123",
  "metrics": {
    "totalViews": 15000,
    "totalLikes": 1200,
    "averageEngagement": 8.5
  }
}
```
- **응답**:
```json
{
  "trend": "상승 추세",
  "confidence": 0.85,
  "recommendation": "콘텐츠 품질을 유지하세요"
}
```

#### 최적 게시 시간 분석
- **URL**: `${AI_SERVER_URL}/analyze/optimal-time`
- **메서드**: POST
- **요청**:
```json
{
  "userId": "user123",
  "postingData": {
    "metrics": [...],
    "userId": "user123"
  }
}
```
- **응답**:
```json
{
  "optimalDay": "월요일",
  "optimalTime": "18:00",
  "expectedEngagement": 12.5
}
```

## ⏰ 스케줄링 작업

### 현재 구현: 스케줄러 사용
```java
@Scheduled(cron = "0 0 0 * * *") // 매일 자정
public void scheduledMetricsCollection() {
    log.info("스케줄된 메트릭 수집 시작");
    // 모든 사용자의 토큰을 조회하여 메트릭 수집
}
```

### 대안: Spring Batch 구현

#### 1. 의존성 추가 (build.gradle)
```gradle
// Spring Batch
implementation 'org.springframework.boot:spring-boot-starter-batch'
implementation 'org.springframework.boot:spring-boot-starter-quartz'

// 배치 모니터링
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

#### 2. 배치 설정
```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public JobBuilderFactory jobBuilderFactory(JobRepository jobRepository) {
        return new JobBuilderFactory(jobRepository);
    }
    
    @Bean
    public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository, 
                                               PlatformTransactionManager transactionManager) {
        return new StepBuilderFactory(jobRepository, transactionManager);
    }
}
```

#### 3. 메트릭 수집 배치 Job
```java
@Component
@RequiredArgsConstructor
public class MetricsCollectionJobConfig {
    
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ExternalApiPort externalApiPort;
    private final PostMetricRepositoryPort postMetricRepositoryPort;
    
    @Bean
    public Job metricsCollectionJob() {
        return jobBuilderFactory.get("metricsCollectionJob")
            .start(collectMetricsStep())
            .next(analyzeTrendsStep())
            .build();
    }
    
    @Bean
    public Step collectMetricsStep() {
        return stepBuilderFactory.get("collectMetricsStep")
            .<UserToken, PostMetric>chunk(100) // 청크 크기
            .reader(userTokenReader())
            .processor(metricsProcessor())
            .writer(metricsWriter())
            .faultTolerant()
            .retry(Exception.class)
            .retryLimit(3)
            .listener(new MetricsCollectionStepListener())
            .build();
    }
    
    @Bean
    public Step analyzeTrendsStep() {
        return stepBuilderFactory.get("analyzeTrendsStep")
            .<PostMetric, AnalysisResult>chunk(50)
            .reader(metricsReader())
            .processor(trendsProcessor())
            .writer(analysisWriter())
            .build();
    }
    
    @Bean
    public ItemReader<UserToken> userTokenReader() {
        return new JpaPagingItemReaderBuilder<UserToken>()
            .name("userTokenReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT ut FROM UserToken ut WHERE ut.snsType = 'YOUTUBE'")
            .pageSize(100)
            .build();
    }
    
    @Bean
    public ItemProcessor<UserToken, PostMetric> metricsProcessor() {
        return userToken -> {
            try {
                // YouTube API 호출하여 메트릭 수집
                List<VideoData> videos = externalApiPort.getYouTubeVideos(
                    userToken.getAccessToken(), userToken.getChannelId());
                
                List<PostMetric> metrics = new ArrayList<>();
                for (VideoData video : videos) {
                    VideoMetrics videoMetrics = externalApiPort.getYouTubeVideoMetrics(
                        userToken.getAccessToken(), video.videoId());
                    
                    PostMetric metric = PostMetric.builder()
                        .socialPostId(Long.parseLong(video.videoId()))
                        .userId(userToken.getUserId())
                        .snsType(SnsType.YOUTUBE)
                        .metricDate(LocalDate.now())
                        .viewCount(videoMetrics.viewCount())
                        .likeCount(videoMetrics.likeCount())
                        .commentCount(videoMetrics.commentCount())
                        .shareCount(videoMetrics.shareCount())
                        .build();
                    
                    metrics.add(metric);
                }
                
                return metrics;
            } catch (Exception e) {
                log.error("메트릭 수집 실패: userId={}", userToken.getUserId(), e);
                return null; // Skip 처리
            }
        };
    }
    
    @Bean
    public ItemWriter<PostMetric> metricsWriter() {
        return items -> {
            for (PostMetric metric : items) {
                if (metric != null) {
                    postMetricRepositoryPort.save(metric);
                }
            }
        };
    }
}
```

#### 4. 배치 스케줄링 (Quartz 사용)
```java
@Configuration
public class QuartzConfig {
    
    @Bean
    public JobDetail metricsCollectionJobDetail() {
        return JobBuilder.newJob(QuartzJobBean.class)
            .withIdentity("metricsCollectionJob")
            .storeDurably()
            .build();
    }
    
    @Bean
    public Trigger metricsCollectionTrigger() {
        return TriggerBuilder.newTrigger()
            .forJob(metricsCollectionJobDetail())
            .withIdentity("metricsCollectionTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")) // 매일 자정
            .build();
    }
}
```

#### 5. 배치 모니터링
```java
@Component
public class MetricsCollectionStepListener implements StepExecutionListener {
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("메트릭 수집 배치 시작: {}", stepExecution.getJobExecution().getJobId());
    }
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("메트릭 수집 배치 완료: 처리된 항목={}, 실패={}", 
                stepExecution.getReadCount(), stepExecution.getSkipCount());
        return ExitStatus.COMPLETED;
    }
}
```

#### 6. 배치 실행 API
```java
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {
    
    private final JobLauncher jobLauncher;
    private final Job metricsCollectionJob;
    
    @PostMapping("/metrics-collection/start")
    public ResponseEntity<String> startMetricsCollection() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            
            JobExecution execution = jobLauncher.run(metricsCollectionJob, params);
            return ResponseEntity.ok("배치 시작됨: " + execution.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("배치 실행 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/metrics-collection/status/{jobId}")
    public ResponseEntity<JobExecution> getJobStatus(@PathVariable Long jobId) {
        // 배치 상태 조회 로직
        return ResponseEntity.ok(jobExecution);
    }
}
```

#### 배치 vs 스케줄러 비교

| 구분 | 스케줄러 | 배치 |
|------|----------|------|
| **메모리 효율성** | 전체 데이터 로드 | 청크별 처리 |
| **장애 복구** | 재시작 시 처음부터 | 중단 지점부터 재시작 |
| **모니터링** | 로그 기반 | 배치 메타데이터 |
| **확장성** | 단일 프로세스 | 멀티 스레드 지원 |
| **복잡도** | 간단 | 복잡 |
| **적합한 상황** | 소규모 데이터 | 대량 데이터 |

#### 배치 사용 권장 상황
- 사용자 수가 1000명 이상
- 일일 처리할 메트릭이 10만개 이상
- 장애 복구 기능이 중요할 때
- 상세한 모니터링이 필요할 때

## 🧪 테스트 방법

### 1. 이벤트 테스트 (Kafka)

#### SNS 토큰 요청 이벤트 테스트
```bash
# Kafka 토픽에 메시지 발송
kafka-console-producer --broker-list localhost:9092 --topic sns-token.request
```

메시지:
```json
{
  "requestId": "test_req_123",
  "userId": "test_user",
  "snsType": "youtube"
}
```

#### 소셜 포스트 응답 이벤트 테스트
```bash
kafka-console-producer --broker-list localhost:9092 --topic social-post.response
```

메시지:
```json
{
  "requestId": "test_req_123",
  "userId": "test_user",
  "snsType": "youtube",
  "posts": [
    {
      "id": 123,
      "socialAccountId": 456,
      "postId": 789,
      "snsPostId": "test_video_123",
      "status": "PUBLISHED",
      "postedAt": "2024-01-15T10:00:00",
      "title": "테스트 비디오",
      "description": "테스트 설명",
      "thumbnailUrl": "https://test.com/thumbnail.jpg"
    }
  ]
}
```

### 2. API 테스트

#### 메트릭 수집 테스트
```bash
curl -X POST "http://localhost:8084/api/analytics/collect-metrics?snsType=youtube" \
  -H "X-USER-ID: test_user"
```

#### 대시보드 통계 테스트
```bash
curl -X GET "http://localhost:8084/api/analytics/dashboard" \
  -H "X-USER-ID: test_user" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
  }'
```

### 3. 외부 API 모킹

#### YouTube API 모킹
```java
@MockBean
private ExternalApiPort externalApiPort;

@Test
void testYouTubeMetricsCollection() {
    // Given
    when(externalApiPort.getYouTubeVideoMetrics(anyString(), anyString()))
        .thenReturn(new VideoMetrics(1000L, 100L, 10L, 5L));
    
    // When & Then
    // 테스트 로직
}
```

#### AI 서버 모킹
```java
@Test
void testSentimentAnalysis() {
    // Given
    when(externalApiPort.analyzeSentiment(anyString(), anyList()))
        .thenReturn(new SentimentAnalysisResult(0.75, 0.15, 0.10, "긍정적"));
    
    // When & Then
    // 테스트 로직
}
```

## ⚠️ 발생 가능한 문제점

### 1. 토큰 관리 문제
- **문제**: SNS 토큰이 만료되어 API 호출 실패
- **해결책**: 
  - 토큰 갱신 이벤트 처리
  - 토큰 만료 시 자동 갱신 로직
  - 토큰 상태 모니터링

### 2. YouTube API 할당량 초과
- **문제**: YouTube API 일일 할당량 초과
- **해결책**:
  - API 호출 빈도 제한
  - 캐싱 전략 적용
  - 중요도에 따른 API 호출 우선순위 설정

### 3. AI 서버 연결 실패
- **문제**: AI 서버가 다운되어 분석 기능 불가
- **해결책**:
  - Circuit Breaker 패턴 적용
  - Fallback 로직 구현
  - 재시도 메커니즘

### 4. 데이터베이스 성능 문제
- **문제**: 대량 데이터 처리 시 성능 저하
- **해결책**:
  - 인덱스 최적화
  - 배치 처리
  - 데이터 파티셔닝

### 5. 이벤트 처리 실패
- **문제**: Kafka 이벤트 처리 중 예외 발생
- **해결책**:
  - Dead Letter Queue 설정
  - 재시도 정책
  - 이벤트 순서 보장

### 6. 메모리 누수
- **문제**: 대량 데이터 처리 시 메모리 부족
- **해결책**:
  - 스트림 처리 활용
  - 배치 크기 조정
  - GC 튜닝

## 🔧 환경 설정

### 필수 환경 변수
```bash
# YouTube API
YOUTUBE_API_KEY=your-youtube-api-key

# AI 서버
AI_SERVER_URL=http://localhost:8085

# 데이터베이스
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/userstore
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=as0201

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### 애플리케이션 설정
```yaml
server:
  port: 8084

spring:
  application:
    name: analytics-service
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://mysql:3306/userstore?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: as0201
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
        format_sql: true
        use_sql_comments: true
        default_batch_fetch_size: 1000
  
  kafka:
    bootstrap-servers: kafka:9092

app:
  youtube:
    api:
      key: ${YOUTUBE_API_KEY:your-youtube-api-key}
  ai-server:
    url: ${AI_SERVER_URL:http://localhost:8085}
```

## 📚 참고 자료

- [YouTube Analytics API 문서](https://developers.google.com/youtube/analytics/channel_reports?hl=ko)
- [YouTube Data API v3 문서](https://developers.google.com/youtube/v3/docs/videos#resource)
- [Spring Kafka 문서](https://spring.io/projects/spring-kafka)
- [Spring Boot Scheduling 문서](https://spring.io/guides/gs/scheduling-tasks/)
