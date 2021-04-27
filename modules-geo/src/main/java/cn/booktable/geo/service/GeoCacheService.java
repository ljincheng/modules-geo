package cn.booktable.geo.service;

import cn.booktable.geo.core.GeoQuery;
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

    boolean deleteCache(String filter);

    boolean deleteCacheByMapId(String mapId);

    boolean clearAll();

    GeoImageCacheEntity findCache(String cacheId);
}
