# Analytics Service

YouTube 및 기타 SNS 플랫폼의 메트릭을 수집하고 분석하는 마이크로서비스입니다.

## 🔄 최근 개선사항

### 🏗️ 헥사고날 아키텍처 완성
- **Port/Adapter 패턴 완전 적용**: 모든 외부 의존성을 Port 인터페이스로 추상화
  - `ExternalApiPort` → `YouTubeApiAdapter`: YouTube API 통신
  - `AiAnalysisPort` → `AiAnalysisAdapter`: AI 분석 서버 통신
  - `CachePort` → `RedisCacheAdapter`: Redis 캐시 관리
  - `ValidationPort` → `AnalyticsDomainService`: 비즈니스 규칙 검증
- **의존성 역전 원칙**: 서비스 계층이 인프라 계층에 의존하지 않음
- **테스트 용이성**: Port 인터페이스를 통한 Mock 테스트 가능

### 📁 폴더 구조 개선
- **Repository Ports**: `port/out/repository/` - 데이터 접근 관련 Port
- **Infrastructure Ports**: `port/out/infrastructure/` - 외부 시스템 통신 관련 Port
- **명확한 책임 분리**: Repository와 Infrastructure Port가 명확히 구분

### 🗑️ 중복 서비스 제거
- **`AiAnalysisService` 제거**: `AiAnalysisAdapter`로 대체
- **`AnalyticsCacheService` 제거**: `RedisCacheAdapter`로 대체
- **`MetricsValidator` 제거**: `AnalyticsDomainService`로 대체
- **`YouTubeApiService` 제거**: `YouTubeApiAdapter`로 대체

### 🔧 구조적 개선
- **공통 Repository 패턴**: `BaseJpaRepository` 추상 클래스로 중복 코드 제거
  - 모든 JpaRepository가 공통 기능을 상속받도록 리팩토링
  - `findAllWithPagination`, `findByCreatedAtBetween` 등 공통 메서드 제공
- **Validator 분리**: `AnalyticsRequestValidator`로 입력 검증 로직 통합
  - ID 형식 검증, 페이지네이션 검증 등 공통 로직 분리
  - 컨트롤러에서 중복되던 검증 코드 제거
- **예외 처리 개선**: 구체적인 예외 클래스들 추가
  - `AnalyticsNotFoundException`: 데이터를 찾을 수 없을 때
  - `AnalyticsValidationException`: 입력 검증 실패 시
  - `AnalyticsQuotaExceededException`: YouTube API 할당량 초과 시

### 날짜 처리 최적화 (2024년 최신 업데이트)
- **LocalDate → Date 변경**: 타임존 일관성을 위한 날짜 타입 통일
  - Jackson의 `time-zone` 설정과 일치하도록 Date 타입 사용
  - 서울 타임존 기준으로 일관된 날짜 처리
- **날짜 필터링 단순화**: 복잡한 범위 검색에서 직접 날짜 비교로 변경
  - `DATE(createdAt) = DATE(:date)` 쿼리로 정확한 날짜 매칭
  - 불필요한 `startDate`/`endDate` 계산 로직 제거
  - 성능 향상 및 코드 간소화
- **Repository 메서드 최적화**: 날짜 전용 조회 메서드 추가
  - `findByPostIdAndCreatedAtDate(Long postId, Date date)`
  - `findByAccountIdAndCreatedAtDate(Long accountId, Date date)`
  - `findByPostIdAndCreatedAtDateWithPagination(Long postId, Date date, Integer page, Integer size)`

### 기능적 개선
- **캐싱 전략**: Redis 캐싱을 활용한 성능 최적화
  - `@Cacheable` 어노테이션으로 메트릭 조회 결과 캐싱
  - 캐시 TTL 설정으로 데이터 신선도 관리
- **비동기 처리**: `CompletableFuture`를 활용한 비동기 메트릭 조회
  - `@Async` 어노테이션으로 대용량 데이터 처리 성능 향상
- **배치 처리**: 병렬 처리를 통한 대용량 데이터 처리 성능 향상
  - 병렬 스레드 수 설정으로 리소스 최적화

### 모니터링 및 문서화
- **Actuator**: 헬스체크 및 메트릭 모니터링 엔드포인트 추가
  - `/actuator/health`: 서비스 상태 확인
  - `/actuator/metrics`: 성능 메트릭 조회
  - `/api/analytics/health`: 커스텀 헬스체크 엔드포인트
- **Swagger**: API 문서화 개선
  - `@Operation`, `@Parameter` 어노테이션으로 상세한 API 문서화
  - 각 엔드포인트별 설명 및 파라미터 정보 추가
- **로깅**: 구조화된 로깅 및 캐시 디버깅 로그 추가
  - 캐시 히트/미스 로그 추가
  - 성능 모니터링을 위한 구조화된 로그 패턴 적용

## 🚀 주요 기능

### 1. 실시간 데이터 조회 API
YouTube API에서 실시간 데이터를 직접 조회하는 API들입니다.

