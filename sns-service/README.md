# SNS Service

**소셜미디어 플랫폼 연동 서비스**

YouTube를 중심으로 한 소셜미디어 플랫폼과의 연동을 담당하는 서비스입니다. OAuth2 기반 인증, 채널 관리, 비디오 업로드/관리, AI 기반 콘텐츠 생성 등의 기능을 제공하며, 다른 서비스와의 이벤트 기반 통신을 지원합니다.

## 📋 개요

- **포트**: 8085
- **주요 기능**: YouTube API 연동, OAuth2 인증, 채널 동기화, 비디오 관리, AI 콘텐츠 생성
- **프레임워크**: Spring Boot, YouTube Data API v3, Google OAuth2

## 🔧 주요 기능

### 1. YouTube API 통합
- **채널 관리**: 채널 정보 조회 및 동기화
- **비디오 업로드**: 새 비디오 업로드 및 메타데이터 설정
- **비디오 관리**: 기존 비디오 수정, 삭제, 상태 변경
- **검색 기능**: 채널 내 비디오 검색
- **쿼터 관리**: YouTube API 사용량 최적화

### 2. OAuth2 인증 시스템
- **Google OAuth2**: YouTube 채널 접근 권한 획득
- **토큰 관리**: Access Token 및 Refresh Token 자동 갱신
- **다중 계정 지원**: 사용자당 여러 YouTube 채널 연동
- **보안 상태 관리**: OAuth 플로우 보안을 위한 state 관리

### 3. 채널 동기화
- **자동 동기화**: 주기적인 채널 정보 및 통계 업데이트
- **실시간 업데이트**: 변경사항 즉시 반영
- **계정 연결**: 로컬 계정과 YouTube 채널 매핑
- **메트릭 수집**: 구독자, 조회수, 비디오 수 등 통계 수집

### 4. AI 기반 콘텐츠 생성
- **AI 게시물 생성**: 매장 정보 기반 YouTube 콘텐츠 자동 생성
- **태그 자동 생성**: AI 기반 비디오 태그 추천
- **최적화된 메타데이터**: SEO 최적화된 제목, 설명 생성

### 5. 이벤트 기반 통신
- **Kafka 통합**: 다른 서비스와의 비동기 통신
- **계정 이벤트**: 계정 생성/삭제/업데이트 이벤트 발행
- **게시물 이벤트**: 비디오 업로드/삭제 이벤트 발행

## 🏗️ 아키텍처

### 헥사고날 아키텍처 구조
```
├── domain/                    # 도메인 계층
│   └── model/                 # 도메인 모델
│       ├── SnsAccount.java   # SNS 계정 엔티티
│       ├── SnsToken.java     # OAuth 토큰 엔티티
│       ├── PostEntity.java   # 게시물 엔티티
│       ├── RefreshedToken.java # 갱신된 토큰
│       └── SnsType.java      # SNS 타입 enum
├── application/               # 애플리케이션 계층
│   ├── port/in/              # 인바운드 포트
│   │   ├── SnsAccountUseCase.java
│   │   ├── SnsOAuthUseCase.java
│   │   ├── SnsPostUseCase.java
│   │   ├── AccountSyncUseCase.java
│   │   ├── AiSnsUseCase.java
│   │   ├── TokenServiceUseCase.java
│   │   └── TokenRefresher.java
│   ├── port/out/             # 아웃바운드 포트
│   │   ├── SnsAccountRepositoryPort.java
│   │   ├── SnsTokenRepositoryPort.java
│   │   ├── PostRepositoryPort.java
│   │   └── EventPublisherPort.java
│   ├── service/              # 애플리케이션 서비스
│   │   ├── SnsAccountDelegator.java
│   │   ├── SnsOAuthDelegator.java
│   │   ├── SnsPostDelegator.java
│   │   ├── AccountSyncDelegator.java
│   │   ├── AiSnsPostService.java
│   │   └── oauth/            # OAuth 서비스
│   │       └── OAuthStateService.java
│   ├── event/                # 도메인 이벤트
│   │   ├── PostEvent.java
│   │   └── SnsAccountEvent.java
│   └── messaging/            # 메시징
│       └── Topics.java
└── adapter/                   # 어댑터 계층
    ├── in/web/               # 웹 어댑터
    │   ├── SnsAccountController.java
    │   ├── SnsOAuthController.java
    │   ├── SnsPostController.java
    │   ├── BuildCookie.java
    │   └── dto/              # 웹 DTO
    └── out/                  # 아웃바운드 어댑터
        ├── persistence/      # 데이터베이스 어댑터
        │   ├── SnsAccountPersistenceAdapter.java
        │   ├── SnsTokenPersistenceAdapter.java
        │   ├── PostPersistenceAdapter.java
        │   └── repository/   # JPA 리포지토리
        ├── youtube/          # YouTube API 어댑터
        │   ├── YoutubeChannelListApi.java
        │   ├── YoutubeChannelUpdateApi.java
        │   ├── YoutubeVideoInsertApi.java
        │   ├── YoutubeVideoUpdateApi.java
        │   ├── YoutubeVideoDeleteApi.java
        │   ├── YoutubeSearchListApi.java
        │   ├── YoutubeCredentialProvider.java
        │   └── YoutubeClientFactory.java
        ├── kafka/            # Kafka 어댑터
        │   └── SnsEventPublisher.java
        └── infra/            # 인프라 어댑터
            ├── S3Storage.java
            └── CloudFrontSigner.java
```

