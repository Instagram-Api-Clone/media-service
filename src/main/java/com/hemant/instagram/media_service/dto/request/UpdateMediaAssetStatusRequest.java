package com.hemant.instagram.media_service.dto.request;

import com.hemant.instagram.media_service.entity.enums.MultimediaAssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMediaAssetStatusRequest {

    private MultimediaAssetStatus status;
}
