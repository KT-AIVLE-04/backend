package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsPostDelegator;
import kt.aivle.sns.adapter.in.web.dto.PostDeleteRequest;
import kt.aivle.sns.adapter.in.web.dto.PostUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.adapter.in.web.dto.PostUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/video")
@RequiredArgsConstructor
public class SnsPostController {

    private final SnsPostDelegator snsPostDelegator;

    @PostMapping("/{snsType}/upload")
    public ResponseEntity<?> uploadVideo(@PathVariable SnsType snsType,
                                         @RequestHeader("X-USER-ID") Long userId,
                                         @RequestBody PostUploadRequest request) {
        snsPostDelegator.upload(snsType, userId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{snsType}/update")
    public ResponseEntity<?> updateVideo(@PathVariable SnsType snsType,
                                         @RequestHeader("X-USER-ID") Long userId,
                                         @RequestBody PostUpdateRequest request) {
        snsPostDelegator.update(snsType, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{snsType}/delete")
    public ResponseEntity<?> deleteVideo(@PathVariable SnsType snsType,
                                         @RequestHeader("X-USER-ID") Long userId,
                                         @RequestBody PostDeleteRequest request) {
        snsPostDelegator.delete(snsType, userId, request);
        return ResponseEntity.ok().build();
    }
}
