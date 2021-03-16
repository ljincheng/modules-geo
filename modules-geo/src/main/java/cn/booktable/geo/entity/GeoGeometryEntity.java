package cn.booktable.geo.entity;

import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.coordinate.Polygon;

import java.util.Map;

/**
 *
 * @author ljc
 */
@Data
public class GeoGeometryEntity {
    private String id;
    private String type;
    private String name;
    private Geometry geometry;//几何数据
    private Map<String,Object> properties;

}
