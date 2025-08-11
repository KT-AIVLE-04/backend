package kt.aivle.content.dto;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 DTO
 *
 * 모든 API 응답의 일관성을 위한 공통 응답 형식
 */
public class ApiResponse<T> {

    private String status;              // success, error, warning
    private String message;             // 응답 메시지
    private T data;                     // 실제 데이터
    private String errorCode;           // 에러 코드 (에러 시)
    private LocalDateTime timestamp;    // 응답 시간

    // 기본 생성자
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // 생성자
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답 생성 메소드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    // 에러 응답 생성 메소드
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>("error", message, null);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>("error", message, null);
        response.setErrorCode(errorCode);
        return response;
    }

    // 경고 응답 생성 메소드
    public static <T> ApiResponse<T> warning(String message, T data) {
        return new ApiResponse<>("warning", message, data);
    }

    // 응답 성공 여부 확인
    public boolean isSuccess() {
        return "success".equals(status);
    }

    // 에러 여부 확인
    public boolean isError() {
        return "error".equals(status);
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}