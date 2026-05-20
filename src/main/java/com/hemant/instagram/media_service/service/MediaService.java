package com.hemant.instagram.media_service.service;

import com.hemant.instagram.media_service.entity.MultimediaAsset;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetOwnerType;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetStatus;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MultimediaAsset uploadMediaAsset(Long uploaderId, MultipartFile file);

    MultimediaAsset uploadMediaAsset(Long uploaderId, MultipartFile file, Map<String, Object> metadata);

    MultimediaAsset uploadMediaAsset(
            Long uploaderId,
            MultimediaAssetOwnerType ownerType,
            Long ownerId,
            MultipartFile file,
            Map<String, Object> metadata);

    MultimediaAsset getMediaAsset(Long mediaAssetId);

    List<MultimediaAsset> getMediaAssetsByUploader(Long uploaderId);

    List<MultimediaAsset> getMediaAssetsByOwner(MultimediaAssetOwnerType ownerType, Long ownerId);

    MultimediaAsset updateMediaAssetStatus(Long mediaAssetId, MultimediaAssetStatus status);
}
