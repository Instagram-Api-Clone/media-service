package com.hemant.instagram.media_service.service.impl;

import com.cloudinary.Cloudinary;
import com.hemant.instagram.media_service.entity.MultimediaAsset;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetCloudProvider;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetOwnerType;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetStatus;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetType;
import com.hemant.instagram.media_service.repository.MultimediaAssetRepository;
import com.hemant.instagram.media_service.service.MediaService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final List<String> IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final List<String> VIDEO_EXTENSIONS = List.of("mp4", "mov", "avi", "mkv", "webm", "m4v");

    private final MultimediaAssetRepository multimediaAssetRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public MultimediaAsset uploadMediaAsset(Long uploaderId, MultipartFile file) {
        return uploadMediaAsset(uploaderId, file, Map.of());
    }

    @Override
    @Transactional
    public MultimediaAsset uploadMediaAsset(Long uploaderId, MultipartFile file, Map<String, Object> metadata) {
        return uploadMediaAsset(uploaderId, null, null, file, metadata);
    }

    @Override
    @Transactional
    public MultimediaAsset uploadMediaAsset(
            Long uploaderId,
            MultimediaAssetOwnerType ownerType,
            Long ownerId,
            MultipartFile file,
            Map<String, Object> metadata) {
        validateUploadRequest(uploaderId, file);
        validateOwner(ownerType, ownerId);
        MultimediaAssetType assetType = resolveAssetType(file);

        log.info("Uploading media asset uploaderId={} originalFilename={} type={}", uploaderId, file.getOriginalFilename(), assetType);

        Map<?, ?> uploadResult = uploadToCloudinary(file);
        String cloudLink = extractCloudLink(uploadResult);
        Map<String, Object> assetMetadata = buildMetadata(file, uploadResult, metadata);

        MultimediaAsset asset = MultimediaAsset.builder()
                .uploaderId(uploaderId)
                .ownerType(ownerType)
                .ownerId(ownerId)
                .type(assetType)
                .status(MultimediaAssetStatus.ACTIVE)
                .cloudLink(cloudLink)
                .cloudProvider(MultimediaAssetCloudProvider.CLOUDINARY)
                .metadata(assetMetadata)
                .build();

        asset = multimediaAssetRepository.save(asset);
        log.info("Media asset persisted assetId={} uploaderId={} type={}", asset.getId(), uploaderId, assetType);
        return asset;
    }

    @Override
    @Transactional(readOnly = true)
    public MultimediaAsset getMediaAsset(Long mediaAssetId) {
        if (mediaAssetId == null) {
            throw new IllegalArgumentException("Media asset id is required");
        }
        return multimediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new IllegalArgumentException("Media asset not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MultimediaAsset> getMediaAssetsByUploader(Long uploaderId) {
        if (uploaderId == null) {
            throw new IllegalArgumentException("Uploader id is required");
        }
        return multimediaAssetRepository.findByUploaderIdAndStatus(uploaderId, MultimediaAssetStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MultimediaAsset> getMediaAssetsByOwner(MultimediaAssetOwnerType ownerType, Long ownerId) {
        if (ownerType == null) {
            throw new IllegalArgumentException("Owner type is required");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner id is required");
        }
        return multimediaAssetRepository.findByOwnerTypeAndOwnerIdAndStatus(ownerType, ownerId, MultimediaAssetStatus.ACTIVE);
    }

    @Override
    @Transactional
    public MultimediaAsset updateMediaAssetStatus(Long mediaAssetId, MultimediaAssetStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Media asset status is required");
        }
        MultimediaAsset asset = getMediaAsset(mediaAssetId);
        asset.setStatus(status);
        asset = multimediaAssetRepository.save(asset);
        log.info("Media asset status updated assetId={} status={}", asset.getId(), asset.getStatus());
        return asset;
    }

    private void validateUploadRequest(Long uploaderId, MultipartFile file) {
        if (uploaderId == null) {
            throw new IllegalArgumentException("Uploader id is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Media file is required");
        }
    }

    private void validateOwner(MultimediaAssetOwnerType ownerType, Long ownerId) {
        if ((ownerType == null && ownerId != null) || (ownerType != null && ownerId == null)) {
            throw new IllegalArgumentException("Both owner type and owner id must be provided together");
        }
    }

    private MultimediaAssetType resolveAssetType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return MultimediaAssetType.PHOTO;
            }
            if (contentType.startsWith("video/")) {
                return MultimediaAssetType.VIDEO;
            }
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return MultimediaAssetType.PHOTO;
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return MultimediaAssetType.VIDEO;
        }
        throw new IllegalArgumentException("Only photo and video uploads are supported");
    }

    private Map<?, ?> uploadToCloudinary(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(file.getBytes(), Map.of("resource_type", "auto"));
        } catch (IOException exception) {
            log.error("Cloudinary upload failed originalFilename={}", file.getOriginalFilename(), exception);
            throw new IllegalStateException("Failed to upload media asset", exception);
        }
    }

    private String extractCloudLink(Map<?, ?> uploadResult) {
        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl != null) {
            return secureUrl.toString();
        }

        Object url = uploadResult.get("url");
        if (url != null) {
            return url.toString();
        }

        throw new IllegalStateException("Cloudinary upload did not return a media URL");
    }

    private Map<String, Object> buildMetadata(
            MultipartFile file,
            Map<?, ?> uploadResult,
            Map<String, Object> requestMetadata) {
        Map<String, Object> metadata = new HashMap<>();
        if (requestMetadata != null) {
            metadata.putAll(requestMetadata);
        }

        addIfPresent(metadata, "originalFilename", file.getOriginalFilename());
        addIfPresent(metadata, "contentType", file.getContentType());
        metadata.put("size", file.getSize());
        copyUploadField(metadata, uploadResult, "public_id");
        copyUploadField(metadata, uploadResult, "resource_type");
        copyUploadField(metadata, uploadResult, "format");
        copyUploadField(metadata, uploadResult, "bytes");
        copyUploadField(metadata, uploadResult, "width");
        copyUploadField(metadata, uploadResult, "height");
        copyUploadField(metadata, uploadResult, "duration");
        return metadata;
    }

    private void copyUploadField(Map<String, Object> metadata, Map<?, ?> uploadResult, String key) {
        Object value = uploadResult.get(key);
        if (value != null) {
            metadata.put(key, value);
        }
    }

    private void addIfPresent(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
