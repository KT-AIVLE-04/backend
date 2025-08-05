package kt.aivle.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatStatsDto {
    private Map<String, Long> formatCounts; // 포맷별 개수
    private String mostUsedFormat; // 가장 많이 사용된 포맷
}