### YouTube 서비스 구조
```
├── youtube/
│   ├── YoutubeAccountSyncService.java      # 계정 동기화
│   ├── YoutubeChannelService.java          # 채널 관리
│   ├── YoutubeOAuthService.java            # OAuth 처리
│   ├── YoutubeSnsPostService.java          # 게시물 관리
│   ├── YoutubeTokenRefresher.java          # 토큰 갱신
│   └── YoutubeTokenService.java            # 토큰 서비스
```

## 🗄️ 데이터베이스 스키마

### SnsAccount 테이블
```sql
CREATE TABLE sns_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    store_id BIGINT,
    sns_type VARCHAR(20) NOT NULL DEFAULT 'YOUTUBE',
    sns_account_id VARCHAR(255) NOT NULL,
    sns_account_name VARCHAR(255) NOT NULL,
    sns_account_description TEXT,
    sns_account_url VARCHAR(500),
    follower BIGINT DEFAULT 0,
    post_count INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_sns (user_id, sns_type, sns_account_id),
    INDEX idx_store_id (store_id)
);
```

### SnsToken 테이블
```sql
CREATE TABLE sns_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sns_type VARCHAR(20) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    token_type VARCHAR(50) DEFAULT 'Bearer',
    expires_at TIMESTAMP,
    scope TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_sns_token (user_id, sns_type)
);
```

### PostEntity 테이블
```sql
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sns_account_id BIGINT NOT NULL,
    sns_post_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    post_url VARCHAR(500),
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    comment_count BIGINT DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_sns_account_id (sns_account_id),
    UNIQUE INDEX idx_sns_post (sns_account_id, sns_post_id)
);
```

## ⚙️ API 엔드포인트

### OAuth 인증 API

#### YouTube OAuth 인증 시작
```http
GET /api/sns/oauth/youtube/authorize
Authorization: Bearer {token}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "OAuth 인증 URL 생성",
    "data": {
        "authUrl": "https://accounts.google.com/o/oauth2/auth?client_id=...&redirect_uri=...&scope=...&state=...",
        "state": "unique-state-string"
    }
}
```

#### YouTube OAuth 콜백 처리
```http
GET /api/sns/oauth/youtube/callback?code={authCode}&state={state}
```

### 계정 관리 API

#### SNS 계정 목록 조회
```http
GET /api/sns/accounts
Authorization: Bearer {token}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "계정 목록 조회 성공",
    "data": [
        {
            "id": 1,
            "snsType": "YOUTUBE",
            "snsAccountId": "UC1234567890",
            "snsAccountName": "내 유튜브 채널",
            "snsAccountDescription": "마케팅 전문 채널",
            "snsAccountUrl": "https://youtube.com/channel/UC1234567890",
            "follower": 1500,
            "postCount": 25,
            "viewCount": 50000,
            "createdAt": "2024-01-01T10:00:00"
        }
    ]
}
```

#### 계정 동기화
```http
POST /api/sns/accounts/{accountId}/sync
Authorization: Bearer {token}
```