#### 실시간 게시물 메트릭
```bash
GET /api/analytics/realtime/posts/{postId}/metrics
```
- **postId**: 로컬 DB의 게시물 ID (Long)
- **응답**: `RealtimePostMetricsResponse` - YouTube API 실시간 데이터

#### 실시간 계정 메트릭
```bash
GET /api/analytics/realtime/accounts/{accountId}/metrics
```
- **accountId**: 로컬 DB의 계정 ID (Long)
- **응답**: `RealtimeAccountMetricsResponse` - YouTube API 실시간 데이터

#### 실시간 댓글 조회
```bash
GET /api/analytics/realtime/posts/{postId}/comments?page=0&size=20
```
- **postId**: 로컬 DB의 게시물 ID (Long)
- **응답**: `PostCommentsQueryResponse` - YouTube API 실시간 댓글

#### 실시간 감정분석 조회
```bash
GET /api/analytics/realtime/posts/{postId}/emotion-analysis
```
- **postId**: 로컬 DB의 게시물 ID (Long)
- **응답**: `EmotionAnalysisResponse` - 댓글 감정분석 결과 및 키워드

### 2. 히스토리 데이터 조회 API
로컬 DB에 저장된 과거 데이터를 조회하는 API들입니다.

#### 히스토리 게시물 메트릭
```bash
GET /api/analytics/history/posts/{postId}/metrics?date=2024-01-15
```
- **postId**: 로컬 DB의 게시물 ID (Long)
- **date**: 조회할 날짜 (Date, 필수) - 서울 타임존 기준
- **응답**: `PostMetricsQueryResponse` - DB 저장된 메트릭

#### 히스토리 계정 메트릭
```bash
GET /api/analytics/history/accounts/{accountId}/metrics?date=2024-01-15
```
- **accountId**: 로컬 DB의 계정 ID (Long)
- **date**: 조회할 날짜 (Date, 필수) - 서울 타임존 기준
- **응답**: `AccountMetricsQueryResponse` - DB 저장된 메트릭

#### 히스토리 댓글 조회
```bash
GET /api/analytics/history/posts/{postId}/comments?date=2024-01-15&page=0&size=20
```
- **postId**: 로컬 DB의 게시물 ID (Long)
- **date**: 조회할 날짜 (Date, 필수) - 서울 타임존 기준
- **응답**: `PostCommentsQueryResponse` - DB 저장된 댓글

#### 감정분석 결과 조회
```bash
GET /api/analytics/history/posts/{postId}/emotion-analysis?date=2024-01-15
```
- **postId**: 로컬 DB의 게시물 ID (Long)
- **date**: 조회할 날짜 (Date, 필수) - 서울 타임존 기준
- **응답**: `EmotionAnalysisResponse` - 댓글 감정분석 결과 및 키워드

### 3. 배치 작업 API
메트릭 수집을 위한 배치 작업을 실행하고 모니터링하는 API들입니다.

#### 배치 작업 실행
```bash
POST /api/analytics/batch/accounts/{accountId}/metrics
POST /api/analytics/batch/posts/{postId}/metrics
POST /api/analytics/batch/posts/{postId}/comments
```
- **accountId/postId**: 로컬 DB의 ID (Long)
- **응답**: `BatchOperationResponse` - 배치 작업 실행 결과

#### 배치 작업 상태 조회
```bash
GET /api/analytics/batch/status
GET /api/analytics/batch/status/{jobName}
```
- **응답**: `BatchJobStatusResponse` - 배치 작업 상태 정보

### 4. 모니터링 API
서비스 상태 및 성능을 모니터링하는 API들입니다.

#### 헬스체크
```bash
GET /api/analytics/health
```
- **응답**: `HealthStatus` - 서비스 상태 정보

#### YouTube API 할당량 상태
```bash
GET /api/analytics/realtime/quota/status
```
- **응답**: `QuotaStatus` - YouTube API 할당량 사용 현황

#### Actuator 엔드포인트
```bash
GET /actuator/health          # 서비스 헬스체크
GET /actuator/metrics         # 성능 메트릭
GET /actuator/prometheus      # Prometheus 메트릭
```

## 📊 DTO 구조

### AI 분석용 DTO
```java
// AiAnalysisRequest - AI 서버로 전송하는 댓글 데이터
{
  "data": [
    {
      "id": "1",
      "result": "댓글 내용"
    }
  ],
  "keyword": {
    "positive": ["긍정키워드1", "긍정키워드2"],
    "negative": ["부정키워드1", "부정키워드2"]
  }
}

// AiAnalysisResponse - AI 서버로부터 받는 분석 결과
{
  "emotion_analysis": {
    "individual_results": [
      {
        "id": "1",
        "result": "POSITIVE"  // POSITIVE, NEGATIVE, NEUTRAL
      }
    ]
  },
  "keywords": {
    "positive": ["긍정키워드1", "긍정키워드2"],
    "negative": ["부정키워드1", "부정키워드2"]
  }
}

// SentimentType Enum
public enum SentimentType {
    POSITIVE("POSITIVE"),    // 긍정
    NEGATIVE("NEGATIVE"),    // 부정
    NEUTRAL("NEUTRAL");      // 중립
}
```

