package kt.aivle.analytics.adapter.out.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialPostRequestEvent {
    
    private String requestId;
    private String userId;
    private String snsType;
    private String startDate; // YYYY-MM-DD 형식
    private String endDate;   // YYYY-MM-DD 형식
}
