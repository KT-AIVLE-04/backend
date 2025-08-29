# Common Module

**공통 컴포넌트 및 유틸리티 모듈**

마이크로서비스 전반에서 공통으로 사용되는 컴포넌트, 유틸리티, 예외 처리, API 응답 구조 등을 제공하는 공유 라이브러리 모듈입니다. 모든 서비스에서 일관된 개발 경험과 표준화된 응답 형식을 보장합니다.

## 📋 개요

- **모듈 타입**: 공통 라이브러리 (JAR)
- **주요 기능**: 공통 예외 처리, API 응답 표준화, 유틸리티 함수
- **의존성**: 모든 마이크로서비스에서 공통 의존

## 🔧 주요 컴포넌트

### 1. API 응답 표준화
- **통일된 응답 형식**: 모든 API의 일관된 응답 구조
- **성공/실패 코드 표준화**: 예측 가능한 응답 코드 시스템
- **메시지 현지화**: 다국어 지원을 위한 메시지 체계
- **페이지네이션 지원**: 목록 조회 API의 표준 페이징

### 2. 예외 처리 체계
- **Global Exception Handler**: 전역 예외 처리
- **사용자 정의 예외**: 비즈니스 로직 예외와 인프라 예외 분리
- **필드 검증 오류**: 입력 검증 실패에 대한 상세 오류 정보
- **표준화된 오류 응답**: 클라이언트 친화적 오류 메시지

### 3. 공통 유틸리티
- **응답 빌더**: API 응답 객체 생성 헬퍼
- **코드 정의**: 응답 코드 및 메시지 상수 관리
- **검증 유틸리티**: 공통 검증 로직

## 🏗️ 모듈 구조

```
├── src/main/java/kt/aivle/common/
│   ├── code/                     # 코드 정의
│   │   ├── CommonResponseCode.java    # 공통 응답 코드
│   │   └── DefaultCode.java           # 기본 코드 인터페이스
│   ├── exception/                # 예외 처리
│   │   ├── BusinessException.java     # 비즈니스 예외
│   │   ├── InfraException.java        # 인프라 예외
│   │   ├── FieldError.java            # 필드 검증 오류
│   │   └── GlobalExceptionHandler.java # 전역 예외 핸들러
│   └── response/                 # 응답 관리
│       ├── ApiResponse.java           # API 응답 객체
│       └── ResponseUtils.java         # 응답 유틸리티
└── src/main/resources/
    └── messages/                 # 메시지 리소스 (향후 확장)
        ├── messages.properties   # 기본 메시지
        └── messages_ko.properties # 한국어 메시지
```

## 📄 주요 클래스 상세

### ApiResponse
모든 API의 표준 응답 형식을 정의하는 클래스입니다.

```java
public class ApiResponse<T> {
    private String code;        // 응답 코드
    private String message;     // 응답 메시지  
    private T data;            // 응답 데이터
    private LocalDateTime timestamp; // 응답 시간
    
    // 성공 응답 생성
    public static <T> ApiResponse<T> success(T data) {
        return success(CommonResponseCode.SUCCESS, data);
    }
    
    // 실패 응답 생성
    public static <T> ApiResponse<T> error(DefaultCode code) {
        return error(code, null);
    }
}
```

**응답 형식 예시:**
```json
{
    "code": "SUCCESS",
    "message": "요청이 성공적으로 처리되었습니다",
    "data": {
        "id": 1,
        "name": "사용자명"
    },
    "timestamp": "2024-01-01T10:00:00"
}
```

### CommonResponseCode
시스템에서 사용하는 공통 응답 코드를 정의합니다.

```java
public enum CommonResponseCode implements DefaultCode {
    // 성공 코드
    SUCCESS("SUCCESS", "요청이 성공적으로 처리되었습니다"),
    
    // 클라이언트 오류 (4xx)
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다"),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다"),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다"),
    NOT_FOUND("NOT_FOUND", "요청한 리소스를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "허용되지 않은 HTTP 메서드입니다"),
    VALIDATION_ERROR("VALIDATION_ERROR", "입력값 검증에 실패했습니다"),
    
    // 서버 오류 (5xx)
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "서비스를 사용할 수 없습니다"),
    DATABASE_ERROR("DATABASE_ERROR", "데이터베이스 오류가 발생했습니다"),
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "외부 API 호출에 실패했습니다");
    
    private final String code;
    private final String message;
}
```

### GlobalExceptionHandler
모든 서비스에서 발생하는 예외를 일관되게 처리합니다.

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getCode()));
    }
    
    // 인프라 예외 처리
    @ExceptionHandler(InfraException.class)
    public ResponseEntity<ApiResponse<Object>> handleInfraException(InfraException e) {
        log.error("Infrastructure exception", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(CommonResponseCode.INTERNAL_SERVER_ERROR));
    }
    
    // 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldError>>> handleValidationException(
            MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> FieldError.builder()
                .field(error.getField())
                .value(error.getRejectedValue())
                .message(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());
            
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(CommonResponseCode.VALIDATION_ERROR, fieldErrors));
    }
    
    // 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(CommonResponseCode.INTERNAL_SERVER_ERROR));
    }
}
```

### 사용자 정의 예외 클래스

#### BusinessException
비즈니스 로직 처리 중 발생하는 예외를 표현합니다.

```java
public class BusinessException extends RuntimeException {
    private final DefaultCode code;
    
    public BusinessException(DefaultCode code) {
        super(code.getMessage());
        this.code = code;
    }
    
