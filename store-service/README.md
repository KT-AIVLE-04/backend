# Store Service

**매장 정보 관리 서비스**

비즈니스 매장 정보를 관리하는 서비스입니다. 매장의 기본 정보, 위치 정보, 업종 분류를 처리하며, 다른 서비스에서 매장 정보를 요청할 수 있도록 Kafka 기반 이벤트 처리를 제공합니다.

## 📋 개요

- **포트**: 8082
- **주요 기능**: 매장 CRUD, 업종 관리, 위치 기반 서비스, 이벤트 기반 통신
- **프레임워크**: Spring Boot, Spring Data JPA, Apache Kafka

## 🔧 주요 기능

### 1. 매장 관리
- **매장 등록**: 기본 정보, 위치, 업종 정보 등록
- **매장 조회**: 단일/목록 조회, 사용자별 매장 조회
- **매장 수정**: 매장 정보 업데이트
- **매장 삭제**: 매장 정보 삭제

### 2. 업종 분류 시스템
- **5가지 업종 지원**: 음식점, 카페, 패션, 뷰티, 기술
- **한국어 현지화**: 업종명의 한국어 표시
- **업종별 필터링**: 업종별 매장 검색 및 분류

### 3. 위치 기반 서비스
- **GPS 좌표 저장**: 위도, 경도 정보 관리
- **주소 정보**: 매장 주소 및 상세 주소
- **지역 기반 검색**: 위치 기반 매장 조회 (향후 확장)

### 4. 이벤트 기반 통신
- **Kafka 통합**: 다른 서비스와의 비동기 통신
- **매장 정보 요청 처리**: 다른 서비스의 매장 정보 요청에 응답
- **이벤트 발행**: 매장 생성/수정/삭제 이벤트 발행

## 🏗️ 아키텍처

### 헥사고날 아키텍처 구조
```
├── domain/                    # 도메인 계층
│   └── model/                 # 도메인 모델
│       ├── Store.java        # 매장 엔티티
│       ├── Industry.java     # 업종 enum
│       └── BaseEntity.java   # 공통 엔티티
├── application/               # 애플리케이션 계층
│   ├── port/in/              # 인바운드 포트
│   │   ├── StoreUseCase.java
│   │   ├── StoreEventUseCase.java
│   │   ├── command/          # 커맨드 객체
│   │   └── query/            # 쿼리 객체
│   ├── port/out/             # 아웃바운드 포트
│   │   └── StoreRepositoryPort.java
│   └── service/              # 애플리케이션 서비스
│       ├── StoreService.java
│       └── StoreEventService.java
└── adapter/                   # 어댑터 계층
    ├── in/                   # 인바운드 어댑터
    │   ├── web/              # 웹 어댑터
    │   │   ├── StoreController.java
    │   │   ├── dto/          # DTO
    │   │   └── mapper/       # 매퍼
    │   └── event/            # 이벤트 어댑터
    │       ├── StoreInfoRequestHandler.java
    │       └── KafkaConfig.java
    └── out/                  # 아웃바운드 어댑터
        └── persistence/      # 데이터베이스 어댑터
            ├── StorePersistenceAdapter.java
            ├── JpaStoreRepository.java
            └── IndustryConverter.java
```

### 주요 컴포넌트

#### StoreService
```java
@Service
@Transactional
public class StoreService implements StoreUseCase {
    // 매장 CRUD 비즈니스 로직
    // 업종별 매장 분류 및 검색
}
```

#### StoreEventService
```java
@Service
public class StoreEventService implements StoreEventUseCase {
    // Kafka 이벤트 처리
    // 다른 서비스로부터의 매장 정보 요청 응답
}
```

#### Industry Enum
```java
public enum Industry {
    RESTAURANT("음식점"),
    CAFE("카페"),
    FASHION("패션"),
    BEAUTY("뷰티"),
    TECH("기술");
    
    private final String displayName;
}
```

## 🗄️ 데이터베이스 스키마

### Store 테이블
```sql
CREATE TABLE stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    business_number VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    industry VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_industry (industry),
    INDEX idx_location (latitude, longitude)
);
```

## ⚙️ API 엔드포인트

### 매장 관리 API

#### 매장 등록
```http
POST /api/stores
Authorization: Bearer {token}
Content-Type: application/json

{
    "name": "맛있는 한식당",
    "address": "서울시 강남구 테헤란로 123",
    "phoneNumber": "02-1234-5678",
    "businessNumber": "123-45-67890",
    "latitude": 37.5665,
    "longitude": 126.9780,
    "industry": "RESTAURANT"
}
```

#### 매장 목록 조회
```http
GET /api/stores?page=0&size=10&industry=RESTAURANT
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
                "name": "맛있는 한식당",
                "address": "서울시 강남구 테헤란로 123",
                "phoneNumber": "02-1234-5678",
                "businessNumber": "123-45-67890",
                "latitude": 37.5665,
                "longitude": 126.9780,
                "industry": "RESTAURANT",
                "industryDisplayName": "음식점",
                "createdAt": "2024-01-01T10:00:00",
                "updatedAt": "2024-01-01T10:00:00"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "size": 10,
        "number": 0
    }
}
```

#### 매장 상세 조회
```http
GET /api/stores/{storeId}
Authorization: Bearer {token}
```

#### 매장 수정
```http
PUT /api/stores/{storeId}
Authorization: Bearer {token}
Content-Type: application/json

{
    "name": "더 맛있는 한식당",
    "address": "서울시 강남구 테헤란로 456",
    "phoneNumber": "02-9876-5432",
    "businessNumber": "123-45-67890",
    "latitude": 37.5670,
    "longitude": 126.9785,
    "industry": "RESTAURANT"
}
```

