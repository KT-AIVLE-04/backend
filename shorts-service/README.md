# Shorts Service

**AI 기반 숏폼 콘텐츠 생성 서비스**

AI를 활용하여 숏폼 비디오 콘텐츠를 자동으로 생성하는 서비스입니다. 사용자의 매장 정보를 기반으로 맞춤형 시나리오를 생성하고, AI 서비스와 연동하여 실제 영상 콘텐츠를 제작합니다.

## 📋 개요

- **포트**: 8083
- **주요 기능**: AI 시나리오 생성, AI 비디오 생성, 비동기 작업 처리, S3 미디어 관리
- **프레임워크**: Spring Boot, Spring WebFlux, AWS S3

## 🔧 주요 기능

### 1. AI 시나리오 생성
- **매장 맞춤형 시나리오**: 매장 정보를 기반으로 한 개인화된 콘텐츠
- **업종별 템플릿**: 업종에 따른 최적화된 시나리오 생성
- **AI 기반 텍스트 생성**: FastAPI AI 서비스 연동

### 2. AI 비디오 생성
- **이미지 기반 비디오 제작**: 업로드된 이미지를 활용한 영상 생성
- **자동 편집**: AI 기반 자동 영상 편집 및 효과 적용
- **다양한 포맷 지원**: 다양한 소셜미디어 플랫폼 대응

### 3. 비동기 작업 처리
- **Job 오케스트레이션**: 복잡한 비디오 생성 작업의 단계별 관리
- **진행률 추적**: 실시간 작업 진행률 모니터링
- **상태 관리**: 작업 상태별 적절한 응답 제공

### 4. 미디어 관리
- **S3 통합**: AWS S3를 활용한 미디어 파일 저장
- **Presigned URL**: 보안된 파일 접근 제어
- **파일 형식 지원**: 이미지 (JPG, PNG) 및 비디오 (MP4) 지원

## 🏗️ 아키텍처

### 헥사고날 아키텍처 구조
```
├── application/               # 애플리케이션 계층
│   ├── port/in/              # 인바운드 포트
│   │   ├── ShortsUseCase.java
│   │   ├── command/          # 커맨드 객체
│   │   │   ├── CreateScenarioCommand.java
│   │   │   ├── CreateShortsCommand.java
│   │   │   └── SaveShortsCommand.java
│   │   └── dto/              # 애플리케이션 DTO
│   ├── port/out/             # 아웃바운드 포트
│   │   ├── ai/               # AI 서비스 포트
│   │   │   └── shorts/
│   │   │       └── AiShortsPort.java
│   │   ├── event/            # 이벤트 포트
│   │   │   ├── contents/
│   │   │   └── store/
│   │   ├── job/              # 작업 관리 포트
│   │   │   └── JobStore.java
│   │   └── s3/               # S3 포트
│   │       └── MediaStoragePort.java
│   └── service/              # 애플리케이션 서비스
│       ├── ShortsService.java
│       ├── JobOrchestrator.java
│       └── mapper/           # 매퍼
└── adapter/                   # 어댑터 계층
    ├── in/web/               # 웹 어댑터
    │   ├── ShortsController.java
    │   ├── dto/              # 웹 DTO
    │   └── mapper/           # 매퍼
    └── out/                  # 아웃바운드 어댑터
        ├── ai/               # AI 서비스 어댑터
        │   └── shorts/
        │       ├── AiShortsAdapter.java
        │       └── dto/
        ├── event/            # 이벤트 어댑터
        │   ├── contents/
        │   ├── store/
        │   └── kafka/
        ├── job/              # 작업 관리 어댑터
        │   └── InMemoryJobStore.java
        └── s3/               # S3 어댑터
            ├── S3StorageAdapter.java
            └── dto/
```

### 주요 컴포넌트

#### ShortsService
```java
@Service
public class ShortsService implements ShortsUseCase {
    // 시나리오 생성 비즈니스 로직
    // 숏폼 생성 워크플로우 관리
}
```

#### JobOrchestrator
```java
@Service
public class JobOrchestrator {
    // 비동기 작업 오케스트레이션
    // 작업 진행률 시뮬레이션
    // 상태 관리 및 알림
}
```

