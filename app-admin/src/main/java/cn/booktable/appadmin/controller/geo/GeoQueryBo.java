package cn.booktable.appadmin.controller.geo;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class GeoQueryBo {
    private String layerSource;
    private String filter;
    private String featureId;
}
