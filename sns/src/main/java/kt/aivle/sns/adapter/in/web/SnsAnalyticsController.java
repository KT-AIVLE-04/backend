package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsAnalyticsDelegator;
import kt.aivle.sns.domain.model.SnsAnalyticsRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.domain.model.YoutubeAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/analytics")
@RequiredArgsConstructor
public class SnsAnalyticsController {

    private final SnsAnalyticsDelegator snsAnalyticsDelegator;

    private final String TEST_USER_ID = "test-user";

    @GetMapping("/{snsType}")
    public ResponseEntity<YoutubeAnalyticsResponse> getAnalytics(@PathVariable SnsType snsType, @RequestBody SnsAnalyticsRequest request) {
        YoutubeAnalyticsResponse response = snsAnalyticsDelegator.getAnalytics(snsType, TEST_USER_ID, request);
        return ResponseEntity.ok(response);
    }
}
