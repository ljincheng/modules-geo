package cn.booktable.geo.test;

import cn.booktable.geo.entity.GeoGeometryEntity;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
public class GeoFeatureServiceTest {

    private String mLayerName="geo_parking_polygon";
    @Test
    public void testWriter(){

        GeoGeometryEntity geometryEntity=new GeoGeometryEntity();
        Polygon geom=new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(-10,0),new Coordinate(-10,100),new Coordinate(100,100),new Coordinate(100,0),new Coordinate(-10,0)});
        geometryEntity.setGeometry(geom);
        geometryEntity.setType("polygon");
        Map<String,Object> attr=new HashMap<>();
        attr.put("building_id","b005");
        attr.put("parking_no","车位W-XX");
        geometryEntity.setProperties(attr);
        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        featureService.addGeometry(mLayerName,geometryEntity);
    }

    @Test
    public void testEmptyGeomEntity(){
        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        GeoGeometryEntity geometryEntity= featureService.createGeometry(mLayerName,null);
        System.out.println("geometryEntity OS");
    }

}
