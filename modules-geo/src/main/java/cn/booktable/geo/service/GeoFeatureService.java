package cn.booktable.geo.service;

import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.GeoFeature;
import org.geotools.feature.FeatureCollection;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

/**
 * 地图要素服务
 * @author ljc
 */
public interface GeoFeatureService {


    /**
     * 添加图层图形
     * @param geometryEntity
     * @return
     */
    boolean addFeature(GeoFeature geometryEntity);

    /**
     * 更新图层图形
     * @param query 图层名称
     * @param geometryEntity
     * @return
     */
    boolean updateFeature(GeoQuery query, GeoFeature geometryEntity);

    boolean deleteFeature(GeoQuery query);

    GeoFeature findFeatureById(GeoQuery query);

    List<GeoFeature> queryFeature(GeoQuery query);


    List<GeoFeature> queryFeatureByMapLayerId(GeoQuery query);

     void writeFeatureByMapLayerSource(GeoQuery query, Object output);
}