### 모니터링용 DTO
```java
// HealthStatus
{
  "service": "analytics-service",
  "status": "UP",
  "timestamp": 1705123456789
}

// QuotaStatus
{
  "currentUsage": 1500,
  "limit": 10000,
  "timeUntilReset": 7200000,  // 2시간 (밀리초)
  "lastResetTime": 1705116000000
}
```

### 실시간 데이터용 DTO
```java
// RealtimePostMetricsResponse
{
  "postId": 1,                    // 로컬 DB ID
  "snsPostId": "w7YKHjH_MpM",     // YouTube Video ID
  "accountId": 1,                 // 로컬 계정 ID
  "likes": 1234,                  // 실시간 좋아요 수 (Long으로 변경)
  "dislikes": 5,                  // 실시간 싫어요 수
  "comments": 89,                 // 실시간 댓글 수
  "shares": null,                 // 공유 수 (YouTube API v3 미지원)
  "views": 56789,                 // 실시간 조회 수
  "fetchedAt": "2024-01-15T12:00:00", // API 호출 시간
  "dataSource": "youtube_api",    // 데이터 소스
  "isCached": false               // 캐시 여부
}

// RealtimeAccountMetricsResponse
{
  "accountId": 1,                 // 로컬 DB ID
  "snsAccountId": "UC123456789",  // YouTube Channel ID
  "followers": 12345,             // 실시간 구독자 수
  "views": 1234567,               // 실시간 총 조회 수
  "fetchedAt": "2024-01-15T12:00:00", // API 호출 시간
  "dataSource": "youtube_api",    // 데이터 소스
  "isCached": false               // 캐시 여부
}
```

### 히스토리 데이터용 DTO
```java
// PostMetricsQueryResponse
{
  "postId": 1,                    // 로컬 DB ID
  "likes": "1234",                // DB 저장된 좋아요 수
  "dislikes": 5,                  // DB 저장된 싫어요 수
  "comments": 89,                 // DB 저장된 댓글 수
  "shares": null,                 // DB 저장된 공유 수
  "views": 56789,                 // DB 저장된 조회 수
  "crawledAt": "2024-01-15T12:00:00" // 수집 시간 (createdAt 기반)
}

// AccountMetricsQueryResponse
{
  "accountId": 1,                 // 로컬 DB ID
  "followers": 12345,             // DB 저장된 구독자 수
  "views": 1234567,               // DB 저장된 총 조회 수
  "crawledAt": "2024-01-15T12:00:00" // 수집 시간 (createdAt 기반)
}

// PostCommentsQueryResponse
{
  "commentId": "UgzDE8pqJ_c",     // YouTube 댓글 ID
  "authorId": 123456789,          // 댓글 작성자 ID
  "text": "댓글 내용",            // 댓글 텍스트
  "likeCount": 5,                 // 댓글 좋아요 수
  "publishedAt": "2024-01-15T12:00:00", // 댓글 작성 시간
  "crawledAt": "2024-01-15T12:00:00"    // 수집 시간 (createdAt 기반)
}
```

### 배치 작업용 DTO
```java
// BatchOperationResponse
{
  "operationName": "collectPostMetrics",
  "status": "SUCCESS",
  "executedAt": "2024-01-15T12:00:00",
  "message": "Post metrics collection completed successfully",
  "processedCount": 1,
  "failedCount": 0
}

// BatchJobStatusResponse
{
  "jobName": "collectPostMetrics_1",
  "status": "RUNNING",
  "startTime": "2024-01-15T12:00:00",
  "endTime": null,
  "progress": 50,
  "totalItems": 100,
  "errorMessage": null
}
```

## 🔧 주요 개선사항

### 1. 아키텍처 개선
- **Hexagonal Architecture 완전 준수**: Port/Adapter 패턴 완전 적용
- **의존성 역전 원칙**: 모든 외부 의존성을 Port 인터페이스로 추상화
- **명확한 책임 분리**: 각 계층의 역할 명확화
- **테스트 용이성**: Port 인터페이스를 통한 Mock 테스트 가능

### 2. 타입 안전성 강화
- **Bean Validation 추가**: `@Pattern`, `@Min`, `@Max` 어노테이션
- **입력 검증 강화**: 컨트롤러 레벨에서 엄격한 검증
- **Lombok 경고 해결**: `@Builder.Default` 어노테이션 적용

### 3. 캐싱 전략 개선
- **기존**: Guava Cache (인메모리, 단일 서버)
- **개선**: Redis Cache (분산 캐시, 클러스터 공유)
- **세분화된 TTL**: 메트릭 5분, 댓글 2분, 할당량 1분
- **스마트 캐시 무효화**: 이벤트 발생 시 관련 캐시만 삭제
- **Port/Adapter 패턴**: `CachePort` 인터페이스를 통한 캐시 추상화