#### 매장 삭제
```http
DELETE /api/stores/{storeId}
Authorization: Bearer {token}
```

### 업종 관리 API

#### 업종 목록 조회
```http
GET /api/stores/industries
```

**응답:**
```json
{
    "code": "SUCCESS",
    "message": "조회 성공",
    "data": [
        {
            "code": "RESTAURANT",
            "displayName": "음식점"
        },
        {
            "code": "CAFE",
            "displayName": "카페"
        },
        {
            "code": "FASHION",
            "displayName": "패션"
        },
        {
            "code": "BEAUTY",
            "displayName": "뷰티"
        },
        {
            "code": "TECH",
            "displayName": "기술"
        }
    ]
}
```

## 🎯 이벤트 시스템

### Kafka 토픽 구성
- **store.request**: 다른 서비스의 매장 정보 요청
- **store.reply**: 매장 정보 응답
- **store.created**: 매장 생성 이벤트
- **store.updated**: 매장 수정 이벤트
- **store.deleted**: 매장 삭제 이벤트

### 이벤트 처리 예시

#### 매장 정보 요청 처리
```java
@KafkaListener(topics = "store.request")
public void handleStoreInfoRequest(StoreInfoRequestMessage message) {
    // 매장 정보 조회
    Store store = storeService.findById(message.getStoreId());
    
    // 응답 이벤트 발행
    StoreInfoResponseMessage response = StoreInfoResponseMessage.builder()
        .requestId(message.getRequestId())
        .storeId(store.getId())
        .storeName(store.getName())
        .industry(store.getIndustry())
        .build();
    
    kafkaTemplate.send("store.reply", response);
}
```

## ⚙️ 설정

### 데이터베이스 설정
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/marketing?useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### Kafka 설정
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    consumer:
      group-id: store-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "kt.aivle.store"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## 🚀 로컬 개발 환경 설정

### Prerequisites
- Java 17+
- MySQL 8.0+
- Apache Kafka 7.4.3+

### 실행 방법
```bash
# Gradle을 통한 실행
./gradlew :store-service:bootRun

# JAR 파일 실행
java -jar store-service/build/libs/store-service-0.0.1-SNAPSHOT.jar

# Docker를 통한 실행
docker build -t marketing-store-service .
docker run -p 8082:8082 marketing-store-service
```

### 환경 변수 설정
```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=marketing
export DB_USERNAME=marketing_user
export DB_PASSWORD=password

# Kafka
export KAFKA_SERVERS=localhost:9092
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :store-service:test
```

### Integration Tests
```bash
./gradlew :store-service:integrationTest
```

### API 테스트 예시
```bash
# 매장 등록 테스트
curl -X POST http://localhost:8082/api/stores \
  -H "Authorization: Bearer {your-jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 매장",
    "address": "서울시 강남구 테헤란로 123",
    "phoneNumber": "02-1234-5678",
    "businessNumber": "123-45-67890",
    "latitude": 37.5665,
    "longitude": 126.9780,
    "industry": "RESTAURANT"
  }'

# 매장 목록 조회 테스트
curl -H "Authorization: Bearer {your-jwt-token}" \
  http://localhost:8082/api/stores?page=0&size=10
```

## 📊 모니터링

### Health Check
```bash
curl http://localhost:8082/actuator/health
```

### Metrics
- 매장 등록/수정/삭제 건수
- 업종별 매장 분포
- API 응답 시간
- Kafka 메시지 처리 통계

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. 데이터베이스 연결 실패
- **증상**: 애플리케이션 시작 실패
- **원인**: 데이터베이스 연결 정보 오류
- **해결**: DB 연결 설정 및 권한 확인

#### 2. Kafka 연결 실패
- **증상**: 이벤트 처리 안됨
- **원인**: Kafka 브로커 연결 불가
- **해결**: Kafka 서버 상태 확인

#### 3. GPS 좌표 유효성 오류
- **증상**: 위치 정보 저장 실패
- **원인**: 잘못된 위도/경도 값
- **해결**: 좌표 범위 검증 로직 확인

#### 4. 업종 변환 오류
- **증상**: 업종 정보 저장/조회 실패
- **원인**: IndustryConverter 오류
- **해결**: Enum 매핑 확인

## 📈 성능 최적화

### 데이터베이스 인덱스 최적화
```sql
-- 사용자별 매장 조회 최적화
CREATE INDEX idx_stores_user_id ON stores(user_id);

-- 업종별 매장 조회 최적화
CREATE INDEX idx_stores_industry ON stores(industry);

-- 위치 기반 검색 최적화 (향후)
CREATE INDEX idx_stores_location ON stores(latitude, longitude);
```

### JPA 쿼리 최적화
```java
// N+1 문제 해결을 위한 페치 조인
@Query("SELECT s FROM Store s WHERE s.userId = :userId")
Page<Store> findByUserIdWithPaging(@Param("userId") Long userId, Pageable pageable);
```

## 🔧 운영 고려사항

### 로깅 설정
```yaml
logging:
  level:
    kt.aivle.store: INFO
    org.springframework.kafka: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 보안 고려사항
- 매장 정보 접근 권한 검증
- 사용자별 매장 데이터 격리
- 민감한 사업자 정보 암호화

### 확장성 고려사항
- 위치 기반 검색 엔진 연동 (ElasticSearch)
- 매장 이미지 관리 기능 추가
- 매장 운영 시간 관리
- 매장 리뷰 및 평점 시스템

---

**서비스 담당**: Store Team  
**최종 업데이트**: 2024년