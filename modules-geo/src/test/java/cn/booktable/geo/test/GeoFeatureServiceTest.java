package cn.booktable.geo.test;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author ljc
 */
public class GeoFeatureServiceTest {

    @AfterAll
    public static void dispose(){
        DBHelper.dispose();
    }



    private String mLayerName="geo_parking_polygon";
    //@Test
    public void testWriter(){

        GeoFeature geometryEntity=new GeoFeature();
        Polygon geom=new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(-10,0),new Coordinate(-10,100),new Coordinate(100,100),new Coordinate(100,0),new Coordinate(-10,0)});
//        geometryEntity.setGeometry(geom);
        geometryEntity.setType("polygon");
        Map<String,Object> attr=new HashMap<>();
        attr.put("building_id","b007");
        attr.put("parking_no","车位W-XX");
        geometryEntity.setProperties(attr);
        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        featureService.addFeature(mLayerName,geometryEntity);
    }

    //@Test
    public void testUpdate(){

        GeoQuery query=new GeoQuery();
        query.setFilter(" building_id='b007' ");
        GeoFeature geometryEntity=new GeoFeature();
        Polygon geom=new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(-10,0),new Coordinate(-10,100),new Coordinate(120,100),new Coordinate(100,0),new Coordinate(-10,0)});
//        geometryEntity.setGeometry(geom);
        geometryEntity.setType("polygon");
        Map<String,Object> attr=new HashMap<>();
        attr.put("building_id","b008");
        attr.put("parking_no","车位W-XX");
        geometryEntity.setProperties(attr);

        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        featureService.updateFeature(query,geometryEntity);
    }

    //@Test
    public void testDelete(){
        GeoQuery query=new GeoQuery();
        query.setLayerName(mLayerName);
        query.setFilter(" building_id='b007' ");
        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        featureService.deleteFeature(query);
    }

    //@Test
    public void testQuery(){
        GeoQuery query=new GeoQuery();
        query.setLayerName(mLayerName);
        query.setFilter(" building_id='b007' ");
        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        List<GeoFeature> entityList=featureService.queryFeature(query);
        for(GeoFeature entity:entityList){

            System.out.println("entity:"+entity.getProperties().size());
        }
    }

   // @Test
    public void testEmptyGeomEntity(){
        GeoFeatureService featureService=new GeoFeatureServiceImpl();
        GeoFeature geometryEntity= featureService.createGeometry(mLayerName,null);
        System.out.println("geometryEntity OS");
    }

}
