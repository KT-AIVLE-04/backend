package kt.aivle.shorts.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.shorts.adapter.in.web.dto.request.CreateScenarioRequest;
import kt.aivle.shorts.adapter.in.web.dto.request.CreateShortsRequest;
import kt.aivle.shorts.adapter.in.web.dto.request.SaveShortsRequest;
import kt.aivle.shorts.adapter.in.web.dto.response.CreateShortsAccepted;
import kt.aivle.shorts.adapter.in.web.dto.response.JobStatusResponse;
import kt.aivle.shorts.adapter.in.web.dto.response.ScenarioResponse;
import kt.aivle.shorts.adapter.in.web.mapper.toCommandMapper;
import kt.aivle.shorts.adapter.in.web.mapper.toResponseMapper;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import kt.aivle.shorts.application.port.out.ai.shorts.model.JobStatus;
import kt.aivle.shorts.application.port.out.job.JobStore;
import kt.aivle.shorts.application.port.out.s3.MediaStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static kt.aivle.common.code.CommonResponseCode.BAD_REQUEST;
import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsUseCase shortsUseCase;
    private final JobStore jobStore;
    private final MediaStoragePort mediaStoragePort;
    private final ResponseUtils responseUtils;
    private final toCommandMapper toCommandMapper;
    private final toResponseMapper toResponseMapper;

    @PostMapping("/scenario")
    public Mono<ResponseEntity<ApiResponse<ScenarioResponse>>> createScenario(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestBody @Valid CreateScenarioRequest request
    ) {
        CreateScenarioCommand command = toCommandMapper.toCreateScenarioCommand(userId, storeId, request);
        return shortsUseCase.createScenario(command)
                .map(toResponseMapper::toScenarioResponse)
                .map(response -> responseUtils.build(OK, response));
    }

//    @PostMapping(consumes = "multipart/form-data")
//    public Mono<ResponseEntity<ApiResponse<ShortsResponse>>> createShorts(
//            @RequestPart("request") @Valid CreateShortsRequest request,
//            @RequestPart("images") Flux<FilePart> images
//    ) {
//        if (images == null) {
//            return Mono.error(new BusinessException(BAD_REQUEST, "이미지를 업로드해주세요."));
//        }
//        CreateShortsCommand command = toCommandMapper.toCreateShortsCommand(request, images);
//        return shortsUseCase.createShorts(command)
//                .map(toResponseMapper::toShortsResponse)
//                .map(response -> responseUtils.build(OK, response));
//    }

    @PostMapping(consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<CreateShortsAccepted>>> createShorts(
            @RequestPart("request") @Valid CreateShortsRequest request,
            @RequestPart("images") Flux<FilePart> images
    ) {
        if (images == null) {
            return Mono.error(new BusinessException(BAD_REQUEST, "이미지를 업로드해주세요."));
        }

        CreateShortsCommand command = toCommandMapper.toCreateShortsCommand(request, images);

        shortsUseCase.startCreateShorts(command);

        String jobId = request.sessionId();
        URI location = URI.create("/api/shorts/jobs/" + jobId);
        CreateShortsAccepted body = new CreateShortsAccepted(jobId, "QUEUED", location.toString());
        return Mono.just(responseUtils.buildAccepted(location, body));
    }

    @GetMapping("/jobs/{jobId}")
    public Mono<ResponseEntity<ApiResponse<JobStatusResponse>>> getJob(@PathVariable String jobId) {
        return Mono.justOrEmpty(jobStore.find(jobId))
                .flatMap(st -> {
                    if (st.getStatus() == JobStatus.SUCCEEDED && st.getResultKey() != null) {
                        return mediaStoragePort.getPresignedUrl(st.getResultKey())
                                .map(url -> responseUtils.build(OK, new JobStatusResponse(
                                        st.getJobId(), st.getStatus().name(), st.getProgress(), url, null
                                )));
                    }
                    return Mono.just(responseUtils.build(OK, new JobStatusResponse(
                            st.getJobId(), st.getStatus().name(), st.getProgress(), null, st.getError()
                    )));
                })
                .switchIfEmpty(Mono.error(new BusinessException(BAD_REQUEST, "존재하지 않는 작업입니다.")));
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<ApiResponse<Void>>> saveShorts(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestBody @Valid SaveShortsRequest request
    ) {
        SaveShortsCommand command = toCommandMapper.toSaveShortsCommand(userId, storeId, request);
        return shortsUseCase.saveShorts(command).then(Mono.just(responseUtils.build(OK, null)));
    }
}