# Gateway Service

**API 게이트웨이 및 인증 필터링 서비스**

Spring Cloud Gateway를 기반으로 한 API 게이트웨이 서비스입니다. 모든 클라이언트 요청의 진입점 역할을 하며, JWT 기반 인증, 라우팅, CORS 설정을 담당합니다.

## 📋 개요

- **포트**: 8080
- **주요 기능**: API 라우팅, JWT 인증, CORS 처리, 토큰 블랙리스트 관리
- **프레임워크**: Spring Cloud Gateway, Spring WebFlux

## 🔧 주요 기능

### 1. API 라우팅
모든 마이크로서비스에 대한 중앙 집중식 라우팅을 제공합니다.

| 경로 패턴 | 대상 서비스 | 포트 |
|-----------|-------------|------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/stores/**` | store-service | 8082 |
| `/api/shorts/**` | shorts-service | 8083 |
| `/api/contents/**` | content-service | 8084 |
| `/api/sns/**` | sns-service | 8085 |
| `/api/analytics/**` | analytics-service | 8086 |

### 2. JWT 인증 필터링
- **Access Token 검증**: JWT 서명 및 만료 시간 검증
- **Token Blacklist 확인**: Redis 기반 토큰 블랙리스트 검증
- **사용자 컨텍스트 전파**: 검증된 사용자 정보를 헤더를 통해 다운스트림 서비스에 전달

### 3. CORS 처리
- 프론트엔드 애플리케이션과의 Cross-Origin 요청 처리
- 개발 환경과 프로덕션 환경에 따른 유연한 CORS 정책

### 4. 인증 제외 경로 관리
- 로그인, 회원가입, Health Check 등의 경로는 인증을 건너뜀
- 유연한 패턴 매칭을 통한 경로 관리

## 🏗️ 아키텍처

### 필터 체인 구조
```
Client Request
    ↓
CORS Filter
    ↓
JWT Authentication Filter
    ↓
Route Filter
    ↓
Target Microservice
```

### 주요 컴포넌트

#### JwtAuthenticationFilter
```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    // JWT 토큰 검증 및 사용자 컨텍스트 설정
    // Redis 블랙리스트 확인
    // 인증 실패 시 401 응답 반환
}
```

#### ExcludePaths
```java
@Component
public class ExcludePaths {
    // 인증 제외 경로 패턴 관리
    // AntPathMatcher를 사용한 패턴 매칭
}
```

## ⚙️ 설정

### Application Configuration
```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2
```

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
```

### JWT Configuration
```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiry: 7200000  # 2 hours
```

## 🔒 보안 기능

### JWT 토큰 검증 프로세스
1. **Authorization 헤더 확인**: `Bearer {token}` 형식 검증
2. **JWT 서명 검증**: 공유 비밀키를 사용한 서명 검증
3. **토큰 만료 확인**: exp claim 검증
4. **블랙리스트 확인**: Redis에서 토큰 블랙리스트 상태 확인
5. **사용자 정보 추출**: JWT claim에서 사용자 ID, 이메일 등 추출

### 인증 제외 경로
```java
// 인증이 필요하지 않은 경로들
private static final List<String> EXCLUDE_PATTERNS = Arrays.asList(
    "/api/auth/login",
    "/api/auth/signup",
    "/api/auth/oauth/**",
    "/actuator/health",
    "/swagger-ui/**",
    "/v3/api-docs/**"
);
```

### 사용자 컨텍스트 전파
인증된 사용자 정보를 다음 헤더를 통해 다운스트림 서비스에 전달:
- `X-User-Id`: 사용자 ID
- `X-User-Email`: 사용자 이메일
- `X-User-Name`: 사용자 이름

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- Redis 서버
- 다른 마이크로서비스들이 실행 중이어야 함

### 실행 방법
```bash
# Gradle을 통한 실행
./gradlew :gateway:bootRun

# JAR 파일 실행
java -jar gateway/build/libs/gateway-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-gateway .
docker run -p 8080:8080 marketing-gateway
```

### 환경 변수
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password
export JWT_SECRET=your-jwt-secret-key
```

## 📊 모니터링 & Health Check

### Health Check Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### Metrics Endpoint
```bash
curl http://localhost:8080/actuator/metrics
```

### Gateway Routes 확인
```bash
curl http://localhost:8080/actuator/gateway/routes
```

## 🧪 테스트

### 인증 토큰 테스트
```bash
# 유효한 토큰으로 요청
curl -H "Authorization: Bearer your-jwt-token" \
     http://localhost:8080/api/stores

# 인증 없이 요청 (401 예상)
curl http://localhost:8080/api/stores
```

### CORS 테스트
```bash
# Preflight 요청 테스트
curl -X OPTIONS \
     -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Authorization" \
     http://localhost:8080/api/auth/login
```

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. JWT 토큰 검증 실패
- **증상**: 401 Unauthorized 응답
- **원인**: 
  - 잘못된 JWT Secret
  - 만료된 토큰
  - 블랙리스트에 등록된 토큰
- **해결**: JWT Secret 확인, 토큰 재발급 요청

#### 2. Redis 연결 실패
- **증상**: 503 Service Unavailable
- **원인**: Redis 서버 연결 불가
- **해결**: Redis 서버 상태 확인, 연결 설정 검토

#### 3. 다운스트림 서비스 연결 실패
- **증상**: 502 Bad Gateway
- **원인**: 대상 서비스가 실행되지 않음
- **해결**: 마이크로서비스들의 실행 상태 확인

#### 4. CORS 오류
- **증상**: 브라우저 콘솔에 CORS 에러
- **원인**: 허용되지 않은 Origin에서 요청
- **해결**: CORS 설정에서 허용 Origin 추가

## 📈 성능 최적화

### 연결 풀 설정
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

### 게이트웨이 성능 튜닝
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
        pool:
          type: elastic
          max-idle-time: 15s
```

## 🔧 운영 고려사항

### 로깅
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    kt.aivle.gateway: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### 메트릭 수집
- Spring Boot Actuator를 통한 메트릭 노출
- 응답 시간, 에러율, 처리량 모니터링
- 각 라우트별 성능 추적

---

**서비스 담당**: Gateway Team  
**최종 업데이트**: 2024년