### 4. 에러 처리 체계화
- **AnalyticsErrorCode enum**: 구체적인 에러 코드 체계
- **AnalyticsException 개선**: 에러 코드와 메시지 분리
- **일관된 에러 응답**: 모든 API에서 동일한 에러 형식

### 5. 도메인 모델 최적화 (2024년 최신 업데이트)
- **SnsPostMetric**: `snsPostId`, `accountId` 필드 제거 (FK로 충분히 조회 가능)
- **SnsAccountMetric**: `snsAccountId` 필드 제거 (FK로 충분히 조회 가능)
- **SnsPostCommentMetric**: `authorName` → `authorId`로 변경 (정확한 사용자 식별)
- **시간 필드 통합**: `crawledAt` 제거, `createdAt` 기반으로 통일
- **데이터 중복 제거**: FK 관계로 충분히 조회 가능한 필드들 제거
- **성능 최적화**: 불필요한 필드 제거로 저장 공간 절약

### 6. AI 분석 통합 (2024년 최신 업데이트)
- **SentimentType Enum**: 감정 분석 결과를 위한 타입 안전한 enum 추가
  - `POSITIVE`, `NEGATIVE`, `NEUTRAL` 값으로 감정 분류
  - Jackson `@JsonCreator`와 `@JsonValue`로 AI 서버 응답과 자동 매핑
  - 대소문자 무관한 파싱 및 기본값 처리
- **AiAnalysisPort/AiAnalysisAdapter**: AI 서버와의 통신을 위한 Port/Adapter 패턴
  - `AiAnalysisPort` 인터페이스로 AI 서버 통신 추상화
  - `AiAnalysisAdapter`에서 실제 HTTP 통신 구현
  - 댓글 데이터를 AI 서버로 전송하여 감정 분석 수행
  - 키워드 추출 기능 포함 (긍정/부정 분리)
- **EmotionAnalysisService**: 감정 분석 비즈니스 로직을 담당하는 서비스
  - `AiAnalysisPort`를 통한 AI 서버 호출
  - 감정 분석 결과를 DB에 저장
  - 에러 처리 및 재시도 로직

### 7. 성능 최적화
- **API 호출 최적화**: 캐싱으로 중복 호출 방지
- **할당량 관리**: YouTube API 할당량 효율적 사용
- **비동기 처리**: 배치 작업의 비동기 실행
- **쿼리 최적화**: `createdAt` 기반 인덱스 활용
- **날짜 필터링 최적화**: `DATE()` 함수를 활용한 정확한 날짜 매칭
- **키워드 조회 최적화**: DB 조회 2번 → 1번으로 최적화
- **Port/Adapter 패턴**: 느슨한 결합으로 성능 최적화
- **Mock 테스트**: 빠른 개발 사이클로 성능 개선
- **의존성 역전**: 외부 시스템과의 느슨한 결합
- **확장성**: 새로운 Adapter 추가 시 기존 코드 변경 없음

## 🏗️ 서비스 아키텍처

### 핵심 서비스 구성

#### 1. **AnalyticsQueryService** - 데이터 조회 서비스
- **역할**: 히스토리 데이터 및 실시간 데이터 조회
- **주요 기능**:
  - 히스토리 메트릭/댓글 조회 (DB 기반)
  - 실시간 메트릭/댓글 조회 (`ExternalApiPort` 기반)
  - 감정분석 결과 조회
  - 캐싱 적용 (`@Cacheable`)
- **특징**: 
  - 히스토리/실시간 완전 분리
  - 키워드 조회 최적화 (한 번에 조회 후 메모리에서 분리)

#### 2. **MetricsCollectionService** - 메트릭 수집 서비스
- **역할**: 배치 작업을 통한 메트릭 수집
- **주요 기능**:
  - 계정 메트릭 수집 (`ExternalApiPort` 사용)
  - 게시물 메트릭 수집 (`ExternalApiPort` 사용)
  - 댓글 수집 및 감정분석 연동
  - 병렬 처리 및 배치 최적화
- **특징**:
  - 새로운 댓글 발견 시 자동 감정분석 수행
  - YouTube API 할당량 관리
  - 배치 작업 모니터링

#### 3. **EmotionAnalysisService** - 감정분석 서비스
- **역할**: 댓글 감정분석 및 키워드 추출
- **주요 기능**:
  - `AiAnalysisPort`를 통한 AI 서버 통신
  - 감정분석 결과 DB 저장
  - 키워드 긍정/부정 분리 저장
- **특징**:
  - AI 서버 응답 처리
  - 에러 처리 및 로깅

#### 4. **AnalyticsEventService** - 이벤트 처리 서비스
- **역할**: 외부 이벤트 처리 (게시물/계정 생성/삭제)
- **주요 기능**:
  - 게시물 생성/삭제 이벤트 처리
  - SNS 계정 연결/해제 이벤트 처리
  - `CachePort`를 통한 관련 캐시 무효화
