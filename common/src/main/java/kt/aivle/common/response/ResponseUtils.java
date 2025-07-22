package kt.aivle.common.response;

import kt.aivle.common.code.DefaultCode;
import kt.aivle.common.exception.FieldError;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponseUtils {
    public <T> ResponseEntity<ApiResponse<T>> build(DefaultCode code, T result) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.of(code, result));
    }

    public ResponseEntity<ApiResponse<Void>> build(DefaultCode code, List<FieldError> errors) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.of(code, errors));
    }

    public <T> ResponseEntity<ApiResponse<T>> build(DefaultCode code, T result, List<FieldError> errors) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.of(code, result, errors));
    }

    public ResponseEntity<ApiResponse<Void>> build(DefaultCode code) {
        return build(code, (Void) null);
    }
}


