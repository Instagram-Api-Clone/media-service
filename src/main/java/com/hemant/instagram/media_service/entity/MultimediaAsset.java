package com.hemant.instagram.media_service.entity;

import com.hemant.instagram.media_service.entity.enums.MultimediaAssetCloudProvider;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetOwnerType;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetStatus;
import com.hemant.instagram.media_service.entity.enums.MultimediaAssetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "multimedia_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultimediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type")
    private MultimediaAssetOwnerType ownerType;

    @Column(name = "owner_id")
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MultimediaAssetType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MultimediaAssetStatus status;

    private String cloudLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MultimediaAssetCloudProvider cloudProvider;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
