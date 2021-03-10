package cn.booktable.geo.provider;

import cn.booktable.geo.core.*;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.FeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ljc
 */
public class FeatureSourceProvider {

    public static Map<String, LayerInfo> mLayerInfoMap=new HashMap<>();
    public static Map<String, StyleInfo> mStyleInfoMap=new HashMap<>();
    public static MapInfoProvider mMapInfoProvider=null;
    public static Map<String,List<Layer>> mMapInfoMap=new HashMap<>();
    private static FeatureSourceProvider mInstance=null;

    static {
        if(mMapInfoProvider==null) {
            mMapInfoProvider = MapInfoProvider.instance();
            reload();
        }
    }

    public static void reload(){
        mMapInfoMap.clear();
        MapInfoProvider.reload();
        List<LayerInfo> layerInfos = mMapInfoProvider.layerInfoList();
        List<StyleInfo> styleInfos = mMapInfoProvider.styleInfoList();
        List<MapLayer> mapLayers = mMapInfoProvider.mapLayerList();
        if (layerInfos != null) {
            for (LayerInfo layerInfo : layerInfos) {
                mLayerInfoMap.put(layerInfo.getLayerId(), layerInfo);
            }
        }
        if (styleInfos != null) {
            for (StyleInfo styleInfo : styleInfos) {
                mStyleInfoMap.put(styleInfo.getStyleId(), styleInfo);
            }
        }
    }

    public static FeatureSourceProvider instance(){
        if(mInstance==null){
            mInstance=new FeatureSourceProvider();
        }
        return mInstance;
    }

    private LayerInfo findLayerInfo(List<LayerInfo> list,String layerId){
        for(LayerInfo layerInfo:list){
            if(layerInfo.getLayerId().equals(layerId)){
                return layerInfo;
            }
        }
        return null;
    }

    public List<Layer> getLayers(String mapId){
        List<Layer> layerList= mMapInfoMap.get(mapId);
        if(layerList!=null && layerList.size()>0){
            return layerList;
        }
        layerList=new ArrayList<>();
        List<MapLayer> mapLayers= mMapInfoProvider.mapLayerList();
        if(mapLayers==null){
            return layerList;
        }
        for(MapLayer mapLayer:mapLayers){
            if(mapLayer.getMapId().equals(mapId)){
                LayerInfo layerInfo=mLayerInfoMap.get(mapLayer.getLayerId());
                StyleInfo styleInfo=mStyleInfoMap.get(mapLayer.getStyleId());
                if(layerInfo!=null && styleInfo!=null){
                    FeatureSource fs=getFeatureSource(layerInfo);
                    Style style=getStyle(styleInfo);
                    if(fs!=null && style!=null) {
                        Layer layer = new FeatureLayer(fs, style);
                        layerList.add(layer);
                    }
                }
            }
        }
        if(layerList.size()>0) {
            mMapInfoMap.put(mapId, layerList);
        }
        return layerList;
    }

    private FeatureSource getFeatureSource(LayerInfo layerInfo){
        try {
            FeatureSource featureSource = DBHelper.dataStore().getFeatureSource(layerInfo.getLayerName());
            return featureSource;
        }catch (IOException ex){
            throw new GeoException(ex);
        }
    }

    private Style getStyle(StyleInfo styleInfo){
        Style style= null;
        StyleGenerator styleGenerator=StyleGenerator.instance();
        StyleType styleType=styleGenerator.parseStyleType(styleInfo.getStyleType());

        if(StringUtils.isNotBlank(styleInfo.getContent())){
            style=styleGenerator.toStyle(styleInfo.getContent(),styleType);
        }else {
            style=styleGenerator.defaultStyle(styleType);
        }
        return style;
    }
}
