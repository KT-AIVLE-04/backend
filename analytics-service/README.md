# Analytics Service

**분석 및 AI 기반 인사이트 서비스**

SNS 계정과 게시물의 성과를 분석하고, AI 기반 감정 분석을 통해 인사이트를 제공하는 서비스입니다. 실시간 분석과 배치 처리를 통한 히스토리컬 데이터 분석, YouTube API를 활용한 메트릭 수집 등의 기능을 제공합니다.

## 📋 개요

- **포트**: 8086
- **주요 기능**: 실시간 분석, 배치 데이터 처리, AI 감정 분석, 성과 메트릭 수집
- **프레임워크**: Spring Boot, Spring Batch, YouTube Data API, Redis

## 🔧 주요 기능

### 1. 실시간 분석
- **계정 메트릭**: 구독자, 총 조회수, 게시물 수 실시간 추적
- **게시물 성과**: 개별 게시물의 조회수, 좋아요, 댓글 수 모니터링
- **트렌드 분석**: 시간대별 성과 변화 추적
- **Redis 캐싱**: 빠른 데이터 조회를 위한 다층 캐싱

### 2. 배치 데이터 처리
- **Spring Batch**: 대용량 데이터 처리를 위한 배치 작업
- **일일 통계**: 매일 자동 실행되는 메트릭 수집 작업
- **히스토리컬 데이터**: 과거 데이터 분석 및 트렌드 파악
- **ETL 프로세스**: YouTube API에서 데이터 추출, 변환, 적재

### 3. AI 감정 분석
- **댓글 감정 분석**: AI 기반 댓글 감정 상태 분석
- **키워드 추출**: 댓글에서 주요 키워드 자동 추출
- **감정 통계**: 긍정/부정/중립 감정 비율 분석
- **FastAPI 통합**: 외부 AI 서비스와의 연동

### 4. 성과 리포팅
- **종합 리포트**: 계정 전체 성과 종합 분석
- **비교 분석**: 기간별, 게시물별 성과 비교
- **인사이트 제공**: AI 기반 개선 제안사항
- **시각화 데이터**: 차트 및 그래프용 데이터 제공

### 5. 이벤트 기반 데이터 수집
- **Kafka 통합**: SNS 서비스로부터 실시간 이벤트 수신
- **자동 동기화**: 계정 및 게시물 변경사항 자동 반영
- **이벤트 소싱**: 모든 변경사항의 히스토리 관리

## 🏗️ 아키텍처

### 헥사고날 아키텍처 구조
```
├── domain/                    # 도메인 계층
│   └── entity/               # 도메인 엔티티
│       ├── SnsAccount.java   # SNS 계정
│       ├── SnsPost.java      # SNS 게시물
│       ├── SnsAccountMetric.java # 계정 메트릭
│       ├── SnsPostMetric.java    # 게시물 메트릭
│       ├── SnsPostCommentMetric.java # 댓글 메트릭
│       ├── PostCommentKeyword.java   # 댓글 키워드
│       └── BaseEntity.java   # 기본 엔티티
├── application/               # 애플리케이션 계층
│   ├── port/in/              # 인바운드 포트
│   │   ├── AnalyticsQueryUseCase.java    # 분석 조회
│   │   ├── AnalyticsEventUseCase.java    # 이벤트 처리
│   │   ├── EmotionAnalysisUseCase.java   # 감정 분석
│   │   ├── MetricsCollectionUseCase.java # 메트릭 수집
│   │   └── dto/              # 애플리케이션 DTO
│   ├── port/out/             # 아웃바운드 포트
│   │   ├── repository/       # 리포지토리 포트
│   │   └── infrastructure/   # 인프라 포트
│   │       ├── AiAnalysisPort.java      # AI 분석 포트
│   │       ├── ExternalApiPort.java     # 외부 API 포트
│   │       └── ValidationPort.java      # 검증 포트
│   └── service/              # 애플리케이션 서비스
│       ├── AnalyticsQueryService.java   # 분석 조회 서비스
│       ├── AnalyticsEventService.java   # 이벤트 처리 서비스
│       ├── EmotionAnalysisService.java  # 감정 분석 서비스
│       ├── MetricsCollectionService.java # 메트릭 수집 서비스
│       ├── BatchJobMonitor.java         # 배치 작업 모니터
│       ├── BatchScheduler.java          # 배치 스케줄러
│       └── EmotionAnalysisBatchService.java # 감정 분석 배치
└── adapter/                   # 어댑터 계층
    ├── in/                   # 인바운드 어댑터
    │   ├── web/              # 웹 어댑터
    │   │   ├── RealtimeAnalyticsController.java
    │   │   ├── HistoricalAnalyticsController.java
    │   │   ├── BatchController.java
    │   │   └── dto/response/ # 응답 DTO
    │   └── event/consumer/   # 이벤트 컨슈머
    │       ├── PostEventConsumer.java
    │       └── SnsAccountEventConsumer.java
    └── out/                  # 아웃바운드 어댑터
        ├── persistence/      # 데이터베이스 어댑터
        │   └── repository/   # JPA 리포지토리
        └── infrastructure/   # 인프라 어댑터
            ├── AiAnalysisAdapter.java       # AI 분석 어댑터
            ├── YouTubeApiAdapter.java       # YouTube API 어댑터
            └── AnalyticsValidationAdapter.java # 검증 어댑터
```

