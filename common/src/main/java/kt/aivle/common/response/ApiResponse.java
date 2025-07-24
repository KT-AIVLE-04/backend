package kt.aivle.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kt.aivle.common.code.DefaultCode;
import kt.aivle.common.exception.FieldError;

import java.util.List;

@JsonPropertyOrder({"isSuccess", "message", "result", "errors"})
public record ApiResponse<T>(
        Boolean isSuccess,
        String message,
        T result,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldError> errors
) {
    public static <T> ApiResponse<T> of(DefaultCode code, T result) {
        return new ApiResponse<>(code.isSuccess(), code.getMessage(), result, null);
    }

    public static <T> ApiResponse<T> of(DefaultCode code, List<FieldError> errors) {
        return new ApiResponse<>(code.isSuccess(), code.getMessage(), null, errors);
    }

    public static <T> ApiResponse<T> of(DefaultCode code, T result, List<FieldError> errors) {
        return new ApiResponse<>(code.isSuccess(), code.getMessage(), result, errors);
    }
}