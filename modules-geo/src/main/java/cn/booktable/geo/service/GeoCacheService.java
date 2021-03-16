package cn.booktable.geo.service;

import cn.booktable.geo.entity.GeoImageCacheEntity;

/**
 * @author ljc
 */
public interface GeoCacheService {

    /**
     * 保存图片缓存
     * @param imageCache
     * @return
     */
    boolean saveCache(GeoImageCacheEntity imageCache);

    GeoImageCacheEntity findCache(String cacheId);
}
