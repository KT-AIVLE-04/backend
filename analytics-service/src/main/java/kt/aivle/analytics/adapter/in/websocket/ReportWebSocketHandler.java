package kt.aivle.analytics.adapter.in.websocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import kt.aivle.analytics.adapter.in.websocket.dto.MessageType;
import kt.aivle.analytics.adapter.in.websocket.dto.ReportRequestMessage;
import kt.aivle.analytics.adapter.in.websocket.dto.WebSocketResponseMessage;
import kt.aivle.analytics.application.port.in.AnalyticsQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportWebSocketHandler extends TextWebSocketHandler {

    private final AnalyticsQueryUseCase analyticsQueryUseCase;
    private final ObjectMapper objectMapper;
    
    private final AtomicLong taskCounter = new AtomicLong(0);
    private final ConcurrentHashMap<String, CompletableFuture<Void>> activeTasks = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[WebSocket] 연결 수립: {}", session.getId());
        
        // 연결 성공 메시지 전송
        sendMessage(session, WebSocketResponseMessage.<Void>builder()
            .type(MessageType.PROGRESS)
            .percentage(0)
            .message("WebSocket 연결이 성공했습니다!")
            .timestamp(System.currentTimeMillis())
            .build());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("[WebSocket] 메시지 수신: {}", payload);

        try {
            ReportRequestMessage request = objectMapper.readValue(payload, ReportRequestMessage.class);
            
            if ("generate_report".equals(request.getAction())) {
                processReportAsync(session, request);
            } else {
                sendMessage(session, WebSocketResponseMessage.error("지원하지 않는 액션: " + request.getAction()));
            }
            
        } catch (Exception e) {
            log.error("[WebSocket] 메시지 처리 오류: {}", e.getMessage(), e);
            sendMessage(session, WebSocketResponseMessage.error("메시지 처리 오류: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[WebSocket] 연결 종료: {} - {}", session.getId(), status);
        
        // 활성 작업 정리
        activeTasks.values().forEach(task -> task.cancel(true));
        activeTasks.clear();
    }

    private void processReportAsync(WebSocketSession session, ReportRequestMessage request) {
        String taskId = "task-" + taskCounter.incrementAndGet();
        
        // 진행률 전송 시작
        sendMessage(session, WebSocketResponseMessage.progress(0, "AI 분석 보고서 생성을 시작합니다..."));
        
        // 통합된 비동기 메서드 호출 (캐시 확인 포함)
        analyticsQueryUseCase.generateReportAsync(
            request.getUserId(), 
            request.getAccountId(), 
            request.getPostId(), 
            request.getStoreId()
        )
        .thenAccept(wsMessage -> {
            // WebSocket 메시지를 직접 전송
            sendMessage(session, wsMessage);
        })
        .exceptionally(throwable -> {
            log.error("[WebSocket] AI 분석 처리 오류: {}", throwable.getMessage(), throwable);
            sendMessage(session, WebSocketResponseMessage.error("AI 분석 처리 오류: " + throwable.getMessage()));
            return null;
        })
        .whenComplete((result, throwable) -> {
            activeTasks.remove(taskId);
            if (throwable != null) {
                log.error("[WebSocket] 작업 완료 오류: {}", throwable.getMessage());
            }
        });
        
        // 작업 추적
        activeTasks.put(taskId, CompletableFuture.completedFuture(null));
    }

    private void sendMessage(WebSocketSession session, WebSocketResponseMessage<?> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.debug("[WebSocket] 메시지 전송: {}", jsonMessage);
        } catch (IOException e) {
            log.error("[WebSocket] 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }
}
