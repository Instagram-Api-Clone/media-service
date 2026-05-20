package com.hemant.instagram.media_service.repository;

import com.hemant.instagram.media_service.entity.MultimediaAsset;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetOwnerType;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultimediaAssetRepository extends JpaRepository<MultimediaAsset, Long> {

    List<MultimediaAsset> findByUploaderId(Long uploaderId);

    List<MultimediaAsset> findByUploaderIdAndStatus(Long uploaderId, MultimediaAssetStatus status);

    List<MultimediaAsset> findByOwnerTypeAndOwnerIdAndStatus(
            MultimediaAssetOwnerType ownerType,
            Long ownerId,
            MultimediaAssetStatus status);
}
