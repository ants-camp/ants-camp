package io.antcamp.assetservice.presentation.controller;

import common.dto.CommonResponse;
import io.antcamp.assetservice.application.dto.query.AssetResult;
import io.antcamp.assetservice.application.service.AssetService;
import io.antcamp.assetservice.presentation.controller.docs.AssetControllerDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController implements AssetControllerDocs {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<CommonResponse<AssetResult>> getAsset(
            @RequestParam UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        AssetResult result = assetService.getAsset(accountId, userId);
        return CommonResponse.ok(result);
    }
}