- **특징**:
  - 이벤트 기반 캐시 무효화
  - 트랜잭션 처리

#### 5. **BatchScheduler** - 스케줄링 서비스
- **역할**: 정기적인 메트릭 수집 스케줄링
- **주요 기능**:
  - 매일 오전 1시 자동 메트릭 수집
  - Spring Batch Job 실행
  - 스케줄 관리
- **특징**:
  - `@Scheduled` 어노테이션 활용
  - 중복 실행 방지

#### 6. **BatchJobMonitor** - 배치 작업 모니터링
- **역할**: 배치 작업 상태 모니터링
- **주요 기능**:
  - 배치 작업 진행률 추적
  - 작업 상태 관리
  - 에러 처리
- **특징**:
  - 실시간 모니터링
  - 작업 히스토리 관리

### Port/Adapter 구성

#### Infrastructure Ports
- **`ExternalApiPort`**: YouTube API 통신 추상화
- **`AiAnalysisPort`**: AI 분석 서버 통신 추상화
- **`CachePort`**: Redis 캐시 관리 추상화
- **`ValidationPort`**: 비즈니스 규칙 검증 추상화

#### Infrastructure Adapters
- **`YouTubeApiAdapter`**: YouTube API 통신 구현
- **`AiAnalysisAdapter`**: AI 분석 서버 통신 구현
- **`RedisCacheAdapter`**: Redis 캐시 구현
- **`AnalyticsDomainService`**: 비즈니스 규칙 검증 구현

#### Repository Ports
- **`SnsAccountRepositoryPort`**: 계정 데이터 접근
- **`SnsPostRepositoryPort`**: 게시물 데이터 접근
- **`SnsAccountMetricRepositoryPort`**: 계정 메트릭 데이터 접근
- **`SnsPostMetricRepositoryPort`**: 게시물 메트릭 데이터 접근
- **`SnsPostCommentMetricRepositoryPort`**: 댓글 메트릭 데이터 접근
- **`PostCommentKeywordRepositoryPort`**: 키워드 데이터 접근

### 서비스 간 의존성 (Port/Adapter 패턴)
```
AnalyticsQueryService
├── ExternalApiPort (YouTubeApiAdapter)
├── CachePort (RedisCacheAdapter)
└── PostCommentKeywordRepositoryPort

MetricsCollectionService
├── ExternalApiPort (YouTubeApiAdapter)
├── ValidationPort (AnalyticsDomainService)
├── EmotionAnalysisService
└── BatchJobMonitor

EmotionAnalysisService
├── AiAnalysisPort (AiAnalysisAdapter)
└── PostCommentKeywordRepositoryPort

AnalyticsEventService
├── CachePort (RedisCacheAdapter)
└── Repository Ports

BatchScheduler
└── MetricsCollectionService
```

## 🚀 실행 방법

### 1. 로컬 실행
```bash
cd analytics-service
./gradlew bootRun
```

### 2. 도커 실행
```bash
docker-compose up -d analytics-service
```

### 3. 빌드
```bash
./gradlew :analytics-service:build
```

## 📝 테스트

### 1. 빠른 테스트
```bash
./quick-test.sh
```

### 2. 배치 작업 테스트
```bash
./batch-test-api.sh
```

### 3. DB 초기화
```bash
./clear-db-only.sh
```

## 🔍 모니터링

### 1. 로그 확인
```bash
# 실시간 로그
docker logs -f analytics-service

# 스케줄러 로그
docker logs analytics-service | grep "DAILY METRICS"

# 캐시 디버깅 로그
docker logs analytics-service | grep "CACHE"
```

### 2. 배치 작업 상태
```bash
curl -X GET "http://localhost:8080/api/analytics/batch/status" \
  -H "X-USER-ID: 4" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 캐시 상태 확인
```bash
# Redis 캐시 확인
redis-cli keys "analytics:*"

# 할당량 상태 확인
curl -X GET "http://localhost:8080/api/analytics/realtime/quota/status"

# 헬스체크
curl -X GET "http://localhost:8080/api/analytics/health"
```

### 4. Actuator 메트릭
```bash
# 서비스 헬스체크
curl -X GET "http://localhost:8080/actuator/health"

# 성능 메트릭
curl -X GET "http://localhost:8080/actuator/metrics"

