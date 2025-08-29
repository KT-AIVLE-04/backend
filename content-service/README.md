# Content Service

**미디어 콘텐츠 관리 서비스**

이미지와 비디오 등 미디어 콘텐츠의 업로드, 저장, 관리를 담당하는 서비스입니다. AWS S3와 CloudFront를 활용한 안전하고 효율적인 미디어 서비스를 제공하며, 메타데이터 추출 및 썸네일 생성 기능을 포함합니다.

## 📋 개요

- **포트**: 8084
- **주요 기능**: 미디어 업로드/관리, 메타데이터 추출, 썸네일 생성, CDN 통합
- **프레임워크**: Spring Boot, AWS S3, CloudFront

## 🔧 주요 기능

### 1. 미디어 업로드 및 저장
- **다중 파일 업로드**: 이미지와 비디오 동시 업로드 지원
- **파일 형식 검증**: 지원되는 미디어 타입 검증
- **S3 멀티버킷 저장**: Origin과 Temp 버킷 분리 관리
- **파일 크기 제한**: 업로드 파일 크기 검증

### 2. 메타데이터 자동 추출
- **이미지 메타데이터**: 해상도(width, height), 파일 크기
- **비디오 메타데이터**: 해상도, 재생 시간, 파일 크기
- **FFmpeg 통합**: 비디오 파일 분석 및 정보 추출
- **자동 Content-Type 감지**: 파일 확장자 기반 MIME 타입 설정

### 3. 썸네일 생성
- **이미지 썸네일**: 다양한 크기의 썸네일 자동 생성
- **비디오 썸네일**: 비디오 첫 프레임 추출
- **최적화**: 웹 표시 최적화된 썸네일 생성
- **자동 리사이징**: 표준 크기로 자동 조정

### 4. CloudFront CDN 통합
- **Signed Cookie**: 보안된 콘텐츠 접근 제어
- **전역 배포**: 전세계 빠른 콘텐츠 전송
- **캐시 최적화**: 효율적인 캐시 정책 적용
- **대역폭 절약**: CDN을 통한 트래픽 분산

## 🏗️ 아키텍처

### 서비스 구조
```
├── controller/               # 컨트롤러 계층
│   ├── ContentController.java # 메인 컨트롤러
│   └── BuildCookie.java      # 쿠키 빌더
├── dto/                      # 데이터 전송 객체
│   ├── request/             # 요청 DTO
│   └── response/            # 응답 DTO
├── entity/                   # 엔티티
│   ├── Content.java         # 콘텐츠 엔티티
│   └── BaseEntity.java      # 기본 엔티티
├── repository/               # 리포지토리
│   └── ContentRepository.java
├── service/                  # 서비스 계층
│   ├── ContentService.java  # 인터페이스
│   ├── ContentServiceImpl.java # 구현체
│   ├── MediaMetadataExtractor.java # 메타데이터 추출
│   └── ThumbnailGenerator.java # 썸네일 생성
├── infra/                    # 인프라 계층
│   ├── S3Storage.java       # S3 스토리지
│   └── CloudFrontSigner.java # CloudFront 서명
├── util/                     # 유틸리티
│   └── ContentTypeUtil.java # 콘텐츠 타입 유틸
└── event/                    # 이벤트 처리
    ├── CreateContentRequestHandler.java
    └── CreateContentRequestMessage.java
```

### 주요 컴포넌트

#### ContentServiceImpl
```java
@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    // 콘텐츠 CRUD 비즈니스 로직
    // 파일 업로드 및 메타데이터 처리
}
```

#### S3Storage
```java
@Component
public class S3Storage {
    // S3 파일 업로드/다운로드
    // 멀티버킷 관리
    // Presigned URL 생성
}
```

#### MediaMetadataExtractor
```java
@Component
public class MediaMetadataExtractor {
    // FFmpeg를 이용한 비디오 메타데이터 추출
    // 이미지 메타데이터 추출
}
```

#### CloudFrontSigner
```java
@Component
public class CloudFrontSigner {
    // Signed Cookie 생성
    // 보안 정책 적용
}
```

## 🗄️ 데이터베이스 스키마

### Content 테이블
```sql
CREATE TABLE contents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    store_id BIGINT,
    title VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    width INT,
    height INT,
    duration_seconds INT,
    bytes BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_store_id (store_id),
    INDEX idx_content_type (content_type)
);
```

## ⚙️ API 엔드포인트

### 콘텐츠 업로드 API

