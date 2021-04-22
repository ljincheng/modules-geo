package cn.booktable.geo.core;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ljc
 */
@Data
public class GeoFeature {
    private String id;
    private String mapId;
    private String layerSource;
    private String geometry;//几何数据
    private Map<String,Object> properties=new HashMap<>();


}
