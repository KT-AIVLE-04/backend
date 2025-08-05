// ContentStatsDto.java
package kt.aivle.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatsDto {
    private Long totalContents;
    private Long totalVideos;
    private Long totalImages;
    private Long aiGeneratedVideos;
    private Long aiGeneratedImages;
    private Long totalShorts;
    private Long totalFileSize;

    // 월별 통계
    private Integer currentMonthUploads;
    private Integer previousMonthUploads;
    private Double uploadGrowthRate;

    // 포맷별 통계
    private FormatStatsDto videoFormats;
    private FormatStatsDto imageFormats;
}
