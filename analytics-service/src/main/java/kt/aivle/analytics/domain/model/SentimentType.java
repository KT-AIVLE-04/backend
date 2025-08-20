package kt.aivle.analytics.domain.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SentimentType {
    
    POSITIVE("POSITIVE"),    // 긍정
    NEGATIVE("NEGATIVE"),    // 부정
    NEUTRAL("NEUTRAL");      // 중립
    
    @JsonValue
    private final String value;
    
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SentimentType from(String v) {
        if (v == null) {
            return NEUTRAL;
        }
        
        return Arrays.stream(values())
                .filter(e -> e.value.equalsIgnoreCase(v))
                .findFirst()
                .orElse(NEUTRAL);  // 예외 대신 기본값 반환
    }
    
    @Override
    public String toString() {
        return value;
    }
}