### Spring Batch 구조
```
├── config/
│   ├── BatchConfig.java      # 배치 설정
│   └── AsyncConfig.java      # 비동기 처리 설정
├── job/
│   ├── MetricsCollectionJob.java     # 메트릭 수집 작업
│   ├── EmotionAnalysisJob.java       # 감정 분석 작업
│   └── HistoricalDataJob.java        # 히스토리컬 데이터 작업
└── step/
    ├── AccountMetricsStep.java       # 계정 메트릭 스텝
    ├── PostMetricsStep.java          # 게시물 메트릭 스텝
    └── CommentAnalysisStep.java      # 댓글 분석 스텝
```

## 🗄️ 데이터베이스 스키마

### SnsAccountMetric 테이블
```sql
CREATE TABLE sns_account_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sns_account_id BIGINT NOT NULL,
    follower_count BIGINT DEFAULT 0,
    following_count BIGINT DEFAULT 0,
    total_view_count BIGINT DEFAULT 0,
    total_post_count INT DEFAULT 0,
    engagement_rate DECIMAL(5,2) DEFAULT 0.00,
    growth_rate DECIMAL(5,2) DEFAULT 0.00,
    metric_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_account_date (sns_account_id, metric_date),
    INDEX idx_metric_date (metric_date)
);
```

### SnsPostMetric 테이블
```sql
CREATE TABLE sns_post_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sns_post_id BIGINT NOT NULL,
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    dislike_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,
    share_count BIGINT DEFAULT 0,
    engagement_rate DECIMAL(5,2) DEFAULT 0.00,
    watch_time_seconds BIGINT DEFAULT 0,
    metric_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_post_date (sns_post_id, metric_date),
    INDEX idx_metric_date (metric_date)
);
```

### SnsPostCommentMetric 테이블
```sql
CREATE TABLE sns_post_comment_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sns_post_id BIGINT NOT NULL,
    total_comments INT DEFAULT 0,
    positive_sentiment_count INT DEFAULT 0,
    negative_sentiment_count INT DEFAULT 0,
    neutral_sentiment_count INT DEFAULT 0,
    positive_ratio DECIMAL(5,2) DEFAULT 0.00,
    negative_ratio DECIMAL(5,2) DEFAULT 0.00,
    average_sentiment_score DECIMAL(3,2) DEFAULT 0.00,
    metric_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_post_comment_date (sns_post_id, metric_date)
);
```

### PostCommentKeyword 테이블
```sql
CREATE TABLE post_comment_keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sns_post_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    frequency INT DEFAULT 1,
    sentiment VARCHAR(20) DEFAULT 'NEUTRAL',
    metric_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_keyword (sns_post_id, keyword),
    INDEX idx_keyword_frequency (keyword, frequency DESC)
);
```

