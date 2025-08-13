package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsAnalyticsDelegator;
import kt.aivle.sns.adapter.in.web.dto.SnsAnalyticsRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.adapter.in.web.dto.YoutubeAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/analytics")
@RequiredArgsConstructor
public class SnsAnalyticsController {

    private final SnsAnalyticsDelegator snsAnalyticsDelegator;

    @GetMapping("/{snsType}")
    public ResponseEntity<YoutubeAnalyticsResponse> getAnalytics(@PathVariable SnsType snsType, @RequestHeader("X-USER-ID") Long userId, @RequestBody SnsAnalyticsRequest request) {
        YoutubeAnalyticsResponse response = snsAnalyticsDelegator.getAnalytics(snsType, userId, request);
        return ResponseEntity.ok(response);
    }
}
