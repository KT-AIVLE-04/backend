package kt.aivle.store.adapter.in.web;

import jakarta.validation.Valid;
import kt.aivle.common.response.ApiResponse;
import kt.aivle.common.response.ResponseUtils;
import kt.aivle.store.adapter.in.web.dto.CreateStoreRequest;
import kt.aivle.store.adapter.in.web.dto.StoreResponse;
import kt.aivle.store.adapter.in.web.dto.UpdateStoreRequest;
import kt.aivle.store.adapter.in.web.mapper.StoreCommandMapper;
import kt.aivle.store.adapter.in.web.mapper.StoreQueryMapper;
import kt.aivle.store.application.port.in.StoreUseCase;
import kt.aivle.store.application.port.in.command.CreateStoreCommand;
import kt.aivle.store.application.port.in.command.DeleteStoreCommand;
import kt.aivle.store.application.port.in.command.UpdateStoreCommand;
import kt.aivle.store.application.port.in.query.GetStoreQuery;
import kt.aivle.store.application.port.in.query.GetStoresQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static kt.aivle.common.code.CommonResponseCode.CREATED;
import static kt.aivle.common.code.CommonResponseCode.OK;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreUseCase storeUseCase;
    private final StoreCommandMapper storeCommandMapper;
    private final StoreQueryMapper storeQueryMapper;
    private final ResponseUtils responseUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores(@RequestHeader("X-USER-ID") Long userId) {
        GetStoresQuery query = storeQueryMapper.toGetStoresQuery(userId);
        List<StoreResponse> response = storeUseCase.getStores(query);
        return responseUtils.build(OK, response);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@RequestHeader("X-USER-ID") Long userId,
                                                               @PathVariable Long storeId) {
        GetStoreQuery query = storeQueryMapper.toGetStoreQuery(storeId, userId);
        StoreResponse response = storeUseCase.getStore(query);
        return responseUtils.build(OK, response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> create(@RequestHeader("X-USER-ID") Long userId,
                                                             @Valid @RequestBody CreateStoreRequest request) {
        CreateStoreCommand command = storeCommandMapper.toCreateCommand(request, userId);
        StoreResponse response = storeUseCase.createStore(command);
        return responseUtils.build(CREATED, response);
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> update(@RequestHeader("X-USER-ID") Long userId,
                                                             @Valid @RequestBody UpdateStoreRequest request,
                                                             @PathVariable Long storeId) {
        UpdateStoreCommand updateCommand = storeCommandMapper.toUpdateCommand(request, storeId, userId);
        StoreResponse response = storeUseCase.updateStore(updateCommand);
        return responseUtils.build(OK, response);
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestHeader("X-USER-ID") Long userId,
                                                    @PathVariable Long storeId) {
        DeleteStoreCommand command = storeCommandMapper.toDeleteCommand(storeId, userId);
        storeUseCase.deleteStore(command);
        return responseUtils.build(OK, null);
    }
}