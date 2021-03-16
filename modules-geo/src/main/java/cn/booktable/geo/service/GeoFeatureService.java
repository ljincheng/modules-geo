package cn.booktable.geo.service;

import cn.booktable.geo.entity.GeoGeometryEntity;
import org.locationtech.jts.geom.Geometry;

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
    GeoGeometryEntity createGeometry(String layerName, Geometry geometry);

    /**
     * 添加图层图形
     * @param layerName 图层名称
     * @param geometryEntity
     * @return
     */
    boolean addGeometry(String layerName, GeoGeometryEntity geometryEntity);

    /**
     * 更新图层图形
     * @param layerName 图层名称
     * @param geometryEntity
     * @return
     */
    boolean updateGeometry(String layerName,GeoGeometryEntity geometryEntity);
    boolean deleteGeometry(String layerName,String id);
    boolean queryGeometry(String layerName,GeoGeometryEntity geometryEntity);
}
