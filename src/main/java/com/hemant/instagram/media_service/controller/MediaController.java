package com.hemant.instagram.media_service.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemant.instagram.media_service.dto.request.UpdateMediaAssetStatusRequest;
import com.hemant.instagram.media_service.entity.MultimediaAsset;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetOwnerType;
import com.hemant.instagram.media_service.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Media Asset")
    public ResponseEntity<MultimediaAsset> uploadMediaAsset(
            @RequestHeader("X-User-Id") Long uploaderId,
            @RequestParam(value = "ownerType", required = false) MultimediaAssetOwnerType ownerType,
            @RequestParam(value = "ownerId", required = false) Long ownerId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "metadata", required = false) String metadata) {
        return ResponseEntity.ok(mediaService.uploadMediaAsset(uploaderId, ownerType, ownerId, file, parseMetadata(metadata)));
    }

    @GetMapping("/{mediaAssetId}")
    @Operation(summary = "Get Media Asset by ID")
    public ResponseEntity<MultimediaAsset> getMediaAsset(@PathVariable Long mediaAssetId) {
        return ResponseEntity.ok(mediaService.getMediaAsset(mediaAssetId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get My Media Assets")
    public ResponseEntity<List<MultimediaAsset>> getMyMediaAssets(
            @RequestHeader("X-User-Id") Long uploaderId) {
        return ResponseEntity.ok(mediaService.getMediaAssetsByUploader(uploaderId));
    }

    @GetMapping
    @Operation(summary = "Get Media Assets by Owner")
    public ResponseEntity<List<MultimediaAsset>> getMediaAssetsByOwner(
            @RequestParam MultimediaAssetOwnerType ownerType,
            @RequestParam Long ownerId) {
        return ResponseEntity.ok(mediaService.getMediaAssetsByOwner(ownerType, ownerId));
    }

    @PatchMapping("/{mediaAssetId}/status")
    @Operation(summary = "Update Media Asset Status")
    public ResponseEntity<MultimediaAsset> updateMediaAssetStatus(
            @PathVariable Long mediaAssetId,
            @RequestBody UpdateMediaAssetStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update media asset status request is required");
        }
        return ResponseEntity.ok(mediaService.updateMediaAssetStatus(mediaAssetId, request.getStatus()));
    }

    private Map<String, Object> parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new IllegalArgumentException("Metadata must be a valid JSON object", exception);
        }
    }
}
