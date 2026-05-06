package io.antcamp.assetservice.presentation.controller;

import common.dto.ApiResponse;
import io.antcamp.assetservice.application.dto.query.AssetResult;
import io.antcamp.assetservice.application.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<ApiResponse<AssetResult>> getAsset(
            @RequestParam UUID accountId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        AssetResult result = assetService.getAsset(accountId, userId);
        return ApiResponse.ok(result);
    }
}