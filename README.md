# 🏗️ Marketing Platform Backend

<div align="center">

**Spring Boot 기반 마이크로서비스 아키텍처**

[![CI/CD](https://github.com/KT-AIVLE-04/backend/actions/workflows/deploy.yml/badge.svg)](https://github.com/KT-AIVLE-04/backend/actions)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://docs.docker.com/compose/)

*AI 기반 소셜미디어 마케팅 자동화 플랫폼의 백엔드 시스템*

</div>

---

## 📋 목차

- [🏗️ 마이크로서비스 아키텍처](#️-마이크로서비스-아키텍처)
- [🛠️ 기술 스택](#️-기술-스택)
- [🔌 서비스 구성](#-서비스-구성)
- [🐳 Docker 환경](#-docker-환경)
- [🚀 빠른 시작](#-빠른-시작)
- [⚙️ 환경 설정](#️-환경-설정)
- [📊 데이터베이스 설계](#-데이터베이스-설계)
- [🔗 서비스 간 통신](#-서비스-간-통신)
- [📚 API 문서](#-api-문서)
- [🔄 CI/CD 파이프라인](#-cicd-파이프라인)
- [📁 프로젝트 구조](#-프로젝트-구조)
- [💻 개발 가이드](#-개발-가이드)
- [🔧 트러블슈팅](#-트러블슈팅)

---

## 🏗️ 마이크로서비스 아키텍처

### 시스템 구조
<img src="https://raw.githubusercontent.com/KT-AIVLE-04/.github/bc0e3ab66a6f085e4f892bf263864025f48fa997/profile/assets/Microservices.svg" alt="Main" style="border-radius: 10px;"/>

### 핵심 설계 원칙

- **단일 책임**: 각 서비스는 특정 비즈니스 도메인에 집중
- **독립 배포**: 서비스별 독립적인 배포 및 스케일링
- **데이터 독립성**: 각 서비스는 자체 데이터베이스 보유
- **장애 격리**: 한 서비스의 장애가 전체 시스템에 영향을 주지 않음

---

## 🛠️ 기술 스택

### Core Framework
- **Java 17** - LTS 버전으로 안정성과 성능 보장
- **Spring Boot 3.5.4** - 최신 프레임워크 기반 마이크로서비스
- **Spring Cloud 2025.0.0** - 마이크로서비스 인프라 통합

### Microservice Infrastructure
- **Spring Cloud Gateway** - API 게이트웨이, 라우팅, 로드 밸런싱
- **Spring Security 6.x** - 보안 및 인증 처리
- **Spring Data JPA** - 데이터 접근 계층

### Database & Cache
- **MySQL 8.0** - 주 데이터베이스 (각 서비스별 독립 스키마)
- **Redis 7.2** - 세션 저장, 토큰 관리, 캐싱

### Message Queue & Storage
- **Apache Kafka 7.4.3** - 이벤트 스트리밍, 서비스 간 비동기 통신
- **AWS S3** - 미디어 파일 저장소
- **AWS CloudFront** - 글로벌 CDN

### DevOps & Build
- **Gradle 8.x** - 멀티모듈 프로젝트 빌드 도구
- **Docker & Docker Compose** - 컨테이너화 및 오케스트레이션
- **GitHub Actions** - CI/CD 파이프라인

### External APIs
- **YouTube Data API v3** - 동영상 업로드 및 관리
- **Google OAuth2** - 소셜 로그인
- **Kakao OAuth2** - 소셜 로그인
- **FastAPI AI Service** - AI 콘텐츠 생성

---

## 🔌 서비스 구성

### 포트 구성

| 서비스 | 개발 포트 | Docker 내부 | 외부 접근 | 상태 |
|:---:|:---:|:---:|:---:|:---:|
| **Gateway** | 8080 | 8080 | `:8080` | ✅ 실행중 |
| **Auth Service** | 8081 | 8081 | 내부 전용 | ✅ 실행중 |
| **Store Service** | 8082 | 8082 | 내부 전용 | ✅ 실행중 |
| **Content Service** | 8083 | 8083 | 내부 전용 | ✅ 실행중 |
| **SNS Service** | 8084 | 8084 | 내부 전용 | ✅ 실행중 |
| **Shorts Service** | 8085 | 8085 | 내부 전용 | ✅ 실행중 |
| **Analytics Service** | 8086 | 8086 | 내부 전용 | ✅ 실행중 |

> **🔒 보안 설계**: Gateway를 통한 단일 진입점으로 내부 서비스 보호

### 서비스별 역할

<details>
<summary><strong>🔐 Gateway (API Gateway)</strong></summary>

**주요 책임**:
- 모든 외부 요청의 단일 진입점
- JWT 토큰 검증 및 사용자 인증
- 서비스별 요청 라우팅 및 로드 밸런싱
- CORS 정책 관리
- Rate Limiting 및 요청 제한

**핵심 기능**:
- 동적 라우팅: `/api/auth/**` → Auth Service
- 토큰 기반 인증: JWT Access Token 검증
- 사용자 컨텍스트 전파: 헤더를 통한 사용자 정보 전달

</details>

<details>
<summary><strong>👤 Auth Service (인증/인가)</strong></summary>

**주요 책임**:
- 사용자 회원가입, 로그인, 로그아웃
- JWT 토큰 생성 및 관리
- OAuth2 소셜 로그인 (Google, Kakao)
- 사용자 프로필 관리
- 보안 정책 관리 (계정 잠금, 실패 추적)

**핵심 기능**:
- 토큰 관리: Access Token (2시간), Refresh Token (14일)
- 토큰 블랙리스트: Redis 기반 로그아웃 토큰 무효화
- 소셜 로그인: OAuth2 Provider 연동

</details>

<details>
<summary><strong>🏪 Store Service (매장 관리)</strong></summary>

**주요 책임**:
- 매장 정보 CRUD (생성, 조회, 수정, 삭제)
- 업종별 분류 관리 (음식점, 카페, 패션, 뷰티, 기술)
- 위치 기반 서비스 (위도/경도 좌표)
- 매장 검증 및 승인 프로세스

**핵심 기능**:
- 지리적 검색: 위치 기반 매장 검색
- 업종 분류: 표준화된 업종 코드 관리
- 매장 검증: 사업자 정보 확인

</details>

<details>
<summary><strong>📁 Content Service (콘텐츠 관리)</strong></summary>

**주요 책임**:
- 이미지/비디오 업로드 및 저장
- 미디어 메타데이터 추출 및 관리
- 썸네일 자동 생성
- AWS S3 연동 및 CloudFront CDN 관리

**핵심 기능**:
- 파일 업로드: 멀티파트 업로드 지원
- 자동 최적화: 이미지 압축 및 리사이징
- CDN 연동: 글로벌 콘텐츠 배포

</details>

<details>
<summary><strong>📱 SNS Service (소셜미디어 연동)</strong></summary>

**주요 책임**:
- YouTube 채널 관리 및 연동
- 동영상 자동 업로드
- SNS 계정 OAuth 관리
- 게시물 예약 및 스케줄링

**핵심 기능**:
- YouTube API: 동영상 업로드, 수정, 삭제
- 채널 동기화: 채널 정보 실시간 동기화
- 배치 업로드: 대량 콘텐츠 예약 게시

</details>

<details>
<summary><strong>🤖 Shorts Service (AI 콘텐츠 생성)</strong></summary>

**주요 책임**:
- AI 기반 시나리오 자동 생성
- 이미지/비디오 콘텐츠 생성
- 비동기 작업 처리 및 진행률 추적
- FastAPI AI 서버 연동

**핵심 기능**:
- 시나리오 생성: GPT 기반 콘텐츠 작성
- 비동기 처리: 긴 작업의 백그라운드 처리
- 진행률 추적: 실시간 작업 상태 모니터링

</details>

<details>
<summary><strong>📊 Analytics Service (분석)</strong></summary>

**주요 책임**:
- 실시간 성과 분석 및 대시보드
- AI 기반 감정 분석
- 배치 처리를 통한 히스토리컬 데이터 분석
- 성과 지표 추적 (조회수, 팔로워, 댓글 등)

**핵심 기능**:
- 실시간 분석: Kafka 스트림 기반 실시간 데이터 처리
- 배치 분석: 대용량 데이터 일괄 처리
- 감정 분석: 댓글 및 반응 감정 분석

</details>

---

## 🐳 Docker 환경

### 개발 환경 vs 운영 환경

#### 개발 환경 (docker-compose.yml)
```yaml
services:
  # 개발용 - 호스트 포트 직접 바인딩
  gateway:
    ports:
      - "8080:8080"  # 외부 접근 가능
  
  auth-service:
    ports:  
      - "8081:8081"  # 개발/테스트용 직접 접근
  
  # ... 기타 서비스들
```

#### 운영 환경 (docker-compose.prod.yml)
```yaml
services:
  gateway:
    ports:
      - "8080:8080"  # 외부 접근 (로드밸런서 뒤)
    
  auth-service:
    # 포트 바인딩 없음 - Gateway를 통해서만 접근
    expose:
      - "8081"
    networks:
      - backend-network
  
  # ... 기타 서비스들 (모두 내부 네트워크만 사용)
```

---

## 🚀 빠른 시작

### 사전 요구사항

```bash
# 필수 소프트웨어 버전 확인
java --version        # Java 17+
docker --version      # Docker 20.10+
docker-compose --version  # Docker Compose 2.0+
```

### 로컬 개발 환경 설정

#### 1. 저장소 클론
```bash
git clone https://github.com/KT-AIVLE-04/backend.git
cd marketing
```

#### 2. 환경변수 설정
```bash
# 환경 파일 생성
cp .env.example .env

# 필수 환경변수 설정
vim .env
```

<details>
<summary>📄 <strong>.env 파일 예시</strong></summary>

```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=marketing_platform
MYSQL_USER=marketing_user
MYSQL_PASSWORD=marketing_password

# Redis Configuration
REDIS_PASSWORD=redis_password

# JWT Configuration
JWT_SECRET_KEY=your-256-bit-secret-key
JWT_ACCESS_EXPIRATION=7200     # 2 hours
JWT_REFRESH_EXPIRATION=1209600 # 14 days

# OAuth2 Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# YouTube API
YOUTUBE_API_KEY=your-youtube-api-key

# AWS Configuration
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_REGION=ap-northeast-2
S3_BUCKET_NAME=your-s3-bucket
CLOUDFRONT_DOMAIN=your-cloudfront-domain

# AI Service
AI_SERVICE_URL=http://ai-service:8000
```

</details>

#### 3. 인프라 서비스 실행
```bash
# MySQL, Redis, Kafka 실행
docker-compose up -d mysql redis kafka zookeeper

# 서비스 상태 확인
docker-compose ps
```

#### 4. 애플리케이션 빌드 및 실행
```bash
# 전체 프로젝트 빌드
./gradlew clean build

# 서비스별 개별 실행 (개발용)
./gradlew :gateway:bootRun &
./gradlew :auth-service:bootRun &
./gradlew :store-service:bootRun &
./gradlew :content-service:bootRun &
./gradlew :sns-service:bootRun &
./gradlew :shorts-service:bootRun &
./gradlew :analytics-service:bootRun &
```

#### 5. Docker Compose 전체 실행 (권장)
```bash
# 전체 시스템 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 특정 서비스 로그만 확인
docker-compose logs -f gateway auth-service
```

### 실행 확인

```bash
# Health Check
curl http://localhost:8080/actuator/health

# API Gateway 상태
curl http://localhost:8080/actuator/gateway/routes

# 각 서비스 Swagger UI 접근
# http://localhost:8080/auth/swagger-ui.html
# http://localhost:8080/store/swagger-ui.html
# http://localhost:8080/content/swagger-ui.html
```

---

## ⚙️ 환경 설정

### 서비스별 설정 파일

각 서비스는 독립적인 `application.yml` 파일을 가집니다:

<details>
<summary>🔐 <strong>Gateway 설정</strong></summary>

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2
            
        - id: store-service  
          uri: http://store-service:8082
          predicates:
            - Path=/api/store/**
          filters:
            - StripPrefix=2
            - AuthFilter

      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
```

</details>

<details>
<summary>👤 <strong>Auth Service 설정</strong></summary>

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://mysql:3306/marketing_auth
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
  
  data:
    redis:
      host: redis
      port: 6379
      password: ${REDIS_PASSWORD}
      
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            
jwt:
  secret: ${JWT_SECRET_KEY}
  access-expiration: ${JWT_ACCESS_EXPIRATION:7200}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:1209600}
```

</details>

### 프로파일별 설정

```bash
# 개발 환경
spring.profiles.active=dev

# 테스트 환경  
spring.profiles.active=test

# 운영 환경
spring.profiles.active=prod
```

---

## 📊 데이터베이스 설계

### ERD (Entity Relationship Diagram)
<img src="https://github.com/KT-AIVLE-04/.github/blob/main/profile/assets/erd.png?raw=true" alt="Main" style="border-radius: 10px;"/>

---

## 📚 API 문서

### Swagger UI 접근

각 서비스별 API 문서는 Gateway를 통해 접근할 수 있습니다:

| 서비스 | Swagger UI | 설명 |
|:---:|:---:|:---|
| **Gateway** | [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | 전체 API 게이트웨이 |
| **Auth** | [localhost:8080/auth/swagger-ui.html](http://localhost:8080/auth/swagger-ui.html) | 인증/인가 API |
| **Store** | [localhost:8080/store/swagger-ui.html](http://localhost:8080/store/swagger-ui.html) | 매장 관리 API |
| **Content** | [localhost:8080/content/swagger-ui.html](http://localhost:8080/content/swagger-ui.html) | 콘텐츠 관리 API |
| **SNS** | [localhost:8080/sns/swagger-ui.html](http://localhost:8080/sns/swagger-ui.html) | SNS 연동 API |
| **Shorts** | [localhost:8080/shorts/swagger-ui.html](http://localhost:8080/shorts/swagger-ui.html) | AI 콘텐츠 생성 API |
| **Analytics** | [localhost:8080/analytics/swagger-ui.html](http://localhost:8080/analytics/swagger-ui.html) | 분석 API |

### API 인증

모든 API 요청은 JWT 토큰 인증이 필요합니다 (인증 API 제외):

```bash
# 1. 로그인으로 토큰 획득
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 7200
}

# 2. API 요청 시 헤더에 토큰 포함
curl -X GET http://localhost:8080/api/store/my-stores \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🔄 CI/CD 파이프라인

### GitHub Actions 워크플로우

우리의 CI/CD는 **변경된 서비스만 선택적으로 배포**하는 스마트한 파이프라인입니다

### 배포 전략

#### 1. Path-based Filtering
```yaml
# 변경된 경로에 따라 서비스 감지
filters:
  gateway:
    - 'gateway/**'
  auth:
    - 'auth-service/**'
  common:
    - 'common/**'     # common 변경 시 모든 서비스 재빌드
  compose:
    - 'docker-compose.prod.yml'  # Docker 설정 변경
```

#### 2. Matrix Parallel Deployment
```yaml
strategy:
  fail-fast: false
  matrix:
    # 동적으로 생성되는 매트릭스
    include:
      - service: "gateway"
        module: "gateway" 
        image: "aivle-gateway"
        compose: "gateway"
        build: true
      - service: "auth-service"
        module: "auth-service"
        image: "aivle-auth"
        compose: "auth-service" 
        build: true
```

#### 3. 스마트 배포 조건

| 조건 | 행동 | 설명 |
|:---|:---|:---|
| **서비스 코드 변경** | 해당 서비스만 빌드/배포 | 효율적인 배포 |
| **common 모듈 변경** | 모든 서비스 재빌드 | 의존성 안정성 |
| **docker-compose 변경** | 설정만 업데이트 | 컨테이너 재시작 |
| **[full-redeploy] PR** | 전체 서비스 재배포 | 강제 전체 배포 |

#### 4. 배포 프로세스

```bash
# 1. 변경 감지 및 매트릭스 생성
echo "Changed services: gateway, auth-service"

# 2. 병렬 빌드 (변경된 서비스만)
./gradlew :gateway:clean :gateway:build
./gradlew :auth-service:clean :auth-service:build

# 3. Docker 이미지 빌드 & 푸시  
docker build -t username/aivle-gateway:latest gateway/
docker push username/aivle-gateway:latest

# 4. AWS 서버 배포
docker-compose pull gateway auth-service
docker-compose up -d --no-deps gateway auth-service

# 5. 헬스체크 및 정리
docker image prune -f
```

### 배포 환경

#### AWS 인프라
- **EC2**: Ubuntu 서버에 Docker Compose로 배포
- **RDS**: MySQL 8.0 관리형 데이터베이스
- **ElastiCache**: Redis 클러스터
- **S3**: 미디어 파일 저장소
- **CloudFront**: 글로벌 CDN

#### 보안 설정
```yaml
# GitHub Secrets에 저장된 환경변수들
secrets:
  DOCKER_USERNAME: Docker Hub 사용자명
  DOCKER_PASSWORD: Docker Hub 패스워드  
  AWS_SECRET_HOST: EC2 서버 IP
  AWS_SECRET_ACCESS_KEY: EC2 SSH 키
  APPLICATION_YML_*: 각 서비스별 설정 파일
  CLOUDFRONT_PRIVATE_KEY: CloudFront 서명 키
```

### 배포 모니터링

```bash
# 배포 상태 확인
curl http://your-server:8080/actuator/health

# 서비스별 상태 확인  
docker-compose ps

# 로그 모니터링
docker-compose logs -f --tail=100 gateway auth-service
```

---

## 📁 프로젝트 구조

```
backend/
├── 📄 README.md                    # 이 파일
├── 📄 build.gradle                 # 루트 빌드 설정
├── 📄 settings.gradle               # 멀티모듈 설정
├── 📄 docker-compose.yml           # 개발용 Docker Compose
├── 📄 docker-compose.prod.yml      # 운영용 Docker Compose
├── 📄 .env.example                 # 환경변수 템플릿
├── 📄 .gitignore                   # Git 무시 파일
│
├── 📁 .github/
│   └── 📁 workflows/
│       └── 📄 deploy.yml           # CI/CD 파이프라인
│
├── 📁 gradle/                      # Gradle Wrapper
│   └── 📁 wrapper/
├── 📄 gradlew                      # Gradle Wrapper 스크립트
├── 📄 gradlew.bat                  # Windows용 Gradle 스크립트
│
├── 📁 common/                      # 공통 모듈
│   ├── 📄 README.md
│   ├── 📄 build.gradle
│   └── 📁 src/
│       └── 📁 main/java/com/marketing/common/
│           ├── 📁 config/          # 공통 설정
│           ├── 📁 dto/             # 공통 DTO
│           ├── 📁 exception/       # 공통 예외 처리
│           └── 📁 util/            # 유틸리티 클래스
│
├── 📁 gateway/                     # API Gateway
│   ├── 📄 README.md
│   ├── 📄 build.gradle
│   ├── 📄 Dockerfile
│   └── 📁 src/
│       ├── 📁 main/
│       │   ├── 📁 java/com/marketing/gateway/
│       │   │   ├── 📁 config/      # Gateway 설정
│       │   │   ├── 📁 filter/      # 커스텀 필터
│       │   │   └── 📄 GatewayApplication.java
│       │   └── 📁 resources/
│       │       └── 📄 application.yml
│       └── 📁 test/
│
├── 📁 auth-service/                # 인증 서비스
├── 📁 store-service/               # 매장 관리 서비스
├── 📁 content-service/             # 콘텐츠 관리 서비스  
├── 📁 sns-service/                 # SNS 연동 서비스
├── 📁 shorts-service/              # AI 콘텐츠 생성 서비스
├── 📁 analytics-service/           # 분석 서비스
│
├── 📁 docs/                        # 문서
    ├── 📄 api-documentation.md     # API 명세서
    ├── 📄 database-schema.md       # DB 스키마
    ├── 📄 deployment-guide.md      # 배포 가이드
    └── 📁 images/                  # 다이어그램 이미지

```

---

## 💻 개발 가이드

### 코딩 컨벤션

#### Java 코딩 스타일
- **Google Java Style Guide** 준수
- **4 spaces** 들여쓰기 (탭 사용 금지)
- **Line length**: 100자 제한
- **Package naming**: `com.marketing.{service}.{layer}`

#### 네이밍 컨벤션
```java
// ✅ 좋은 예시
@RestController
@RequestMapping("/api/stores")
public class StoreController {
    
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long storeId) {
        // ...
    }
}

// ❌ 나쁜 예시  
@RestController
public class storecontroller {
    
    @GetMapping("/getStore/{id}")
    public ResponseEntity<StoreDto> getstore(@PathVariable Long id) {
        // ...
    }
}
```

### Git 워크플로우

#### 브랜치 전략
```
main
├── develop
│   ├── feature/auth-jwt-implementation
│   ├── feature/store-crud-api  
│   ├── feature/shorts-ai-integration
│   └── hotfix/auth-token-validation
└── release
    └── release/v1.0.0
```

#### 커밋 메시지 컨벤션 (Conventional Commits)
```bash
# 형식: type(scope): description

# 기능 추가
feat(auth): implement JWT token refresh mechanism

# 버그 수정  
fix(store): resolve null pointer exception in store search

# 문서 업데이트
docs(readme): update API endpoint documentation

# 리팩토링
refactor(common): extract common exception handling logic

# 테스트 추가
test(auth): add unit tests for login service

# 빌드/배포 관련
ci(github): add parallel deployment for changed services
```

#### Pull Request 가이드라인

<details>
<summary>📋 <strong>PR 템플릿</strong></summary>

```markdown
## 🎯 변경 사항
- [ ] 새로운 기능 추가
- [ ] 버그 수정  
- [ ] 문서 업데이트
- [ ] 리팩토링
- [ ] 테스트 추가

## 📋 상세 내용
### 변경된 내용
- API 엔드포인트 추가: `POST /api/stores`
- 매장 생성 시 유효성 검사 로직 구현
- 위치 기반 검색 기능 추가

### 테스트 완료 사항
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과  
- [ ] API 문서 업데이트
- [ ] 로컬 환경 테스트 완료

## 🔗 관련 이슈
Closes #123

## 📷 스크린샷 (UI 변경 시)
![image](screenshot-url)

## 🚀 배포 노트
- 데이터베이스 마이그레이션 필요: `V001__create_stores_table.sql`
- 환경변수 추가: `MAPS_API_KEY`

## 📝 리뷰어에게
- 특별히 검토가 필요한 부분: 위치 검색 알고리즘의 성능
- 테스트 데이터: `test-data.sql` 참조
```

</details>

---

## 🔧 트러블슈팅

### 자주 발생하는 문제들

<details>
<summary>🔥 <strong>포트 충돌 오류</strong></summary>

**문제**: `Address already in use: bind`

**해결**:
```bash
# 1. 포트 사용 프로세스 확인
lsof -i :8080
netstat -tlnp | grep :8080

# 2. 프로세스 종료
kill -9 <PID>

# 3. Docker 컨테이너 정리
docker-compose down
docker system prune -f
```

</details>

<details>
<summary>🗄️ <strong>데이터베이스 연결 실패</strong></summary>

**문제**: `Connection refused: connect`

**해결**:
```bash
# 1. MySQL 컨테이너 상태 확인
docker-compose ps mysql

# 2. 로그 확인
docker-compose logs mysql

# 3. 데이터베이스 재시작
docker-compose restart mysql

# 4. 연결 테스트
mysql -h localhost -P 3306 -u marketing_user -p
```

</details>

<details>
<summary>🔑 <strong>JWT 토큰 관련 오류</strong></summary>

**문제**: `Invalid JWT token` 또는 `Token expired`

**해결**:
```bash
# 1. Redis 캐시 확인
docker-compose exec redis redis-cli
> keys *
> get refresh_token:user_123

# 2. 토큰 재발급
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "..."}'

# 3. 로그아웃 후 재로그인
curl -X POST http://localhost:8080/api/auth/logout
```

</details>

<details>
<summary>🐳 <strong>Docker 메모리/디스크 부족</strong></summary>

**문제**: `No space left on device`

**해결**:
```bash
# 1. 사용하지 않는 컨테이너 정리
docker container prune -f

# 2. 사용하지 않는 이미지 정리  
docker image prune -a -f

# 3. 볼륨 정리
docker volume prune -f

# 4. 전체 시스템 정리 (주의!)
docker system prune -a -f --volumes

# 5. 디스크 사용량 확인
docker system df
df -h
```

</details>

<details>
<summary>⚡ <strong>서비스 간 통신 오류</strong></summary>

**문제**: `Connection refused` between services

**해결**:
```bash
# 1. 네트워크 상태 확인
docker network ls
docker network inspect backend_default

# 2. 서비스 간 연결 테스트
docker-compose exec gateway ping auth-service
docker-compose exec gateway curl http://auth-service:8081/actuator/health

# 3. DNS 확인
docker-compose exec gateway nslookup auth-service

# 4. 포트 바인딩 확인
docker-compose ps
```

</details>

### 로그 분석

#### 구조화된 로그 확인
```bash
# 전체 서비스 로그 (최신 100줄)
docker-compose logs -f --tail=100

# 특정 서비스 로그
docker-compose logs -f gateway auth-service

# 에러 로그만 필터링
docker-compose logs | grep ERROR

# 특정 시간대 로그
docker-compose logs --since="2024-01-15T10:00:00"
```

#### 로그 레벨별 분석
- **ERROR**: 즉시 해결 필요한 심각한 오류
- **WARN**: 주의가 필요한 상황, 모니터링 필요
- **INFO**: 정상적인 비즈니스 플로우 로그
- **DEBUG**: 개발/디버깅용 상세 로그 (운영 환경에서는 비활성화)

<br>

### 비상 대응

#### 서비스 긴급 재시작
```bash
# 1. 특정 서비스만 재시작
docker-compose restart gateway auth-service

# 2. 전체 시스템 재시작 (데이터는 보존)
docker-compose restart

# 3. 완전히 재배포 (주의: 데이터 손실 가능)
docker-compose down
docker-compose pull
docker-compose up -d
```

#### 롤백 절차
```bash
# 1. 이전 Docker 이미지로 롤백
docker-compose stop gateway
docker run -d --name gateway_backup username/aivle-gateway:previous-tag
docker-compose start gateway

# 2. Git 코드 롤백
git log --oneline -10
git reset --hard <commit-hash>
git push --force-with-lease origin release
```

---

<div align="center">

## 🤝 기여하기

버그 발견, 기능 제안, 또는 개선사항이 있다면 언제든지 이슈를 등록해주세요!

[🐛 버그 리포트](https://github.com/KT-AILVE-04/backend/issues/new?template=bug_report.md) •
[💡 기능 제안](https://github.com/KT-AILVE-04/backend/issues/new?template=feature_request.md) •
[📚 문서 개선](https://github.com/KT-AILVE-04/backend/issues/new?template=docs_improvement.md)

---

**🏗️ Built with Spring Boot and Microservice Architecture**

**© 2025 KT AIVLE School 4기 - Marketing Platform Team**

</div>