#### 파일 업로드
```http
POST /api/contents/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

storeId: 1
title: "우리 매장 사진"
files: [file1.jpg, file2.jpg, video1.mp4]
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "업로드 완료",
    "data": [
        {
            "id": 1,
            "title": "우리 매장 사진",
            "originalName": "store_image.jpg",
            "contentType": "image/jpeg",
            "width": 1920,
            "height": 1080,
            "bytes": 256000,
            "url": "https://d1234567890.cloudfront.net/contents/store_image.jpg",
            "thumbnailUrl": "https://d1234567890.cloudfront.net/thumbnails/store_image_thumb.jpg"
        }
    ]
}
```

### 콘텐츠 조회 API

#### 콘텐츠 목록 조회
```http
GET /api/contents?page=0&size=10&storeId=1
Authorization: Bearer {token}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "조회 성공",
    "data": {
        "content": [
            {
                "id": 1,
                "title": "우리 매장 사진",
                "originalName": "store_image.jpg",
                "contentType": "image/jpeg",
                "width": 1920,
                "height": 1080,
                "durationSeconds": null,
                "bytes": 256000,
                "url": "https://d1234567890.cloudfront.net/contents/store_image.jpg",
                "thumbnailUrl": "https://d1234567890.cloudfront.net/thumbnails/store_image_thumb.jpg",
                "createdAt": "2024-01-01T10:00:00"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "size": 10,
        "number": 0
    }
}
```

#### 콘텐츠 상세 조회
```http
GET /api/contents/{contentId}
Authorization: Bearer {token}
```

#### 콘텐츠 수정
```http
PUT /api/contents/{contentId}
Authorization: Bearer {token}
Content-Type: application/json

{
    "title": "수정된 제목",
    "storeId": 1
}
```

#### 콘텐츠 삭제
```http
DELETE /api/contents/{contentId}
Authorization: Bearer {token}
```

### 보안 접근 API

#### Signed Cookie 생성
```http
POST /api/contents/signed-cookie
Authorization: Bearer {token}
Content-Type: application/json

{
    "contentIds": [1, 2, 3]
}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "Signed Cookie 생성 완료",
    "data": {
        "cookies": [
            {
                "name": "CloudFront-Policy",
                "value": "eyJ2ZXJzaW9uIjoiMjAxMi0xMC0xN...",
                "domain": ".cloudfront.net",
                "secure": true,
                "httpOnly": true
            },
            {
                "name": "CloudFront-Signature",
                "value": "abc123def456...",
                "domain": ".cloudfront.net",
                "secure": true,
                "httpOnly": true
            },
            {
                "name": "CloudFront-Key-Pair-Id",
                "value": "APKAEIBAERJR2EXAMPLE",
                "domain": ".cloudfront.net",
                "secure": true,
                "httpOnly": true
            }
        ]
    }
}
```

## 📁 파일 처리 흐름

### 업로드 처리 과정
```
1. 파일 수신 및 검증
   ↓
2. S3 업로드 (멀티파트)
   ↓
3. 메타데이터 추출
   ↓
4. 썸네일 생성
   ↓
5. DB 정보 저장
   ↓
6. CloudFront URL 생성
```

### 지원 파일 형식
```java
// 이미지 파일
private static final Set<String> IMAGE_TYPES = Set.of(
    "image/jpeg", "image/png", "image/gif", "image/webp"
);

// 비디오 파일  
private static final Set<String> VIDEO_TYPES = Set.of(
    "video/mp4", "video/quicktime", "video/x-msvideo"
);
```

### 파일 크기 제한
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB      # 단일 파일 최대 크기
      max-request-size: 500MB   # 전체 요청 최대 크기
```

## ⚙️ AWS 설정

### S3 버킷 구성
```yaml
aws:
  s3:
    buckets:
      origin: marketing-content-origin    # 원본 콘텐츠
      temp: marketing-content-temp        # 임시 파일
    region: ap-northeast-2
```

### CloudFront 설정
```yaml
aws:
  cloudfront:
    distribution-domain: d1234567890.cloudfront.net
    key-pair-id: ${CLOUDFRONT_KEY_PAIR_ID}
    private-key-path: ${CLOUDFRONT_PRIVATE_KEY_PATH}
    signed-cookie-duration: 3600  # 1시간
```

### FFmpeg 설정
```yaml
ffmpeg:
  path: ${FFMPEG_PATH:/usr/local/bin/ffmpeg}
  thumbnail:
    time-offset: 1    # 1초 지점에서 썸네일 추출
    width: 320
    height: 240
