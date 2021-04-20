package cn.booktable.geo.core;

import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import org.geotools.jdbc.JDBCDataStore;

/**
 * @author ljc
 */
public interface GeoEngine {
    JDBCDataStore getDataStore();
    GeoMapService getGeoMapService();
    GeoFeatureService getGeoFeatureService();
    GeoCacheService getGeoCacheService();
    GeoMapManageService getGeoMapManageService();
}