#### AiShortsAdapter
```java
@Component
public class AiShortsAdapter implements AiShortsPort {
    // FastAPI AI 서비스와의 통신
    // 시나리오 및 비디오 생성 요청 처리
}
```

## ⚙️ API 엔드포인트

### 시나리오 생성 API

#### 시나리오 생성 요청
```http
POST /api/shorts/scenarios
Authorization: Bearer {token}
Content-Type: application/json

{
    "storeId": 1,
    "prompt": "우리 카페의 특별한 음료를 소개하는 영상을 만들어주세요"
}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "시나리오 생성 완료",
    "data": {
        "id": "scenario-123",
        "content": "따뜻한 카페 분위기 속에서 바리스타가 정성스럽게 내리는 커피... [시나리오 내용]",
        "estimatedDuration": 30,
        "suggestedImages": [
            "카페 외관",
            "바리스타 모습",
            "커피 제조 과정",
            "완성된 음료"
        ]
    }
}
```

### 숏폼 생성 API

#### 숏폼 생성 요청
```http
POST /api/shorts/create
Authorization: Bearer {token}
Content-Type: multipart/form-data

storeId: 1
scenario: "시나리오 내용..."
images: [file1.jpg, file2.jpg, file3.jpg]
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "숏폼 생성이 시작되었습니다",
    "data": {
        "jobId": "job-456789",
        "status": "PROCESSING",
        "estimatedTime": 180
    }
}
```

#### 작업 상태 조회
```http
GET /api/shorts/jobs/{jobId}/status
Authorization: Bearer {token}
```

**응답 (진행 중):**
```json
{
    "code": "SUCCESS",
    "message": "작업 진행 중",
    "data": {
        "jobId": "job-456789",
        "status": "PROCESSING",
        "progress": 65,
        "currentStep": "비디오 편집 중",
        "estimatedTimeRemaining": 63
    }
}
```

**응답 (완료):**
```json
{
    "code": "SUCCESS",
    "message": "작업 완료",
    "data": {
        "jobId": "job-456789",
        "status": "COMPLETED",
        "progress": 100,
        "result": {
            "videoUrl": "https://example.com/shorts/video-123.mp4",
            "thumbnailUrl": "https://example.com/shorts/thumb-123.jpg",
            "duration": 30,
            "size": 15728640
        }
    }
}
```

#### 숏폼 저장
```http
POST /api/shorts/save
Authorization: Bearer {token}
Content-Type: application/json

{
    "jobId": "job-456789",
    "title": "우리 카페의 특별한 커피",
    "description": "바리스타가 정성스럽게 내리는 특별한 커피를 소개합니다"
}
```

## 🤖 AI 서비스 연동

### FastAPI AI 서비스 통신

#### 시나리오 생성 요청
```json
POST http://localhost:8000/api/ai/scenario
{
    "storeInfo": {
        "name": "맛있는 카페",
        "industry": "CAFE",
        "description": "따뜻한 분위기의 동네 카페"
    },
    "prompt": "우리 카페의 특별한 음료를 소개하는 영상을 만들어주세요",
    "targetDuration": 30
}
```

#### 비디오 생성 요청
```json
POST http://localhost:8000/api/ai/shorts
{
    "scenario": "시나리오 텍스트...",
    "imageUrls": [
        "https://s3.amazonaws.com/bucket/image1.jpg",
        "https://s3.amazonaws.com/bucket/image2.jpg"
    ],
    "style": "modern",
    "duration": 30
}
```

## 🎯 작업 오케스트레이션

### Job 상태 흐름
```
PENDING → PROCESSING → COMPLETED
                    ↓
                   FAILED
```

### 진행 단계별 처리
```java
public class JobOrchestrator {
    private final Map<String, Integer> STEP_PROGRESS = Map.of(
        "이미지 업로드 중", 10,
        "시나리오 분석 중", 25,
        "AI 영상 생성 중", 60,
        "비디오 편집 중", 85,
        "최종 처리 중", 95,
        "완료", 100
    );
}
```

## ⚙️ 설정

### S3 설정
```yaml
cloud:
  aws:
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
    region:
      static: ${AWS_REGION:ap-northeast-2}
    s3:
      bucket:
        shorts: marketing-shorts-bucket
        temp: marketing-temp-bucket
```

