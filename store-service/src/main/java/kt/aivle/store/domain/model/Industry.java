package kt.aivle.store.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import kt.aivle.common.exception.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static kt.aivle.store.exception.StoreErrorCode.NOT_FOUND_INDUSTRY;

@Getter
@RequiredArgsConstructor
public enum Industry {

    RESTAURANT("음식점"),
    CAFE("카페"),
    FASHION("패션"),
    BEAUTY("뷰티"),
    TECH("테크");

    @JsonValue
    private final String value;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Industry from(String v) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(v))
                .findFirst()
                .orElseThrow(() -> new BusinessException(NOT_FOUND_INDUSTRY, "해당 업종을 찾을 수 없습니다. value: " + v));
    }

    @Override
    public String toString() {
        return value;
    }
}