## ⚙️ API 엔드포인트

### 실시간 분석 API

#### 계정 메트릭 조회
```http
GET /api/analytics/realtime/accounts/{accountId}/metrics?period=7d
Authorization: Bearer {token}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "실시간 계정 메트릭 조회 성공",
    "data": {
        "accountId": 1,
        "accountName": "내 유튜브 채널",
        "currentMetrics": {
            "followerCount": 1520,
            "totalViewCount": 52000,
            "totalPostCount": 28,
            "engagementRate": 3.45,
            "growthRate": 2.1
        },
        "periodComparison": {
            "followerGrowth": "+20",
            "viewGrowth": "+1500",
            "postGrowth": "+3",
            "engagementChange": "+0.25"
        },
        "trendData": [
            {
                "date": "2024-01-01",
                "followers": 1500,
                "views": 50500,
                "engagement": 3.2
            }
        ]
    }
}
```

#### 게시물 성과 분석
```http
GET /api/analytics/realtime/posts/{postId}/metrics
Authorization: Bearer {token}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "게시물 성과 분석 조회 성공",
    "data": {
        "postId": 1,
        "title": "우리 매장 소개 영상",
        "currentMetrics": {
            "viewCount": 3200,
            "likeCount": 145,
            "commentCount": 23,
            "shareCount": 12,
            "engagementRate": 5.62,
            "watchTimeSeconds": 1800
        },
        "performanceAnalysis": {
            "ranking": "상위 20%",
            "peakViewTime": "2024-01-01T15:30:00",
            "audienceRetention": 78.5,
            "clickThroughRate": 4.2
        }
    }
}
```

### 히스토리컬 분석 API

#### 계정 히스토리컬 데이터
```http
GET /api/analytics/historical/accounts/{accountId}?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {token}
```

#### 트렌드 분석
```http
GET /api/analytics/historical/trends?accountId=1&metric=engagement&period=30d
Authorization: Bearer {token}
```

### 감정 분석 API

#### 게시물 댓글 감정 분석
```http
GET /api/analytics/emotion/posts/{postId}/comments
Authorization: Bearer {token}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "댓글 감정 분석 결과",
    "data": {
        "postId": 1,
        "totalComments": 150,
        "sentimentDistribution": {
            "positive": 85,
            "negative": 25,
            "neutral": 40
        },
        "sentimentRatios": {
            "positiveRatio": 56.67,
            "negativeRatio": 16.67,
            "neutralRatio": 26.67
        },
        "averageSentimentScore": 0.65,
        "topKeywords": [
            {
                "keyword": "맛있다",
                "frequency": 12,
                "sentiment": "POSITIVE"
            },
            {
                "keyword": "좋다",
                "frequency": 8,
                "sentiment": "POSITIVE"
            },
            {
                "keyword": "비싸다",
                "frequency": 5,
                "sentiment": "NEGATIVE"
            }
        ]
    }
}
```

### 리포트 API

#### 종합 리포트 생성
```http
POST /api/analytics/reports/comprehensive
Authorization: Bearer {token}
Content-Type: application/json

{
    "accountIds": [1, 2],
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "includeEmotionAnalysis": true,
    "includeTrendAnalysis": true
}
```

### 배치 작업 관리 API

#### 배치 작업 상태 조회
```http
GET /api/analytics/batch/jobs/{jobId}/status
Authorization: Bearer {token}
```

#### 수동 배치 작업 실행
```http
POST /api/analytics/batch/jobs/metrics-collection/trigger
Authorization: Bearer {token}
```

## 🤖 AI 감정 분석 통합

### FastAPI 감정 분석 서비스 통신

#### 댓글 감정 분석 요청
```json
POST http://localhost:8000/api/ai/emotion/analyze
{
    "texts": [
        "정말 맛있어요! 또 가고 싶습니다.",
        "가격이 좀 비싼 것 같아요",
        "서비스가 친절했습니다"
    ],
    "language": "ko"
}
```

