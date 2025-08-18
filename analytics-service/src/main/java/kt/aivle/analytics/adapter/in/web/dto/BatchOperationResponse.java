package kt.aivle.analytics.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResponse {
    private String operationName;
    private String status;
    private LocalDateTime executedAt;
    private String message;
    private Long processedCount;
    private Long failedCount;
}
