package kt.aivle.sns.adapter.in.web;

import kt.aivle.sns.application.service.SnsPostDelegator;
import kt.aivle.sns.domain.model.PostDeleteRequest;
import kt.aivle.sns.domain.model.PostUpdateRequest;
import kt.aivle.sns.domain.model.SnsType;
import kt.aivle.sns.domain.model.PostUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sns/video")
@RequiredArgsConstructor
public class SnsPostController {

    private final SnsPostDelegator snsPostDelegator;

    //AuthenticatonPrincipal?
    //SecurityContext?
    private final String TEST_USER_ID = "test-user"; // 테스트용 userId

    @PostMapping("/{snsType}/upload")
    public ResponseEntity<?> uploadVideo(@PathVariable SnsType snsType, @RequestBody PostUploadRequest request) {
        snsPostDelegator.upload(snsType, TEST_USER_ID, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{snsType}/update")
    public ResponseEntity<?> updateVideo(@PathVariable SnsType snsType, @RequestBody PostUpdateRequest request) {
        snsPostDelegator.update(snsType, TEST_USER_ID, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{snsType}/delete")
    public ResponseEntity<?> deleteVideo(@PathVariable SnsType snsType, @RequestBody PostDeleteRequest request) {
        snsPostDelegator.delete(snsType, TEST_USER_ID, request);
        return ResponseEntity.ok().build();
    }
}
