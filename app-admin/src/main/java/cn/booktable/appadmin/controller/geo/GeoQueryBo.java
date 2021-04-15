package cn.booktable.appadmin.controller.geo;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class GeoQueryBo {
    private String layerId;
    private String filter;
    private String featureId;
}
