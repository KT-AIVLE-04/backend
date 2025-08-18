package kt.aivle.common.exception;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

import static kt.aivle.common.code.CommonResponseCode.*;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ResponseUtils responseUtils;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("서버 예외 발생: {}", e.getMessage(), e);
        return responseUtils.build(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InfraException.class)
    public ResponseEntity<ApiResponse<Void>> handleInfraException(InfraException e) {
        log.error("인프라 예외 발생: {}", e.toString(), e);
        return responseUtils.build(e.getCode());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("비즈니스 예외 발생: {}", e.toString(), e);
        return responseUtils.build(e.getCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("필드 예외 발생: {}", e.toString(), e);
        List<FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldError(err.getField(), err.getDefaultMessage()))
                .toList();
        return responseUtils.build(BAD_REQUEST, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HTTP 메시지 읽기 예외 발생: {}", e.getMessage(), e);
        return responseUtils.build(BAD_REQUEST_MESSAGE);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleUploadSizeException(MaxUploadSizeExceededException e) {
        log.error("파일 업로드 예외 발생: {} ", e.getMessage(), e);
        return responseUtils.build(BAD_REQUEST_UPLOAD);
    }

}
