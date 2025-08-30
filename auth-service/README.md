# Auth Service

**사용자 인증 및 인가 서비스**

Spring Security와 JWT를 기반으로 한 인증/인가 서비스입니다. 로컬 계정 인증과 OAuth2 소셜 로그인을 지원하며, JWT 토큰 기반의 상태 없는(stateless) 인증을 제공합니다.

## 📋 개요

- **포트**: 8081
- **주요 기능**: 회원가입, 로그인, JWT 토큰 관리, OAuth2 소셜 로그인, 계정 보안
- **프레임워크**: Spring Boot, Spring Security, Spring Data JPA

## 🔧 주요 기능

### 1. 사용자 인증
- **로컬 인증**: 이메일/비밀번호 기반 로그인
- **OAuth2 소셜 로그인**: Google, Kakao 지원
- **JWT 토큰 발급**: Access Token (2시간) + Refresh Token (14일)
- **토큰 관리**: Redis 기반 Refresh Token 저장, 토큰 블랙리스트

### 2. 계정 보안
- **비밀번호 정책**: 최소 8자, 영문/숫자/특수문자 조합
- **로그인 실패 추적**: 5회 실패 시 계정 잠금 (30분)
- **계정 잠금 해제**: 시간 기반 자동 해제
- **비밀번호 암호화**: BCrypt 해싱

### 3. OAuth2 통합
- **Google OAuth2**: Google 계정 연동
- **Kakao OAuth2**: 카카오 계정 연동
- **프로바이더 관리**: 다중 OAuth 프로바이더 지원
- **계정 연결**: 기존 계정과 소셜 계정 연결

## 🏗️ 아키텍처

### 헥사고날 아키텍처 구조
```
├── domain/                    # 도메인 계층
│   ├── model/                 # 도메인 모델
│   │   ├── User.java         # 사용자 엔티티
│   │   ├── OAuthUser.java    # OAuth 사용자 정보
│   │   ├── OAuthToken.java   # OAuth 토큰
│   │   └── OAuthProvider.java # OAuth 프로바이더 enum
│   └── service/               # 도메인 서비스
│       └── UserPasswordPolicy.java # 비밀번호 정책
├── application/               # 애플리케이션 계층
│   ├── port/in/              # 인바운드 포트
│   │   ├── AuthUseCase.java
│   │   ├── OAuthUseCase.java
│   │   └── command/          # 커맨드 객체
│   ├── port/out/             # 아웃바운드 포트
│   │   ├── UserRepositoryPort.java
│   │   ├── RefreshTokenRepositoryPort.java
│   │   ├── TokenBlacklistRepositoryPort.java
│   │   └── OAuthPort.java
│   └── service/              # 애플리케이션 서비스
│       ├── AuthService.java
│       ├── OAuthService.java
│       └── UserLoginFailService.java
└── adapter/                   # 어댑터 계층
    ├── in/web/               # 웹 어댑터
    │   ├── AuthController.java
    │   └── dto/              # DTO
    └── out/                  # 아웃바운드 어댑터
        ├── persistence/      # 데이터베이스 어댑터
        ├── oauth/            # OAuth 어댑터
        ├── jwt/              # JWT 유틸리티
        └── redis/            # Redis 어댑터
```

### 주요 컴포넌트

#### AuthService
```java
@Service
@Transactional
public class AuthService implements AuthUseCase {
    // 로그인, 로그아웃, 토큰 관리
    // 비밀번호 검증 및 계정 잠금 관리
}
```

#### OAuthService
```java
@Service
public class OAuthService implements OAuthUseCase {
    // OAuth2 로그인 처리
    // 소셜 계정과 로컬 계정 연결
}
```

#### JwtUtils
```java
@Component
public class JwtUtils {
    // JWT 토큰 생성 및 검증
    // 토큰에서 사용자 정보 추출
}
```

## 🗄️ 데이터베이스 스키마

### User 테이블
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    login_fail_count INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Redis 데이터 구조
```
# Refresh Token
refresh_token:{userId} -> {refreshToken}
TTL: 14 days

# Token Blacklist
token_blacklist:{jti} -> "blacklisted"
TTL: token expiry time

# Login Fail Count
login_fail:{email} -> {failCount}
TTL: 30 minutes
```

## ⚙️ API 엔드포인트

### 인증 관련 API

