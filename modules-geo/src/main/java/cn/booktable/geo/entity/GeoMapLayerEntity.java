package cn.booktable.geo.entity;

import lombok.Data;

/**
 * 地图图层
 * @author ljc
 */
@Data
public class GeoMapLayerEntity {
    private String id;//唯一标识
    private String mapId;//地图ID
//    private String layerId;//图层ID
    private Integer layerOrder;//图层排序
    private Integer display;//是否显示
    private String styleId;//样式ID
    private String layerSource;
    private String layerType;
    private String layerFilter;
    private String envelope;
    private String title;

    private GeoStyleInfoEntity styleInfoEntity;

}
