package cn.booktable.geo.core;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class GeoQuery {

    private String mapId;
    private String layerId;
    private String layerSource;
    private String filter;
    private String featureId;


}