#### 계정 연결 해제
```http
DELETE /api/sns/accounts/{accountId}
Authorization: Bearer {token}
```

### 게시물 관리 API

#### 비디오 업로드
```http
POST /api/sns/posts/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

snsAccountId: 1
title: "우리 매장 소개 영상"
description: "새로운 메뉴를 소개합니다"
tags: ["맛집", "신메뉴", "추천"]
categoryId: "22"
privacyStatus: "public"
videoFile: video.mp4
thumbnailFile: thumbnail.jpg
```

#### 비디오 정보 수정
```http
PUT /api/sns/posts/{postId}
Authorization: Bearer {token}
Content-Type: application/json

{
    "title": "수정된 제목",
    "description": "수정된 설명",
    "tags": ["업데이트된", "태그"],
    "privacyStatus": "public"
}
```

#### 비디오 삭제
```http
DELETE /api/sns/posts/{postId}
Authorization: Bearer {token}
```

#### 게시물 목록 조회
```http
GET /api/sns/posts?snsAccountId=1&page=0&size=10
Authorization: Bearer {token}
```

### AI 콘텐츠 생성 API

#### AI 게시물 생성
```http
POST /api/sns/ai/posts
Authorization: Bearer {token}
Content-Type: application/json

{
    "storeId": 1,
    "prompt": "새로 출시된 시그니처 메뉴를 홍보하는 내용으로 작성해주세요",
    "platform": "YOUTUBE",
    "contentType": "SHORT_FORM"
}
```

#### AI 태그 생성
```http
POST /api/sns/ai/tags
Authorization: Bearer {token}
Content-Type: application/json

{
    "title": "우리 카페의 신메뉴 소개",
    "description": "바리스타가 직접 만든 특별한 라떼",
    "storeInfo": {
        "name": "맛있는 카페",
        "industry": "CAFE"
    }
}
```

## 🎯 YouTube API 통합

### 지원하는 YouTube API 기능

#### 채널 관리
```java
// 채널 정보 조회
public ChannelResponse getChannelInfo(String channelId)

// 채널 통계 업데이트  
public void updateChannelStatistics(String channelId)

// 채널 브랜딩 설정
public void updateChannelBranding(String channelId, BrandingSettings settings)
```

#### 비디오 관리
```java
// 비디오 업로드
public VideoUploadResponse uploadVideo(VideoUploadRequest request)

// 비디오 정보 수정
public void updateVideoDetails(String videoId, VideoUpdateRequest request)

// 비디오 삭제
public void deleteVideo(String videoId)

// 비디오 검색
public SearchResponse searchVideos(SearchRequest request)
```

#### 통계 및 분석
```java
// 비디오 통계 조회
public VideoStatistics getVideoStatistics(String videoId)

// 채널 분석 데이터 조회
public AnalyticsResponse getChannelAnalytics(String channelId, DateRange range)
```

### YouTube API 쿼터 관리

#### 쿼터 사용량 추적
```java
@Component
public class YoutubeQuotaManager {
    // 일일 쿼터 제한: 10,000 units
    // API 호출별 비용 계산
    // 쿼터 초과 시 대기 로직
}
```

## ⚙️ 설정

### YouTube API 설정
```yaml
youtube:
  api:
    key: ${YOUTUBE_API_KEY}
    application-name: Marketing Platform
    quota:
      daily-limit: 10000
      warning-threshold: 8000
  oauth:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${YOUTUBE_OAUTH_REDIRECT_URI}
    scopes:
      - https://www.googleapis.com/auth/youtube
      - https://www.googleapis.com/auth/youtube.upload
```

### AWS S3 설정 (비디오 스토리지)
```yaml
aws:
  s3:
    buckets:
      videos: marketing-youtube-videos
      thumbnails: marketing-youtube-thumbnails
    region: ap-northeast-2
```

### Kafka 설정
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: sns-service
      auto-offset-reset: earliest
```

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- MySQL 8.0+
- Kafka
- YouTube Data API v3 키
- Google OAuth2 애플리케이션

### Google Cloud Console 설정
1. **프로젝트 생성**: Google Cloud Console에서 새 프로젝트 생성
2. **API 활성화**: YouTube Data API v3 활성화
3. **OAuth2 클라이언트**: 웹 애플리케이션용 OAuth2 클라이언트 생성
4. **리디렉션 URI**: 개발/프로덕션 환경 URI 설정

### 실행 방법
```bash
# Gradle을 통한 실행
./gradlew :sns-service:bootRun