**응답:**
```json
{
    "results": [
        {
            "text": "정말 맛있어요! 또 가고 싶습니다.",
            "sentiment": "POSITIVE",
            "confidence": 0.92,
            "score": 0.85
        },
        {
            "text": "가격이 좀 비싼 것 같아요",
            "sentiment": "NEGATIVE",
            "confidence": 0.78,
            "score": -0.45
        },
        {
            "text": "서비스가 친절했습니다",
            "sentiment": "POSITIVE",
            "confidence": 0.88,
            "score": 0.67
        }
    ]
}
```

#### 키워드 추출 요청
```json
POST http://localhost:8000/api/ai/keywords/extract
{
    "texts": [
        "맛있는 음식과 친절한 서비스가 인상적이었습니다",
        "분위기가 좋고 가격도 합리적이에요"
    ],
    "language": "ko",
    "maxKeywords": 10
}
```

## 📊 Spring Batch 작업

### 일일 메트릭 수집 작업

#### Job 설정
```java
@Configuration
public class MetricsCollectionJobConfig {
    
    @Bean
    public Job metricsCollectionJob() {
        return jobBuilderFactory.get("metricsCollectionJob")
            .start(accountMetricsStep())
            .next(postMetricsStep())
            .next(commentAnalysisStep())
            .build();
    }
    
    // 매일 새벽 2시 실행
    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyMetricsCollection() {
        jobLauncher.run(metricsCollectionJob(), new JobParameters());
    }
}
```

### 배치 작업 모니터링
```java
@Component
public class BatchJobMonitor {
    
    @EventListener
    public void handleJobExecution(JobExecutionEvent event) {
        // 작업 실행 상태 모니터링
        // 실패 시 알림 발송
        // 메트릭 수집
    }
}
```

## ⚙️ 설정

### Redis 캐싱 설정
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      timeout: 2000ms
  cache:
    type: redis
    cache-names:
      - analytics-realtime
      - analytics-historical
      - emotion-analysis
    redis:
      time-to-live: 3600000  # 1 hour
```

### Spring Batch 설정
```yaml
spring:
  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false  # 수동 실행
  task:
    execution:
      pool:
        core-size: 10
        max-size: 20
        queue-capacity: 100
```

### YouTube API 설정
```yaml
youtube:
  api:
    key: ${YOUTUBE_API_KEY}
    quota:
      daily-limit: 10000
      analytics-calls: 2000
