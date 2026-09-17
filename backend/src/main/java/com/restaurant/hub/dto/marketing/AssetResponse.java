package com.restaurant.hub.dto.marketing;

import com.restaurant.hub.entity.MarketingAsset;

public record AssetResponse(
        Long id,
        String name,
        String assetType,
        String url
) {
    public static AssetResponse from(MarketingAsset a) {
        return new AssetResponse(a.getId(), a.getName(), a.getAssetType(), a.getUrl());
    }
}
