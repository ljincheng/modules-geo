package cn.booktable.geo.test;

import cn.booktable.geo.core.*;
import cn.booktable.geo.service.GeoDataService;
import cn.booktable.geo.service.impl.GeoDataServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * @author ljc
 */
public class GeoDataServiceTest {
    public static void main(String[] args) throws Exception {
        GeoDataService geoDataService=new GeoDataServiceImpl();
        List<MapInfo> mapInfoList= geoDataService.mapInfoList();
        for(MapInfo mapInfo:mapInfoList){
            System.out.println(String.format("mapId=%s,title=%s,bbox=%s",mapInfo.getMapId(),mapInfo.getTitle(),mapInfo.getBbox()));
        }
    }

    @Test
    public void testMapInfoList(){
        GeoDataService geoDataService=new GeoDataServiceImpl();
        List<MapInfo> mapInfoList= geoDataService.mapInfoList();
        for(MapInfo mapInfo:mapInfoList){
            System.out.println(String.format("mapId=%s,title=%s,bbox=%s",mapInfo.getMapId(),mapInfo.getTitle(),mapInfo.getBbox()));
        }
    }

    @Test
    public void testInsert(){

        String bbox="-200,-200,200,200";
        String mapId="T20210308-001";
        GeoDataService geoDataService=new GeoDataServiceImpl();
        MapInfo mapInfo=new MapInfo();
        mapInfo.setMapId(mapId);
        mapInfo.setTitle("测试地图3");
        mapInfo.setBbox(bbox);
        geoDataService.deleteMapInfo(mapInfo);



        LayerInfo layerInfo=new LayerInfo();
        layerInfo.setLayerId("T20210308-001");
        layerInfo.setTitle("全球城市-50m");
        layerInfo.setLayerName("ne_50m_admin_0_countries");
        layerInfo.setLayerType(StyleType.POLYGON.name());
        layerInfo.setBbox(bbox);
        geoDataService.deleteLayerInfo(layerInfo);
        geoDataService.addLayerInfo(layerInfo);

        StyleInfo styleInfo=new StyleInfo();
        styleInfo.setStyleId(StyleType.POLYGON.name());
        styleInfo.setStyleType(StyleType.POLYGON.name());
        styleInfo.setTitle("面样式");
        geoDataService.deleteStyleInfo(styleInfo);
        geoDataService.addStyleInfo(styleInfo);


        MapLayer mapLayer=new MapLayer();
        mapLayer.setId(UUID.randomUUID().toString());
        mapLayer.setDisplay(1);
        mapLayer.setLayerOrder(1);
        mapLayer.setStyleId(styleInfo.getStyleId());
        mapLayer.setLayerId(layerInfo.getLayerId());
        mapLayer.setMapId(mapInfo.getMapId());
        geoDataService.deleteMapLayer(mapLayer);
        geoDataService.addMapLayer(mapLayer);

        geoDataService.addMapInfo(mapInfo);

    }

    @Test
    public void testDelete(){
        GeoDataService geoDataService=new GeoDataServiceImpl();
        MapInfo mapInfo=new MapInfo();
        mapInfo.setMapId("078d0901-0b61-44d4-a90f-5fdaf546393b");
        geoDataService.deleteMapInfo(mapInfo);
    }
}
