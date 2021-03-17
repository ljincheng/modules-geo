package cn.booktable.geo.core;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class GeoFeatureRequest {
    private String type;//操作类型
    private String layerName;//图层名称
    private GeoFeature feature;
    private GeoQuery query;

}
