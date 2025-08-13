package kt.aivle.shorts.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.common.exception.BusinessException;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.shorts.adapter.in.web.dto.request.CreateScenarioRequest;
import kt.aivle.shorts.adapter.in.web.dto.request.CreateShortsRequest;
import kt.aivle.shorts.adapter.in.web.dto.request.SaveShortsRequest;
import kt.aivle.shorts.adapter.in.web.dto.response.ScenarioResponse;
import kt.aivle.shorts.adapter.in.web.dto.response.ShortsResponse;
import kt.aivle.shorts.adapter.in.web.mapper.toCommandMapper;
import kt.aivle.shorts.adapter.in.web.mapper.toResponseMapper;
import kt.aivle.shorts.application.port.in.ShortsUseCase;
import kt.aivle.shorts.application.port.in.command.CreateScenarioCommand;
import kt.aivle.shorts.application.port.in.command.CreateShortsCommand;
import kt.aivle.shorts.application.port.in.command.SaveShortsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static kt.aivle.common.code.CommonResponseCode.BAD_REQUEST;
import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsUseCase shortsUseCase;
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

    @PostMapping(consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<ShortsResponse>>> createShorts(
            @RequestPart("request") @Valid CreateShortsRequest request,
            @RequestPart("images") Flux<FilePart> images
    ) {
        if (images == null) {
            return Mono.error(new BusinessException(BAD_REQUEST, "이미지를 업로드해주세요."));
        }
        CreateShortsCommand command = toCommandMapper.toCreateShortsCommand(request, images);
        return shortsUseCase.createShorts(command)
                .map(toResponseMapper::toShortsResponse)
                .map(response -> responseUtils.build(OK, response));
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