package kt.aivle.analytics.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kt.aivle.common.code.CommonResponseCode;
import kt.aivle.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/analytics/health")
@RequiredArgsConstructor
public class HealthController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<HealthStatus>> healthCheck() {
        log.debug("Health check requested");
        
        HealthStatus status = new HealthStatus(
            "analytics-service",
            "UP",
            System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(ApiResponse.of(CommonResponseCode.OK, status));
    }
    
    public static class HealthStatus {
        private final String service;
        private final String status;
        private final long timestamp;
        
        public HealthStatus(String service, String status, long timestamp) {
            this.service = service;
            this.status = status;
            this.timestamp = timestamp;
        }
        
        public String getService() { return service; }
        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
    }
}
