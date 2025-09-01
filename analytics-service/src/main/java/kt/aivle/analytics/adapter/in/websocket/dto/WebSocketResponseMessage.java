package kt.aivle.analytics.adapter.in.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketResponseMessage<T> {
    private MessageType type;           // PROGRESS, COMPLETE, ERROR
    private T result;                   // 제네릭 결과 타입 (null 가능)
    private int percentage;             // 0-100 진행률
    private String message;             // 상태 메시지
    private Long timestamp;             // 타임스탬프
    
    // 타입별 정적 팩토리 메서드들
    public static <T> WebSocketResponseMessage<T> progress(int percentage, String message) {
        return WebSocketResponseMessage.<T>builder()
            .type(MessageType.PROGRESS)
            .percentage(percentage)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> WebSocketResponseMessage<T> complete(T result, String message) {
        return WebSocketResponseMessage.<T>builder()
            .type(MessageType.COMPLETE)
            .result(result)
            .percentage(100)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> WebSocketResponseMessage<T> error(String message) {
        return WebSocketResponseMessage.<T>builder()
            .type(MessageType.ERROR)
            .percentage(0)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
