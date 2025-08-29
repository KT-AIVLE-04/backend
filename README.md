# Marketing Platform

**AI 기반 소셜미디어 마케팅 자동화 플랫폼**

이 프로젝트는 Spring Boot 기반의 마이크로서비스 아키텍처로 구축된 마케팅 자동화 플랫폼입니다. AI를 활용한 숏폼 콘텐츠 생성과 YouTube 업로드 자동화를 통해 효율적인 소셜미디어 마케팅을 지원합니다.

## 🏗️ 아키텍처

### 마이크로서비스 구성

| 서비스 | 포트 | 설명 | 주요 기능 |
|--------|------|------|-----------|
| **gateway** | 8080 | API 게이트웨이 | 인증, 라우팅, CORS |
| **auth-service** | 8081 | 인증/인가 서비스 | JWT, OAuth2, 사용자 관리 |
| **store-service** | 8082 | 매장 관리 서비스 | 매장 정보 CRUD, 위치 기반 서비스 |
| **shorts-service** | 8083 | 숏폼 콘텐츠 생성 서비스 | AI 시나리오 생성, 비디오 생성 |
| **content-service** | 8084 | 콘텐츠 관리 서비스 | 미디어 업로드, CDN, 메타데이터 |
| **sns-service** | 8085 | SNS 연동 서비스 | YouTube API, 채널 관리, 게시물 업로드 |
| **analytics-service** | 8086 | 분석 서비스 | 성과 분석, 감정 분석, 배치 처리 |
| **common** | - | 공통 모듈 | 공통 유틸리티, 예외 처리 |

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.5.4** - 메인 프레임워크
- **Spring Cloud 2025.0.0** - 마이크로서비스 인프라
- **Spring Cloud Gateway** - API 게이트웨이
- **Java 17** - 런타임 환경

### Database & Cache
- **MySQL 8.0** - 주 데이터베이스
- **Redis 7.2** - 캐싱, 세션 관리
- **Spring Data JPA** - ORM

### Message Broker
- **Apache Kafka 7.4.3** - 이벤트 스트리밍

### Cloud & Storage
- **AWS S3** - 미디어 스토리지
- **AWS CloudFront** - CDN

### External APIs
- **YouTube Data API v3** - 동영상 업로드/관리
- **Google OAuth2** - 소셜 로그인
- **Kakao OAuth2** - 소셜 로그인
- **FastAPI AI Service** - AI 콘텐츠 생성

### Security & Authentication
- **JWT** - 토큰 기반 인증
- **OAuth2** - 소셜 로그인
- **Spring Security** - 보안 프레임워크

### Build & Deployment
- **Gradle** - 빌드 도구
- **Docker & Docker Compose** - 컨테이너화
- **Multi-module Project** - 모듈화된 구조

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 7.2+
- Kafka 7.4.3+

### Environment Setup

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd marketing
   ```

2. **Environment Configuration**
   각 서비스의 `application.yml` 파일에서 다음 설정을 구성하세요:
   - Database connection strings
   - Redis configuration
   - Kafka broker settings
   - AWS credentials (S3, CloudFront)
   - YouTube API credentials
   - OAuth client credentials

3. **Build Project**
   ```bash
   ./gradlew clean build
   ```

4. **Start Infrastructure Services**
   ```bash
   docker-compose up -d mysql redis kafka zookeeper
   ```

5. **Run Services**
   ```bash
   # Start services in order
   ./gradlew :gateway:bootRun
   ./gradlew :auth-service:bootRun
   ./gradlew :store-service:bootRun
   ./gradlew :content-service:bootRun
   ./gradlew :sns-service:bootRun
   ./gradlew :shorts-service:bootRun
   ./gradlew :analytics-service:bootRun
   ```

### Using Docker Compose

```bash
# Development environment
docker-compose up

# Production environment
docker-compose -f docker-compose.prod.yml up
```

## 📱 주요 기능

### 1. 사용자 인증 및 관리
- 이메일/비밀번호 로그인
- Google/Kakao 소셜 로그인
- JWT 토큰 기반 인증
- 계정 보안 (로그인 실패 추적, 계정 잠금)

### 2. 매장 관리
- 매장 정보 등록/수정/삭제
- 업종별 분류 (음식점, 카페, 패션, 뷰티, 기술)
- 위치 기반 서비스 (위도/경도)

### 3. AI 숏폼 콘텐츠 생성
- AI 기반 시나리오 자동 생성
- 이미지/비디오 콘텐츠 생성
- 비동기 작업 처리 및 진행률 추적
- S3 기반 미디어 관리

### 4. 콘텐츠 관리
- 이미지/비디오 업로드
- 메타데이터 자동 추출
- 썸네일 자동 생성
- CloudFront CDN 통합

### 5. SNS 플랫폼 연동
- YouTube 채널 관리
- 동영상 업로드/수정/삭제
- OAuth 기반 소셜 플랫폼 연동
- 채널 동기화

### 6. 분석 및 리포팅
- 실시간 성과 분석
- 감정 분석 (AI 기반)
- 배치 처리를 통한 히스토리컬 데이터 분석
- 성과 지표 추적 (조회수, 팔로워, 댓글 등)

## 🔧 Configuration

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/marketing
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
```

### Kafka Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    consumer:
      group-id: marketing-platform
```

### AWS Configuration
```yaml
cloud:
  aws:
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
    region:
      static: ${AWS_REGION:ap-northeast-2}
```

## 📊 API Documentation

각 서비스는 Swagger/OpenAPI 3.0 문서를 제공합니다:

- **Gateway**: http://localhost:8080/swagger-ui.html
- **Auth Service**: http://localhost:8081/swagger-ui.html
- **Store Service**: http://localhost:8082/swagger-ui.html
- **Shorts Service**: http://localhost:8083/swagger-ui.html
- **Content Service**: http://localhost:8084/swagger-ui.html
- **SNS Service**: http://localhost:8085/swagger-ui.html
- **Analytics Service**: http://localhost:8086/swagger-ui.html

## 🔒 Security

### Authentication Flow
1. 사용자 로그인 (로컬 또는 OAuth)
2. JWT Access Token (2시간) + Refresh Token (14일) 발급
3. API Gateway에서 토큰 검증
4. 사용자 컨텍스트를 헤더를 통해 각 서비스에 전달

### Token Management
- Access Token: 2시간 유효
- Refresh Token: 14일 유효, Redis에 저장
- Token Blacklisting: 로그아웃 시 토큰 무효화

## 🎯 Inter-Service Communication

### Synchronous
- HTTP REST API (API Gateway를 통한 라우팅)
- 직접적인 서비스 간 호출

### Asynchronous (Kafka)
- **post.created/deleted**: SNS 서비스 이벤트
- **store.request/reply**: 매장 정보 교환
- **content.request/reply**: 콘텐츠 서비스 연동
- **account.sync**: SNS-Analytics 간 계정 동기화

## 🧪 Testing

```bash
# Unit Tests
./gradlew test

# Integration Tests
./gradlew integrationTest

# Service-specific tests
./gradlew :auth-service:test
./gradlew :shorts-service:test
```

## 📈 Monitoring & Logging

### Application Monitoring
- Spring Boot Actuator endpoints
- Health checks for all services
- Metrics collection

### Logging
- Structured logging with JSON format
- Log aggregation across microservices
- Request/response logging at gateway level

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

문의사항이 있으시면 다음 채널을 이용해 주세요:
- 이슈 등록: GitHub Issues
- 개발팀 연락: [연락처 정보]

---

**개발팀**: KT AIVLE School 4기  
**프로젝트 버전**: 0.0.1-SNAPSHOT