```

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- MySQL 8.0+
- FFmpeg 설치
- AWS S3/CloudFront 설정
- Kafka (이벤트 처리용)

### FFmpeg 설치
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install ffmpeg

# macOS
brew install ffmpeg

# Windows
# https://ffmpeg.org/download.html에서 다운로드
```

### 실행 방법
```bash
# Gradle을 통한 실행
./gradlew :content-service:bootRun

# JAR 파일 실행
java -jar content-service/build/libs/content-service-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-content-service .
docker run -p 8084:8084 marketing-content-service
```

### 환경 변수 설정
```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=marketing
export DB_USERNAME=marketing_user
export DB_PASSWORD=password

# AWS S3
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=ap-northeast-2
export S3_ORIGIN_BUCKET=marketing-content-origin
export S3_TEMP_BUCKET=marketing-content-temp

# CloudFront
export CLOUDFRONT_DISTRIBUTION_DOMAIN=d1234567890.cloudfront.net
export CLOUDFRONT_KEY_PAIR_ID=your-key-pair-id
export CLOUDFRONT_PRIVATE_KEY_PATH=/path/to/private-key.pem

# FFmpeg
export FFMPEG_PATH=/usr/local/bin/ffmpeg

# Kafka
export KAFKA_SERVERS=localhost:9092
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :content-service:test
```

### Integration Tests
```bash
./gradlew :content-service:integrationTest
```

### API 테스트 예시
```bash
# 파일 업로드 테스트
curl -X POST http://localhost:8084/api/contents/upload \
  -H "Authorization: Bearer {your-jwt-token}" \
  -F "storeId=1" \
  -F "title=테스트 이미지" \
  -F "files=@test-image.jpg"

# 콘텐츠 목록 조회 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  "http://localhost:8084/api/contents?page=0&size=10&storeId=1"
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

### Metrics
- 파일 업로드 성공/실패율
- 평균 업로드 시간
- 스토리지 사용량
- CloudFront 캐시 히트율
- 썸네일 생성 성공률

### Custom Metrics
```java
@Component
public class ContentMetrics {
    private final Counter uploadCount;
    private final Timer uploadDuration;
    private final Gauge storageUsage;
    
    // 메트릭 수집 로직
}
```

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. 파일 업로드 실패
- **증상**: 업로드 시 500 에러
- **원인**: 
  - S3 권한 부족
  - 파일 크기 초과
  - 네트워크 타임아웃
- **해결**: AWS 권한, 파일 크기, 네트워크 상태 확인

#### 2. 메타데이터 추출 실패
- **증상**: 이미지/비디오 정보 누락
- **원인**: FFmpeg 경로 오류 또는 파일 손상
- **해결**: FFmpeg 설치 확인, 파일 무결성 검증

#### 3. 썸네일 생성 실패
- **증상**: 썸네일 이미지가 생성되지 않음
- **원인**: 메모리 부족 또는 이미지 처리 라이브러리 오류
- **해결**: 메모리 설정 조정, 라이브러리 버전 확인

#### 4. CloudFront 접근 오류
- **증상**: 403 Forbidden 에러
- **원인**: Signed Cookie 설정 오류
- **해결**: CloudFront 키 쌍 및 정책 확인

## 📈 성능 최적화

### 파일 처리 최적화
```java
@Configuration
public class AsyncConfig {
    @Bean
    public TaskExecutor fileProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        return executor;
    }
}
```

### S3 업로드 최적화
```java
@Service
public class S3OptimizedUpload {
    // 멀티파트 업로드
    // 병렬 업로드
    // 재시도 로직
}
```

### 캐싱 전략
```java
@Service
public class ContentCache {
    @Cacheable("contents")
    public Content findById(Long id) {
        // DB 조회 결과 캐싱
    }
    
    @Cacheable("metadata")
    public MediaMetadata extractMetadata(String objectKey) {
        // 메타데이터 결과 캐싱
    }
}
```

## 🔧 운영 고려사항

### 로깅 설정
```yaml
logging:
  level:
    kt.aivle.content: INFO
    com.amazonaws: WARN
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 보안 고려사항
- 업로드 파일 바이러스 스캔
- 파일 확장자 화이트리스트
- CloudFront Signed URL/Cookie 보안
- 민감한 콘텐츠 접근 제어

### 확장성 고려사항
- CDN 엣지 로케이션 확대
- 이미지 리사이징 서비스 분리
- 비디오 트랜스코딩 파이프라인
- 스토리지 비용 최적화 (Lifecycle Policy)

---

**서비스 담당**: Content Team  
**최종 업데이트**: 2024년