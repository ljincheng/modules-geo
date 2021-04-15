package cn.booktable.geo.core;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ljc
 */
@Data
public class GeoQuery {

    private String mapId;
    private String layerId;
//    private String layerName;
    private String filter;
    private String featureId;


}
