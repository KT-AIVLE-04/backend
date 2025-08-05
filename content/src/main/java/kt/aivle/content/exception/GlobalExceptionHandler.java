// GlobalExceptionHandler.java
package kt.aivle.content.exception;

import kt.aivle.content.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 콘텐츠를 찾을 수 없는 경우
     */
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleContentNotFoundException(ContentNotFoundException e) {
        log.warn("Content not found: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "CONTENT_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 잘못된 파일 형식이나 크기인 경우
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFileException(InvalidFileException e) {
        log.warn("Invalid file: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "INVALID_FILE");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 파일 처리 중 오류가 발생한 경우
     */
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileProcessingException(FileProcessingException e) {
        log.error("File processing error: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error("파일 처리 중 오류가 발생했습니다.", "FILE_PROCESSING_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 저장소 관련 오류
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleStorageException(StorageException e) {
        log.error("Storage error: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.error("저장소 오류가 발생했습니다.", "STORAGE_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 파일 크기 제한 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("File size exceeded: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error("파일 크기가 제한을 초과했습니다.", "FILE_SIZE_EXCEEDED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 유효성 검사 실패 - @Valid 어노테이션
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("입력 값 검증에 실패했습니다.")
                .data(errors)
                .errorCode("VALIDATION_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 바인딩 오류
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException e) {
        log.warn("Bind error: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("요청 파라미터 바인딩에 실패했습니다.")
                .data(errors)
                .errorCode("BIND_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "ILLEGAL_ARGUMENT");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);

        ApiResponse<Void> response = ApiResponse.error("서버 내부 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}