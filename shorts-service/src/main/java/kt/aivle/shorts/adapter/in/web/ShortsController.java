package kt.aivle.shorts.adapter.in.web;

import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.shorts.adapter.in.web.dto.request.CreateScenarioRequest;
import kt.aivle.shorts.adapter.in.web.dto.response.ScenarioResponse;
import kt.aivle.shorts.adapter.in.web.mapper.CommandMapper;
import kt.aivle.shorts.adapter.in.web.mapper.ResponseMapper;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsUseCase shortsUseCase;
    private final ResponseUtils responseUtils;
    private final CommandMapper commandMapper;
    private final ResponseMapper responseMapper;

    @PostMapping(value = "/scenario", consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<ScenarioResponse>>> createScenario(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-STORE-ID") Long storeId,
            @RequestPart("images") Flux<FilePart> images,
            @RequestPart("request") CreateScenarioRequest request
    ) {
        CreateScenarioCommand command = commandMapper.toCreateScenarioCommand(userId, storeId, request, images);
        return shortsUseCase.createScenario(command)
                .map(responseMapper::toScenarioResponse)
                .map(dto -> responseUtils.build(OK, dto));
    }

//    @PostMapping("/scene")
//    public Mono<ResponseEntity<ApiResponse<SceneResponse>>> selectScene(@RequestBody SceneRequest request) {
//        CreateSceneCommand command = commandMapper.toCreateSceneCommand(request.sessionId(), request.title(), request.content());
//        return shortsUseCase.createScene(command).map(dto -> responseUtils.build(OK, dto));
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

