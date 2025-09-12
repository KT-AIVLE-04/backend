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

import kt.aivle.analytics.adapter.in.web.dto.response.ReportResponse;
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
    private final ConcurrentHashMap<String, CompletableFuture<ReportResponse>> activeTasks = new ConcurrentHashMap<>();

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
        
        log.info("[WebSocket] AI 분석 보고서 생성 요청 - postId: {}, accountId: {}, storeId: {}", 
            request.getPostId(), request.getAccountId(), request.getStoreId());
        
        // 중복 요청 방지: 이미 진행 중인 작업이 있는지 확인
        if (!activeTasks.isEmpty()) {
            log.info("[WebSocket] 이미 진행 중인 작업이 있습니다. 요청을 무시합니다. - sessionId: {}", session.getId());
            return; // 조용히 무시
        }
        
        // 진행률 전송 시작
        sendMessage(session, WebSocketResponseMessage.progress(10, "AI 분석 보고서 생성을 시작합니다..."));
        
        // accountId로 userId 조회 후 AI 분석 진행
        // userId는 sns_account 테이블에서 조회하여 사용
        CompletableFuture<ReportResponse> reportFuture = analyticsQueryUseCase.generateReportAsync(
            request.getAccountId(), 
            request.getPostId(), 
            request.getStoreId(),
            (progress, message) -> {
                // 진행률 콜백으로 WebSocket 메시지 전송
                sendMessage(session, WebSocketResponseMessage.progress(progress, message));
            }
        )
        .whenComplete((reportResponse, throwable) -> {
            if (throwable != null) {
                // 에러 발생 시
                log.error("[WebSocket] AI 분석 처리 오류: {}", throwable.getMessage(), throwable);
                sendMessage(session, WebSocketResponseMessage.error("AI 분석 처리 오류: " + throwable.getMessage()));
            } else {
                // 성공 시 최종 완료 메시지 전송
                sendMessage(session, WebSocketResponseMessage.complete(reportResponse, "AI 분석 보고서가 완성되었습니다!"));
            }
            
            // 작업 정리
            activeTasks.remove(taskId);
            
            // 완료 후 연결 유지를 위한 지연 (캐싱된 데이터 처리 시 중요)
            if (isSessionActive(session)) {
                try {
                    Thread.sleep(200); // 200ms 지연으로 연결 안정화
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // 작업 추적 - 실제 CompletableFuture를 저장
        activeTasks.put(taskId, reportFuture);
    }
    
    private void sendMessage(WebSocketSession session, WebSocketResponseMessage<?> message) {
        try {
            // 세션 상태 확인
            if (!isSessionActive(session)) {
                log.warn("[WebSocket] 비활성 세션에 메시지 전송 시도 - sessionId: {}", session.getId());
                return;
            }
            
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.info("[WebSocket] 메시지 전송: {}", jsonMessage);
        } catch (IOException e) {
            log.error("[WebSocket] 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }
    
    private boolean isSessionActive(WebSocketSession session) {
        return session != null && session.isOpen();
    }
}
