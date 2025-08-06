package kt.aivle.shorts.adapter.in.web;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.shorts.adapter.in.web.dto.ScenarioRequest;
import kt.aivle.shorts.adapter.in.web.dto.ScenarioResponse;
import kt.aivle.shorts.adapter.in.web.mapper.ScenarioCommandMapper;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsUseCase shortsUseCase;
    private final ResponseUtils responseUtils;
    private final ScenarioCommandMapper scenarioCommandMapper;

    @PostMapping(value = "/scenario", consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<List<ScenarioResponse>>>> createScenario(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestPart("images") Flux<FilePart> images,
            @RequestPart("request") ScenarioRequest request
    ) {
        CreateScenarioCommand command = scenarioCommandMapper.toCreateCommand(userId, storeId, request, images);
        return shortsUseCase.createScenario(command)
                .map(dto -> responseUtils.build(OK, dto));
    }

//    @PostMapping("/scenario/edit")
//    public Mono<ResponseEntity<ApiResponse<ScenarioResponse>>> regenerateScenario(
//            @RequestBody ScenarioRequest request
//    ) {
//        return shortsService.regenerateScenario(request)
//                .map(dto -> responseUtils.build(SUCCESS, dto));
//    }
//
//    @PostMapping("/scenario/select")
//    public Mono<ResponseEntity<ApiResponse<SceneResponse>>> selectScene(
//            @RequestBody SceneRequest request
//    ) {
//        return shortsService.generateScenes(request)
//                .map(dto -> responseUtils.build(SUCCESS, dto));
//    }
//
//    @PostMapping("/video")
//    public Mono<ResponseEntity<ApiResponse<VideoResponse>>> createVideo(
//            @RequestBody VideoRequest request
//    ) {
//        return shortsService.generateVideo(request)
//                .map(dto -> responseUtils.build(SUCCESS, dto));
//    }
}