# JAR 파일 실행
java -jar sns-service/build/libs/sns-service-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-sns-service .
docker run -p 8085:8085 marketing-sns-service
```

### 환경 변수 설정
```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=marketing
export DB_USERNAME=marketing_user
export DB_PASSWORD=password

# YouTube API
export YOUTUBE_API_KEY=your-youtube-api-key
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export YOUTUBE_OAUTH_REDIRECT_URI=http://localhost:8085/api/sns/oauth/youtube/callback

# AWS S3
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=ap-northeast-2

# Kafka
export KAFKA_SERVERS=localhost:9092
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :sns-service:test
```

### Integration Tests
```bash
./gradlew :sns-service:integrationTest
```

### API 테스트 예시
```bash
# OAuth 인증 URL 생성 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  http://localhost:8085/api/sns/oauth/youtube/authorize

# SNS 계정 목록 조회 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  http://localhost:8085/api/sns/accounts

# AI 게시물 생성 테스트
curl -X POST http://localhost:8085/api/sns/ai/posts \
  -H "Authorization: Bearer {your-jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": 1,
    "prompt": "새 메뉴 홍보 게시물 작성",
    "platform": "YOUTUBE",
    "contentType": "SHORT_FORM"
  }'
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8085/actuator/health
```

### Metrics
- YouTube API 호출 성공/실패율
- OAuth 인증 성공율
- 비디오 업로드 성공율
- API 쿼터 사용량
- 토큰 갱신 성공율

### YouTube API 모니터링
```java
@Component
public class YoutubeApiMetrics {
    private final Counter apiCalls;
    private final Counter quotaUsage;
    private final Timer apiResponseTime;
    
    // YouTube API 메트릭 수집
}
```

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. YouTube API 쿼터 초과
- **증상**: API 호출 시 403 Forbidden 에러
- **원인**: 일일 쿼터 10,000 units 초과
- **해결**: 쿼터 사용량 모니터링, 캐싱 전략 적용

#### 2. OAuth 토큰 만료
- **증상**: API 호출 시 401 Unauthorized 에러
- **원인**: Access Token 만료
- **해결**: Refresh Token을 이용한 자동 갱신

#### 3. 비디오 업로드 실패
- **증상**: 업로드 시 오류 발생
- **원인**: 
  - 파일 크기 제한 초과
  - 지원하지 않는 파일 형식
  - 네트워크 타임아웃
- **해결**: 파일 검증, 재시도 로직, 멀티파트 업로드

#### 4. 채널 동기화 오류
- **증상**: 채널 정보가 업데이트되지 않음
- **원인**: API 권한 부족 또는 채널 접근 제한
- **해결**: OAuth 스코프 확인, 채널 권한 재설정

## 📈 성능 최적화

### API 호출 최적화
```java
@Service
public class YoutubeApiOptimizer {
    // 배치 요청으로 여러 API 호출 통합
    // 캐싱으로 중복 요청 방지
    // 쿼터 사용량 기반 호출 우선순위 설정
}
```

### 토큰 관리 최적화
```java
@Component
public class TokenCache {
    @Cacheable("oauth-tokens")
    public SnsToken getToken(Long userId, SnsType snsType) {
        // 토큰 캐싱으로 DB 조회 최소화
    }
    
    @CacheEvict("oauth-tokens")
    public void evictToken(Long userId, SnsType snsType) {
        // 토큰 갱신 시 캐시 무효화
    }
}
```

## 🔧 운영 고려사항

### 로깅 설정
```yaml
logging:
  level:
    kt.aivle.sns: INFO
    com.google.api.client: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 보안 고려사항
- OAuth2 state 매개변수 검증
- API 키 및 시크릿 보안 저장
- 토큰 암호화 저장
- HTTPS 강제 사용

### 확장성 고려사항
- 다른 SNS 플랫폼 추가 (Instagram, TikTok)
- 대량 비디오 업로드 처리
- 실시간 스트리밍 지원
- 멀티 채널 관리 시스템

---

**서비스 담당**: SNS Team  
**최종 업데이트**: 2024년