#### 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
    "email": "user@example.com",
    "name": "홍길동",
    "password": "password123!"
}
```

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123!"
}
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "로그인 성공",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 7200
    }
}
```

#### 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 로그아웃
```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

### OAuth2 API

#### Google OAuth2 로그인 시작
```http
GET /api/auth/oauth/google/login
```

#### Google OAuth2 콜백
```http
GET /api/auth/oauth/google/callback?code={authCode}&state={state}
```

#### Kakao OAuth2 로그인 시작
```http
GET /api/auth/oauth/kakao/login
```

#### Kakao OAuth2 콜백
```http
GET /api/auth/oauth/kakao/callback?code={authCode}&state={state}
```

## 🔒 보안 설정

### JWT 설정
```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiry: 7200000   # 2 hours
  refresh-token-expiry: 1209600000 # 14 days
```

### OAuth2 설정
```yaml
oauth:
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI}
    scope: profile email
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    redirect-uri: ${KAKAO_REDIRECT_URI}
```

### 비밀번호 정책
```java
public class UserPasswordPolicy {
    private static final int MIN_LENGTH = 8;
    private static final String PATTERN = 
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]";
    
    // 비밀번호 강도 검증
    // 특수문자, 숫자, 영문 포함 필수
}
```

### 계정 보안 정책
- **로그인 실패 제한**: 5회 실패 시 30분 계정 잠금
- **토큰 만료**: Access Token 2시간, Refresh Token 14일
- **토큰 블랙리스트**: 로그아웃 시 토큰 즉시 무효화

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- MySQL 8.0+
- Redis 7.2+
- Google/Kakao OAuth2 앱 등록

### 실행 방법
```bash
# Gradle을 통한 실행
./gradlew :auth-service:bootRun

# JAR 파일 실행
java -jar auth-service/build/libs/auth-service-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-auth-service .
docker run -p 8081:8081 marketing-auth-service
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

# JWT
export JWT_SECRET=your-jwt-secret-key-at-least-32-characters

# Google OAuth2
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export GOOGLE_REDIRECT_URI=http://localhost:8081/api/auth/oauth/google/callback

# Kakao OAuth2
export KAKAO_CLIENT_ID=your-kakao-client-id
export KAKAO_REDIRECT_URI=http://localhost:8081/api/auth/oauth/kakao/callback
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :auth-service:test
```

### Integration Tests
```bash
./gradlew :auth-service:integrationTest
```

### API 테스트 예시
```bash
# 회원가입 테스트
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "테스트 사용자",
    "password": "password123!"
  }'

# 로그인 테스트
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123!"
  }'
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

### Metrics
- 로그인 성공/실패율
- 토큰 발급 횟수
- OAuth2 로그인 통계
- 계정 잠금 통계

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. JWT 토큰 생성 실패
- **증상**: 로그인 시 500 에러
- **원인**: JWT_SECRET 환경변수 미설정 또는 길이 부족
- **해결**: 32자 이상의 JWT_SECRET 설정

#### 2. OAuth2 콜백 오류
- **증상**: OAuth2 로그인 후 오류 페이지
- **원인**: 
  - 잘못된 redirect URI
  - OAuth2 앱 설정 오류
- **해결**: OAuth2 앱 설정 및 환경변수 확인

#### 3. 계정 잠금 해제 안됨
- **증상**: 계정 잠금 후 로그인 불가
- **원인**: 시스템 시간 불일치
- **해결**: 서버 시간 동기화 확인

#### 4. Redis 연결 오류
- **증상**: 토큰 저장/조회 실패
- **원인**: Redis 서버 연결 불가
- **해결**: Redis 서버 상태 및 연결 설정 확인

## 📈 성능 최적화

### 데이터베이스 최적화
```sql
-- 이메일 검색 최적화
CREATE INDEX idx_users_email ON users(email);

-- OAuth 사용자 검색 최적화
CREATE INDEX idx_users_provider_id ON users(provider, provider_id);
```

### Redis 연결 풀 최적화
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

## 🔧 운영 고려사항

### 로깅 설정
```yaml
logging:
  level:
    kt.aivle.auth: INFO
    org.springframework.security: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 보안 헤더 설정
```java
@Configuration
public class SecurityConfig {
    // CSRF 보호
    // XSS 보호
    // Content Security Policy
    // Secure 쿠키 설정
}
```

---

**서비스 담당**: Auth Team  
**최종 업데이트**: 2024년