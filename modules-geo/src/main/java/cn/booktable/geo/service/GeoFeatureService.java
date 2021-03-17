package cn.booktable.geo.service;

import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.GeoFeature;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

/**
 * 地图要素服务
 * @author ljc
 */
public interface GeoFeatureService {

    /**
     * 创建图层图形
     * @param layerName 图层名称
     * @param geometry 几何对象
     * @return
     */
    GeoFeature createGeometry(String layerName, Geometry geometry);

    /**
     * 添加图层图形
     * @param layerName 图层名称
     * @param geometryEntity
     * @return
     */
    boolean addFeature(String layerName, GeoFeature geometryEntity);

    /**
     * 更新图层图形
     * @param query 图层名称
     * @param geometryEntity
     * @return
     */
    boolean updateFeature(GeoQuery query, GeoFeature geometryEntity);

    boolean deleteFeature(GeoQuery query);

    List<GeoFeature> queryFeature(GeoQuery query);
}
