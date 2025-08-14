package kt.aivle.content.controller;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.content.dto.ContentMapper;
import kt.aivle.content.dto.ContentResponse;
import kt.aivle.content.dto.CreateContentRequest;
import kt.aivle.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static kt.aivle.common.code.CommonResponseCode.CREATED;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;
    private final ContentMapper contentMapper;
    private final ResponseUtils responseUtils;


    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        CreateContentRequest request = contentMapper.toCreateContentRequest(userId, storeId, file);
        ContentResponse response = contentService.uploadContent(request);
        return responseUtils.build(CREATED, response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ContentResponse> get(@PathVariable Long id) {
//        return ResponseEntity.ok(service.get(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ContentResponse>> list() {
//        return ResponseEntity.ok(service.list());
//    }
//
//    @PutMapping("/{id}/title")
//    public ResponseEntity<ContentResponse> updateTitle(
//            @PathVariable Long id,
//            @RequestParam String title
//    ) {
//        return ResponseEntity.ok(service.updateTitle(id, title));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}