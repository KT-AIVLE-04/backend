package kt.aivle.analytics.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long postId;
    private String markdownReport;  // AI가 생성한 마크다운 보고서
    private String title;           // 게시물 제목
    private String description;     // 게시물 설명
    private String url;             // 게시물 URL
    private String publishAt;       // 게시일
}