# Prometheus 메트릭
curl -X GET "http://localhost:8080/actuator/prometheus"
```

## 📊 배치 스케줄링 시스템

### 🕐 스케줄 설정

#### 프로덕션 환경 - 매일 오전 1시 자동 실행
```java
@Scheduled(cron = "0 0 01 * * ?", zone = "Asia/Seoul")
public void runDailyMetricsCollectionJob()
```
- **실행 시간**: 매일 오전 1시 (한국 시간)
- **실행 순서**: 계정 메트릭 → 게시물 메트릭 → 댓글 수집
- **Spring Batch Job**: `dailyMetricsCollectionJob`

#### 개발 환경 - 테스트용 스케줄 (현재 비활성화)
```java
// @Scheduled(cron = "0 */2 * * * ?", zone = "Asia/Seoul")
// @Profile("dev")
public void runTestMetricsCollectionJob()
```
- **실행 주기**: 2분마다 (테스트용)
- **프로파일**: `dev` 환경에서만 활성화
- **용도**: 개발 및 테스트를 위한 빠른 수집

### 🔄 배치 작업 흐름

#### 1. 스케줄러 실행 (`BatchScheduler`)
```
매일 9시 → BatchScheduler.runDailyMetricsCollectionJob()
    ↓
JobParameters 생성 (실행시간, 타임스탬프)
    ↓
Spring Batch Job 실행
```

#### 2. Spring Batch Job 구성 (`BatchConfig`)
```
dailyMetricsCollectionJob
    ↓
Step 1: collectAccountMetricsStep (계정 메트릭 수집)
    ↓
Step 2: collectPostMetricsStep (게시물 메트릭 수집)
    ↓
Step 3: collectPostCommentsStep (댓글 수집)
```

#### 3. 배치 처리 로직 (`MetricsCollectionService`)
```
processBatch() 메서드
    ↓
1. 전체 아이템 수 조회
2. 페이지네이션으로 배치 처리 (기본 100개씩)
3. 각 아이템별 개별 처리
4. 진행률 모니터링
5. 에러 처리 및 재시도
```

### 🛡️ 중단 처리 및 복구 메커니즘

#### 1. YouTube API 할당량 초과 시
```java
catch (AnalyticsQuotaExceededException e) {
    log.warn("YouTube API quota exceeded during {} collection. Stopping batch.", itemType);
    break; // 배치 작업 중단
}
```
- **동작**: 즉시 배치 작업 중단
- **복구**: 다음 날 9시에 다시 실행
- **데이터 손실**: 없음 (부분적으로 수집된 데이터는 저장됨)

#### 2. Spring Batch Job 중복 실행 방지
```java
catch (JobExecutionAlreadyRunningException e) {
    log.error("❌ Batch job is already running: {}", e.getMessage());
}
```
- **동작**: 이미 실행 중인 배치가 있으면 새 배치 실행 안함
- **복구**: 기존 배치 완료 후 다음 스케줄까지 대기

#### 3. 배치 작업 실패 시
```java
catch (Exception e) {
    batchJobMonitor.recordJobFailure(jobName, e.getMessage());
    log.error("Failed to collect {}", itemType, e);
    throw new AnalyticsException("Failed to collect " + itemType, e);
}
```
- **동작**: 실패한 배치 작업 상태 기록
- **복구**: 수동으로 재실행 가능 (API 엔드포인트 제공)
- **데이터 손실**: 실패한 아이템만 손실, 성공한 아이템은 저장됨

### 📊 배치 작업 모니터링

#### 1. 실시간 상태 조회
```bash
# 모든 배치 작업 상태
GET /api/analytics/batch/status

# 특정 배치 작업 상태
GET /api/analytics/batch/status/{jobName}
```

#### 2. 배치 작업 상태 정보
```java
{
  "jobName": "account-metrics-collection",
  "status": "RUNNING",           // RUNNING, COMPLETED, FAILED
  "startTime": "2024-01-15T09:00:00",
  "endTime": null,
  "progress": 50,                // 처리된 아이템 수
  "totalItems": 100,             // 전체 아이템 수
  "errorMessage": null           // 에러 메시지 (실패 시)
}
```

#### 3. 로그 모니터링
```bash
# 배치 작업 시작/완료 로그
docker logs analytics-service | grep "Daily metrics collection"

# 실시간 진행률 로그
docker logs analytics-service | grep "Batch job progress"

# 에러 로그
docker logs analytics-service | grep "❌"
```

### 🔧 수동 배치 작업 실행

#### 1. 전체 메트릭 수집
```bash
POST /api/analytics/batch/metrics
```

#### 2. 개별 메트릭 수집
```bash
# 계정 메트릭
POST /api/analytics/batch/accounts/metrics
POST /api/analytics/batch/accounts/{accountId}/metrics

# 게시물 메트릭
POST /api/analytics/batch/posts/metrics
POST /api/analytics/batch/posts/{postId}/metrics

