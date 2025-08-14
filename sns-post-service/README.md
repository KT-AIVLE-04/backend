# SNS Post Service

SNS 게시물 생성 서비스입니다. FastAPI와 연동하여 AI 기반 게시물과 해시태그를 생성합니다.

## 주요 기능

- **게시물 생성**: AI를 활용한 SNS 게시물 제목 및 본문 생성
- **해시태그 생성**: 게시물 내용 기반 해시태그 자동 생성
- **전체 생성**: 게시물과 해시태그를 한 번에 생성
- **서킷브레이커**: FastAPI 서비스 장애 시 폴백 처리
- **데이터 저장**: 생성된 게시물과 해시태그를 MySQL에 저장

## API 엔드포인트

### 1. 게시물 생성

```
POST /api/sns-posts/generate-post
Headers: X-USER-ID, X-STORE-ID
Body: {
  "contentData": "string",
  "contentType": "image|video",
  "userKeywords": ["string"],
  "snsPlatform": "instagram|facebook|youtube",
  "businessType": "string",
  "location": "string"
}
```

### 2. 해시태그 생성

```
POST /api/sns-posts/generate-hashtags
Body: {
  "postTitle": "string",
  "postContent": "string",
  "userKeywords": ["string"],
  "snsPlatform": "instagram|facebook|youtube",
  "businessType": "string",
  "location": "string"
}
```

### 3. 전체 생성

```
POST /api/sns-posts/generate-full
Headers: X-USER-ID, X-STORE-ID
Body: {
  "contentData": "string",
  "contentType": "image|video",
  "userKeywords": ["string"],
  "snsPlatform": "instagram|facebook|youtube",
  "businessType": "string",
  "location": "string"
}
```

## 기술 스택

- **Spring Boot 3.5.4**: 메인 프레임워크
- **Spring WebFlux**: 비동기 웹 처리
- **Spring Data JPA**: 데이터 접근 계층
- **MySQL 8.0**: 데이터베이스
- **WebClient**: FastAPI 통신
- **Resilience4j**: 서킷브레이커 및 재시도
- **MapStruct**: DTO 매핑
- **Swagger**: API 문서화

## 환경 설정

### 환경 변수

```bash
# Database
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password
MYSQL_ROOT_PASSWORD=your_root_password

# FastAPI
FASTAPI_BASE_URL=http://localhost:8000
FASTAPI_TIMEOUT=5000
```

### 포트

- **8084**: SNS Post Service
- **8000**: FastAPI (AI 서비스)

## 실행 방법

### 로컬 실행

```bash
# 1. MySQL 실행
docker-compose up mysql -d

# 2. FastAPI 실행
docker-compose up fastapi -d

# 3. SNS Post Service 실행
./gradlew :sns-post-service:bootRun
```

### Docker 실행

```bash
# 전체 서비스 실행
docker-compose up -d

# SNS Post Service만 실행
docker-compose up sns-post-service -d
```

## 데이터베이스 스키마

### posts 테이블

- id: 게시물 ID (PK)
- user_id: 사용자 ID
- store_id: 매장 ID
- title: 게시물 제목
- content: 게시물 내용
- location: 위치
- sns_platform: SNS 플랫폼
- business_type: 업종
- content_data: 콘텐츠 데이터
- content_type: 콘텐츠 타입
- user_keywords: 사용자 키워드
- is_public: 공개 여부
- view_count: 조회수
- like_count: 좋아요 수
- comment_count: 댓글 수
- created_at: 생성일시
- updated_at: 수정일시

### hashtags 테이블

- id: 해시태그 ID (PK)
- name: 해시태그명
- post_count: 사용된 게시물 수
- created_at: 생성일시
- updated_at: 수정일시

### post_hashtags 테이블

- id: ID (PK)
- post_id: 게시물 ID (FK)
- hashtag_id: 해시태그 ID (FK)
- created_at: 생성일시
- updated_at: 수정일시

## 모니터링

### 로그 레벨

- `kt.aivle.snspost`: DEBUG
- `org.springframework.web.reactive.function.client.WebClient`: DEBUG

### 서킷브레이커 설정

- 슬라이딩 윈도우 크기: 10
- 실패율 임계값: 50%
- 열린 상태 대기 시간: 10초
- 반열린 상태 허용 호출 수: 5
- 느린 호출 임계값: 50%
- 느린 호출 지속 시간: 2초

## Swagger UI

API 문서는 다음 URL에서 확인할 수 있습니다:

- http://localhost:8084/swagger-ui.html
