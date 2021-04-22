package cn.booktable.appadmin.controller.wms;

import lombok.Data;

/**
 * @author ljc
 */
@Data
public class GeoQueryVO {
    private String layerSource;
    private String filter;
    private String featureId;
}
