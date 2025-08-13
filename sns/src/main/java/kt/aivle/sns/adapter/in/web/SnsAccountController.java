package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsAccountDelegator;
import kt.aivle.sns.adapter.in.web.dto.SnsAccountUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/account")
@RequiredArgsConstructor
public class SnsAccountController {

    private final SnsAccountDelegator snsAccountDelegator;

    @GetMapping("/{snsType}")
    public ResponseEntity<?> getAccountInfo(@PathVariable SnsType snsType, @RequestHeader("X-USER-ID") Long userId) {
        snsAccountDelegator.getAccountInfo(snsType, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{snsType}")
    public ResponseEntity<?> updateAccount(@PathVariable SnsType snsType, @RequestHeader("X-USER-ID") Long userId, @RequestBody SnsAccountUpdateRequest request) {
        snsAccountDelegator.updateAccount(snsType, userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{snsType}/list")
    public ResponseEntity<?> getPostList(@PathVariable SnsType snsType, @RequestHeader("X-USER-ID") Long userId) {
        snsAccountDelegator.getPostList(snsType, userId);
        return ResponseEntity.ok().build();
    }
}
