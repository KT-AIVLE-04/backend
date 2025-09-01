package kt.aivle.analytics.adapter.out.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReportResponse {
    @JsonProperty("markdown_report")
    private String markdownReport;
}