# 댓글 수집
POST /api/analytics/batch/posts/comments
POST /api/analytics/batch/posts/{postId}/comments
```

### 🚨 에러 처리 및 복구 전략

#### 1. 네트워크 에러
- **재시도**: `@Retryable` 어노테이션으로 최대 3회 재시도
- **백오프**: 1초 간격으로 지수 백오프
- **복구**: 재시도 성공 시 정상 처리, 실패 시 다음 배치에서 재시도

#### 2. 데이터베이스 에러
- **트랜잭션**: 각 배치 아이템별 트랜잭션 처리
- **롤백**: 실패 시 해당 아이템만 롤백
- **복구**: 성공한 아이템은 유지, 실패한 아이템만 재처리

#### 3. YouTube API 에러
- **할당량 초과**: 즉시 배치 중단, 다음 날 재시도
- **API 오류**: 개별 아이템 실패 처리, 배치 계속 진행
- **복구**: 실패한 아이템은 수동 재실행 가능

### 📈 성능 최적화

#### 1. 병렬 처리
- **배치 크기**: 기본 100개씩 처리
- **병렬 스레드**: 4개 스레드로 병렬 처리
- **메모리 효율성**: 페이지네이션으로 메모리 사용량 제한

#### 2. 캐싱 전략
- **중복 방지**: 최근 1시간 내 데이터가 있으면 스킵
- **API 호출 최소화**: 캐싱으로 중복 API 호출 방지
- **성능 향상**: 캐시 히트 시 즉시 응답

#### 3. 할당량 관리
- **실시간 모니터링**: YouTube API 할당량 사용량 추적
- **스마트 중단**: 할당량 초과 시 즉시 배치 중단
- **효율적 사용**: 배치 처리로 API 호출 최적화

### 🔄 다음 날 처리 로직

#### 1. 자동 재시작
- **스케줄**: 매일 9시 자동 실행
- **독립성**: 이전 배치 실패와 무관하게 새 배치 시작
- **데이터 무결성**: 중복 데이터 방지 로직으로 데이터 일관성 보장

#### 2. 누적 데이터 처리
- **히스토리 보존**: 이전 수집 데이터는 그대로 유지
- **증분 수집**: 새로운 데이터만 추가 수집
- **데이터 갱신**: 기존 데이터는 최신 정보로 업데이트

#### 3. 실패한 데이터 복구
- **수동 재실행**: API를 통한 개별 아이템 재처리
- **선택적 복구**: 실패한 아이템만 선택적으로 재처리
- **데이터 보완**: 누락된 데이터 보완 가능

## 🏗️ 아키텍처 구조

```
analytics-service/
├── adapter/
│   ├── in/
│   │   ├── web/           # REST API 컨트롤러
│   │   │   └── validator/ # 입력 검증 로직
│   │   └── event/         # 이벤트 컨트롤러
│   └── out/
│       ├── persistence/   # 데이터 접근 계층
│       │   └── repository/ # JPA Repository
│       └── infrastructure/ # 외부 시스템 통신 계층
│           ├── YouTubeApiAdapter
│           ├── AiAnalysisAdapter
│           └── RedisCacheAdapter
├── application/
│   ├── port/
│   │   ├── in/            # UseCase 인터페이스
│   │   └── out/           # Port 인터페이스
│   │       ├── repository/ # Repository Ports
│   │       └── infrastructure/ # Infrastructure Ports
│   └── service/           # 비즈니스 로직
├── domain/
│   ├── entity/            # 도메인 엔티티
│   ├── model/             # 도메인 모델
│   └── service/           # 도메인 서비스 (AnalyticsDomainService)
├── exception/             # 예외 처리 클래스
└── config/                # 설정 클래스
```

### 개선된 구조 특징
- **헥사고날 아키텍처**: Port/Adapter 패턴 완전 적용
- **의존성 역전**: 모든 외부 의존성을 Port 인터페이스로 추상화
- **공통 Repository**: `BaseJpaRepository`로 중복 코드 제거
- **Validator 분리**: 입력 검증 로직을 별도 패키지로 분리
- **예외 처리**: 구체적인 예외 클래스들로 세분화된 에러 처리
- **캐싱 계층**: Redis 캐싱을 통한 성능 최적화
- **모니터링**: Actuator를 통한 헬스체크 및 메트릭 수집

## 🔄 캐싱 전략

### 캐시 계층 구조
1. **Redis Cache** (분산 캐시)
   - 메트릭 데이터: 5분 TTL (`@Cacheable(value = "post-metrics")`)
   - 댓글 데이터: 2분 TTL (`@Cacheable(value = "comments")`)
   - 할당량 정보: 1분 TTL (Guava Cache)

2. **Port/Adapter 패턴**
   - `CachePort` 인터페이스로 캐시 추상화
   - `RedisCacheAdapter`에서 실제 Redis 구현
   - 서비스 계층은 Port를 통해서만 캐시 접근

3. **이벤트 기반 무효화**
   - 게시물 생성/삭제 시 관련 캐시 삭제
   - 계정 연결/해제 시 관련 캐시 삭제
   - `CachePort.evict*` 메서드로 캐시 무효화

4. **캐시 키 패턴**
   - `post-metrics::{userId}-{postId}-{date}`
   - `account-metrics::{userId}-{accountId}-{date}`
   - `comments::{postId}-{page}-{size}`

5. **성능 최적화**
   - 캐시 히트율 모니터링
   - 캐시 미스 시 자동 재로딩
   - 배치 작업 시 캐시 일괄 갱신

## 🚨 에러 코드

### 입력 검증 (ANALYTICS-001 ~ 004)
- `INVALID_POST_ID`: 잘못된 게시물 ID 형식
- `INVALID_ACCOUNT_ID`: 잘못된 계정 ID 형식
- `INVALID_DATE`: 잘못된 날짜 파라미터
- `INVALID_PAGINATION`: 잘못된 페이지네이션 파라미터

### 데이터 조회 (ANALYTICS-101 ~ 103)
- `POST_NOT_FOUND`: 게시물을 찾을 수 없음
- `ACCOUNT_NOT_FOUND`: 계정을 찾을 수 없음
- `NO_DATA_AVAILABLE`: 조회 조건에 맞는 데이터 없음

### YouTube API (ANALYTICS-201 ~ 204)
- `YOUTUBE_API_ERROR`: YouTube API 오류 발생
- `YOUTUBE_QUOTA_EXCEEDED`: YouTube API 할당량 초과
- `YOUTUBE_VIDEO_NOT_FOUND`: YouTube 비디오를 찾을 수 없음
- `YOUTUBE_CHANNEL_NOT_FOUND`: YouTube 채널을 찾을 수 없음

### 권한 관련 (ANALYTICS-301 ~ 302)
- `UNAUTHORIZED_ACCESS`: 데이터에 대한 권한 없음
- `USER_MISMATCH`: 사용자 ID 불일치

### 시스템 관련 (ANALYTICS-500 ~ 502)
- `INTERNAL_ERROR`: 내부 서버 오류
- `DATABASE_ERROR`: 데이터베이스 작업 실패
- `CACHE_ERROR`: 캐시 작업 실패

## 🔧 설정

### 애플리케이션 설정
```yaml
app:
  youtube:
    api:
      quota-limit: 10000        # YouTube API 일일 할당량
      quota-window: 86400       # 할당량 윈도우 (초)
      batch-size: 100           # 배치 처리 크기
      retry-attempts: 3         # 재시도 횟수
      retry-delay: 1000         # 재시도 지연 (밀리초)
      parallel-threads: 4       # 병렬 처리 스레드 수
  cache:
    ttl:
      post-metrics: 300         # 게시물 메트릭 캐시 TTL (초)
      account-metrics: 300      # 계정 메트릭 캐시 TTL (초)
      comments: 120             # 댓글 캐시 TTL (초)
  ai:
    analysis:
      url: http://localhost:8081/analyze  # AI 분석 서버 URL