### AI 서비스 설정
```yaml
ai:
  service:
    base-url: ${AI_SERVICE_URL:http://localhost:8000}
    timeout: 300s
    retry:
      max-attempts: 3
      backoff-delay: 2s
```

### 비동기 처리 설정
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 5
        max-size: 20
        queue-capacity: 100
    scheduling:
      pool:
        size: 5
```

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- FastAPI AI Service (localhost:8000)
- AWS S3 접근 권한
- Kafka (이벤트 통신용)

### AI 서비스 실행
```bash
# FastAPI AI 서비스 실행 (별도 리포지토리)
cd ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

### Shorts Service 실행
```bash
# Gradle을 통한 실행
./gradlew :shorts-service:bootRun

# JAR 파일 실행
java -jar shorts-service/build/libs/shorts-service-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-shorts-service .
docker run -p 8083:8083 marketing-shorts-service
```

### 환경 변수 설정
```bash
# AWS S3
export AWS_ACCESS_KEY=your-aws-access-key
export AWS_SECRET_KEY=your-aws-secret-key
export AWS_REGION=ap-northeast-2

# AI Service
export AI_SERVICE_URL=http://localhost:8000

# Kafka
export KAFKA_SERVERS=localhost:9092
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :shorts-service:test
```

### Integration Tests
```bash
./gradlew :shorts-service:integrationTest
```

### API 테스트 예시
```bash
# 시나리오 생성 테스트
curl -X POST http://localhost:8083/api/shorts/scenarios \
  -H "Authorization: Bearer {your-jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": 1,
    "prompt": "우리 카페의 특별한 음료를 소개하는 영상을 만들어주세요"
  }'

# 작업 상태 조회 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  http://localhost:8083/api/shorts/jobs/{jobId}/status
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

### Metrics
- 시나리오 생성 요청 수
- 비디오 생성 성공/실패률
- 평균 처리 시간
- AI 서비스 응답 시간
- S3 업로드/다운로드 통계

### Custom Metrics
```java
@Component
public class ShortsMetrics {
    private final Counter scenarioCreated;
    private final Counter videoCreated;
    private final Timer processingTime;
    
    // 메트릭 수집 로직
}
```

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. AI 서비스 연결 실패
- **증상**: 시나리오/비디오 생성 실패
- **원인**: AI 서비스 불가용 또는 네트워크 오류
- **해결**: AI 서비스 상태 확인, 재시도 로직 동작 확인

#### 2. S3 업로드 실패
- **증상**: 이미지 업로드 또는 결과 저장 실패
- **원인**: AWS 자격 증명 오류 또는 권한 부족
- **해결**: AWS 설정 및 IAM 권한 확인

#### 3. 작업 진행률 업데이트 안됨
- **증상**: 작업 상태가 계속 PROCESSING
- **원인**: 비동기 작업 스케줄러 오류
- **해결**: 스레드 풀 설정 및 스케줄러 상태 확인

#### 4. 파일 형식 지원 오류
- **증상**: 업로드된 파일 처리 실패
- **원인**: 지원하지 않는 파일 형식
- **해결**: 파일 검증 로직 및 지원 형식 확인

## 📈 성능 최적화

### 이미지 처리 최적화
```java
@Service
public class ImageOptimizer {
    // 이미지 리사이징
    // 포맷 최적화
    // 압축률 조정
}
```

### 비동기 처리 최적화
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 10      # 코어 스레드 수 증가
        max-size: 50       # 최대 스레드 수 증가
        keep-alive: 60s    # 유휴 스레드 유지 시간
```

### 캐싱 전략
```java
@Service
public class ScenarioCache {
    // 시나리오 템플릿 캐싱
    // 매장별 시나리오 캐싱
    // AI 응답 결과 캐싱
}
```

## 🔧 운영 고려사항

### 로깅 설정
```yaml
logging:
  level:
    kt.aivle.shorts: INFO
    org.springframework.web.reactive: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 보안 고려사항
- 업로드 파일 크기 제한
- 파일 형식 검증 강화
- S3 Presigned URL 만료 시간 관리
- AI 서비스 API 키 보안

### 확장성 고려사항
- AI 서비스 로드 밸런싱
- 분산 작업 큐 시스템 도입
- 비디오 처리 전용 서버 분리
- CDN 통합으로 전송 최적화

---

**서비스 담당**: Shorts Team  
**최종 업데이트**: 2024년