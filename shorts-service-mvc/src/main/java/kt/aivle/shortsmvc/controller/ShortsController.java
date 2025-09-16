package kt.aivle.shortsmvc.controller;

import jakarta.validation.Valid;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.shortsmvc.dto.CreateShortsRequest;
import kt.aivle.shortsmvc.dto.CreateShortsAccepted;
import kt.aivle.shortsmvc.dto.JobStatusResponse;
import kt.aivle.shortsmvc.service.ShortsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/shorts-mvc")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsService shortsService;
    private final ResponseUtils responseUtils;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CreateShortsAccepted>> createShorts(
            @RequestPart("request") @Valid CreateShortsRequest request,
            @RequestPart("images") MultipartFile images
    ) {
        String jobId = shortsService.startCreateShorts(request, List.of(images));
        
        URI location = URI.create("/api/shorts-mvc/jobs/" + jobId);
        CreateShortsAccepted body = new CreateShortsAccepted(jobId, "QUEUED", location.toString());
        
        return responseUtils.buildAccepted(location, body);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJob(@PathVariable String jobId) {
        JobStatusResponse response = shortsService.getJobStatus(jobId);
        return responseUtils.build(OK, response);
    }
}