```

### Actuator 설정
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
  metrics:
    export:
      prometheus:
        enabled: true
```

## 📈 최신 업데이트 (2024년)

### 엔티티 최적화
- **중복 필드 제거**: FK로 충분히 조회 가능한 필드들 제거
- **시간 필드 통합**: `crawledAt` → `createdAt` 기반으로 통일
- **사용자 식별 개선**: `authorName` → `authorId`로 변경
- **저장 공간 절약**: 불필요한 필드 제거로 DB 용량 최적화

### 날짜 처리 최적화
- **LocalDate → Date 변경**: 타임존 일관성을 위한 날짜 타입 통일
- **날짜 필터링 단순화**: 복잡한 범위 검색에서 직접 날짜 비교로 변경
- **Repository 메서드 최적화**: 날짜 전용 조회 메서드 추가
- **성능 향상**: 불필요한 날짜 변환 로직 제거

### AI 분석 통합
- **SentimentType Enum**: 감정 분석 결과를 위한 타입 안전한 enum
- **AiAnalysisRequest/AiAnalysisResponse DTO**: AI 서버와의 통신을 위한 전용 DTO
- **EmotionAnalysisService**: 감정 분석 로직을 담당하는 서비스
- **키워드 긍정/부정 분리**: AI 서버 요청/응답에서 키워드를 긍정/부정으로 분리

### 쿼리 최적화
- **인덱스 활용**: `createdAt` 기반 인덱스로 조회 성능 향상
- **FK 관계 활용**: 중복 데이터 제거로 조인 성능 개선
- **메모리 효율성**: 엔티티 크기 감소로 메모리 사용량 최적화
- **날짜 필터링 최적화**: `DATE(createdAt) = DATE(:date)` 쿼리로 정확한 날짜 매칭
- **키워드 조회 최적화**: DB 조회 2번 → 1번으로 최적화

### 데이터 일관성
- **정규화 강화**: 중복 데이터 제거로 데이터 일관성 확보
- **관계 명확화**: FK 관계를 통한 명확한 데이터 관계
- **무결성 보장**: 중복 필드 제거로 데이터 무결성 향상

### 히스토리/실시간 분리
- **완전한 분리**: 히스토리 조회와 실시간 조회를 완전히 분리
- **명확한 역할**: 각 API의 역할과 책임 명확화
- **성능 최적화**: 불필요한 실시간 데이터 조회 로직 제거