```

### AI 서비스 설정
```yaml
ai:
  service:
    emotion-analysis:
      url: ${AI_SERVICE_URL:http://localhost:8000}
      timeout: 30s
      batch-size: 100
```

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- MySQL 8.0+
- Redis 7.2+
- Kafka
- YouTube Data API 키
- FastAPI AI Service

### AI 서비스 실행
```bash
# FastAPI AI 서비스 실행
cd ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

### Analytics Service 실행
```bash
# Gradle을 통한 실행
./gradlew :analytics-service:bootRun

# JAR 파일 실행
java -jar analytics-service/build/libs/analytics-service-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-analytics-service .
docker run -p 8086:8086 marketing-analytics-service
```

### 환경 변수 설정
```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=marketing
export DB_USERNAME=marketing_user
export DB_PASSWORD=password

# Redis
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password

# YouTube API
export YOUTUBE_API_KEY=your-youtube-api-key

# AI Service
export AI_SERVICE_URL=http://localhost:8000

# Kafka
export KAFKA_SERVERS=localhost:9092
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :analytics-service:test
```

### Integration Tests
```bash
./gradlew :analytics-service:integrationTest
```

### API 테스트 예시
```bash
# 실시간 계정 메트릭 조회 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  "http://localhost:8086/api/analytics/realtime/accounts/1/metrics?period=7d"

# 감정 분석 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  http://localhost:8086/api/analytics/emotion/posts/1/comments

# 배치 작업 실행 테스트
curl -X POST \
  -H "Authorization: Bearer {your-jwt-token}" \
  http://localhost:8086/api/analytics/batch/jobs/metrics-collection/trigger
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8086/actuator/health
```

### Metrics
- 실시간 분석 응답 시간
- 배치 작업 성공/실패율
- AI 감정 분석 정확도
- 캐시 히트율
- YouTube API 사용량

### Custom Metrics
```java
@Component
public class AnalyticsMetrics {
    private final Counter analysisRequests;
    private final Timer batchJobDuration;
    private final Gauge cacheHitRate;
    
    // 분석 서비스 메트릭 수집
}
```

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. 배치 작업 실패
- **증상**: 메트릭 수집 작업이 완료되지 않음
- **원인**: 
  - YouTube API 쿼터 초과
  - 데이터베이스 연결 오류
  - 메모리 부족
- **해결**: API 사용량 확인, DB 연결 상태 점검, 메모리 설정 조정

#### 2. 실시간 데이터 지연
- **증상**: 최신 데이터가 반영되지 않음
- **원인**: Redis 캐시 설정 오류 또는 Kafka 지연
- **해결**: 캐시 설정 확인, Kafka 컨슈머 상태 점검

#### 3. AI 감정 분석 오류
- **증상**: 감정 분석 결과가 부정확하거나 오류 발생
- **원인**: AI 서비스 연결 실패 또는 텍스트 전처리 오류
- **해결**: AI 서비스 상태 확인, 텍스트 데이터 검증

#### 4. 메트릭 데이터 불일치
- **증상**: 서로 다른 API에서 다른 결과 반환
- **원인**: 캐시 동기화 문제 또는 데이터 정합성 오류
- **해결**: 캐시 초기화, 데이터 재동기화 작업 실행

## 📈 성능 최적화

### 데이터베이스 최적화
```sql
-- 메트릭 조회 최적화를 위한 인덱스
CREATE INDEX idx_account_metric_date ON sns_account_metrics(sns_account_id, metric_date DESC);
CREATE INDEX idx_post_metric_date ON sns_post_metrics(sns_post_id, metric_date DESC);

-- 파티셔닝으로 대용량 데이터 처리 최적화
ALTER TABLE sns_account_metrics PARTITION BY RANGE (YEAR(metric_date));
```

### 캐싱 전략 최적화
```java
@Service
public class AnalyticsCache {
    
    // 실시간 데이터 캐싱 (5분)
    @Cacheable(value = "realtime-metrics", key = "#accountId")
    public AccountMetrics getRealtimeMetrics(Long accountId);
    
    // 히스토리컬 데이터 캐싱 (1시간)
    @Cacheable(value = "historical-metrics", key = "#accountId + '_' + #period")
    public List<MetricData> getHistoricalMetrics(Long accountId, String period);
}
```

### 배치 처리 최적화
```java
@Configuration
public class BatchOptimizationConfig {
    
    @Bean
    public Step optimizedMetricsStep() {
        return stepBuilderFactory.get("optimizedMetricsStep")
            .<InputData, OutputData>chunk(1000)  // 청크 크기 최적화
            .reader(parallelReader())             // 병렬 처리
            .processor(asyncProcessor())          // 비동기 처리
            .writer(batchWriter())                // 배치 쓰기
            .taskExecutor(taskExecutor())         // 멀티스레드
            .build();
    }
}
```

## 🔧 운영 고려사항

### 로깅 설정
```yaml
logging:
  level:
    kt.aivle.analytics: INFO
    org.springframework.batch: DEBUG
    org.springframework.kafka: INFO
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 보안 고려사항
- 분석 데이터 접근 권한 제어
- 민감한 메트릭 정보 암호화
- API 레이트 리미팅
- 데이터 익명화 처리

### 확장성 고려사항
- 분산 캐시 시스템 도입
- 대용량 데이터 처리를 위한 파티셔닝
- 실시간 스트림 처리 (Kafka Streams)
- 머신러닝 기반 예측 분석 기능

---

**서비스 담당**: Analytics Team  
**최종 업데이트**: 2024년