    public BusinessException(DefaultCode code, String customMessage) {
        super(customMessage);
        this.code = code;
    }
}
```

**사용 예시:**
```java
// 서비스 코드에서 사용
if (user == null) {
    throw new BusinessException(CommonResponseCode.NOT_FOUND, "사용자를 찾을 수 없습니다");
}
```

#### InfraException
인프라 계층에서 발생하는 예외를 표현합니다.

```java
public class InfraException extends RuntimeException {
    private final DefaultCode code;
    
    public InfraException(DefaultCode code, Throwable cause) {
        super(code.getMessage(), cause);
        this.code = code;
    }
}
```

### ResponseUtils
API 응답 생성을 위한 유틸리티 메서드를 제공합니다.

```java
public class ResponseUtils {
    
    // 성공 응답 (데이터 포함)
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    // 성공 응답 (데이터 없음)
    public static ResponseEntity<ApiResponse<Object>> success() {
        return success(null);
    }
    
    // 생성 성공 응답
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(data));
    }
    
    // 오류 응답
    public static ResponseEntity<ApiResponse<Object>> error(DefaultCode code) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(code));
    }
}
```

### FieldError
검증 실패 시 필드별 오류 정보를 담는 클래스입니다.

```java
@Builder
@Getter
public class FieldError {
    private String field;      // 오류가 발생한 필드명
    private Object value;      // 입력된 잘못된 값
    private String message;    // 오류 메시지
}
```

## 💡 사용법

### 1. 의존성 추가
각 서비스의 `build.gradle`에 common 모듈을 추가합니다.

```gradle
dependencies {
    implementation project(':common')
    // 다른 의존성들...
}
```

### 2. 컨트롤러에서 사용

#### 성공 응답
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseUtils.success(user);
    }
    
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        return ResponseUtils.created(user);
    }
}
```

#### 비즈니스 예외 발생
```java
@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(CommonResponseCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));
    }
}
```

### 3. 서비스별 확장 코드 정의

각 서비스는 고유한 응답 코드를 정의할 수 있습니다.

```java
// auth-service의 AuthErrorCode
public enum AuthErrorCode implements DefaultCode {
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "계정이 잠겨 있습니다"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다");
    
    // 구현...
}
```

### 4. 검증 오류 처리

```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다")
    private String password;
}
```

**검증 실패 시 응답 예시:**
```json
{
    "code": "VALIDATION_ERROR",
    "message": "입력값 검증에 실패했습니다",
    "data": [
        {
            "field": "email",
            "value": "invalid-email",
            "message": "올바른 이메일 형식이 아닙니다"
        },
        {
            "field": "password",
            "value": "123",
            "message": "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다"
        }
    ],
    "timestamp": "2024-01-01T10:00:00"
}
```

## 🧪 테스트

### Unit Tests
```bash
./gradlew :common:test
```

### 테스트 예시
```java
@Test
void testApiResponseSuccess() {
    String testData = "test";
    ApiResponse<String> response = ApiResponse.success(testData);
    
    assertThat(response.getCode()).isEqualTo("SUCCESS");
    assertThat(response.getData()).isEqualTo(testData);
    assertThat(response.getTimestamp()).isNotNull();
}

@Test
void testGlobalExceptionHandler() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    BusinessException exception = new BusinessException(CommonResponseCode.NOT_FOUND);
    
    ResponseEntity<ApiResponse<Object>> response = handler.handleBusinessException(exception);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
}
```

## 🔧 확장 가능성

### 1. 국제화 지원
```java
// MessageSource를 활용한 다국어 지원
@Component
public class MessageUtils {
    
    @Autowired
    private MessageSource messageSource;
    
    public String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }
}
```

### 2. 로깅 강화
```java
// 공통 로깅 유틸리티
@Component
public class LogUtils {
    
    public static void logApiCall(String method, String uri, Object request) {
        log.info("API Call: {} {} - Request: {}", method, uri, request);
    }
    
    public static void logApiResponse(String method, String uri, Object response, long duration) {
        log.info("API Response: {} {} - Response: {} ({}ms)", method, uri, response, duration);
    }
}
```

### 3. 보안 강화
```java
// 민감한 정보 마스킹 유틸리티
public class SecurityUtils {
    
    public static String maskEmail(String email) {
        // 이메일 마스킹 로직
    }
    
    public static String maskPhoneNumber(String phone) {
        // 전화번호 마스킹 로직
    }
}
```

### 4. 성능 모니터링
```java
// 공통 성능 메트릭 수집
@Component
public class MetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    public void recordApiCall(String endpoint, long duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.call")
            .tag("endpoint", endpoint)
            .register(meterRegistry));
    }
}
```

## 📚 모범 사례

### 1. 예외 처리 가이드라인
- **BusinessException**: 사용자에게 보여줄 수 있는 비즈니스 로직 오류
- **InfraException**: 시스템 내부 오류, 사용자에게 세부 정보 노출하지 않음
- **적절한 HTTP 상태 코드**: 예외 성격에 맞는 상태 코드 사용

### 2. 응답 메시지 작성 원칙
- **사용자 친화적**: 개발자가 아닌 일반 사용자도 이해할 수 있는 메시지
- **구체적**: 오류 원인과 해결 방법을 명확히 제시
- **일관성**: 동일한 오류에 대해서는 항상 같은 메시지

### 3. 코드 네이밍 규칙
- **응답 코드**: 대문자와 언더스코어 조합 (예: `USER_NOT_FOUND`)
- **명확한 의미**: 코드만 보고도 어떤 상황인지 파악 가능
- **계층적 구조**: 서비스별 접두사 고려 (예: `AUTH_TOKEN_EXPIRED`)

---

**모듈 담당**: Platform Team  
**최종 업데이트**: 2024년