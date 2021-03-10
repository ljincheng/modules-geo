package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.*;
import cn.booktable.geo.service.GeoDataService;

import javax.swing.text.Style;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

/**
 * @author ljc
 */
public class GeoDataServiceImpl implements GeoDataService {

    private static List<MapInfo> mMapInfos=null;
    private static List<LayerInfo> mLayerInfos=null;
    private static List<StyleInfo> mStyleInfos=null;
    private static List<MapLayer> mMapLayers=null;
    static {
        Connection conn=DBHelper.getConnection();
        try {
            mMapInfos = DBHelper.queryList(conn, MapInfo.TABLENAME, MapInfo.COLUMNS, null, MapInfo.class);
            mLayerInfos=DBHelper.queryList(conn,LayerInfo.TABLENAME,LayerInfo.COLUMNS,null,LayerInfo.class);
            mStyleInfos=DBHelper.queryList(conn,StyleInfo.TABLENAME,StyleInfo.COLUMNS,null,StyleInfo.class);
            mMapLayers=DBHelper.queryList(conn,MapLayer.TABLENAME,MapLayer.COLUMNS,null,MapLayer.class);

        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public List<MapInfo> mapInfoList() {
        return mMapInfos;
    }

    @Override
    public List<LayerInfo> layerInfoList() {
        return mLayerInfos;
    }

    @Override
    public List<StyleInfo> styleInfoList() {
        return mStyleInfos;
    }

    @Override
    public List<MapLayer> mapLayerList() {
        return mMapLayers;
    }

    @Override
    public boolean addMapInfo(MapInfo mapInfo) {
        Connection conn=DBHelper.getConnection();
        try {
            return mapInfo.insert(conn);
        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean addLayerInfo(LayerInfo layerInfo) {
        Connection conn=DBHelper.getConnection();
        try {
            return layerInfo.insert(conn);
        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean addStyleInfo(StyleInfo styleInfo) {
        Connection conn=DBHelper.getConnection();
        try {
            return styleInfo.insert(conn);
        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean addMapLayer(MapLayer mapLayer) {
        Connection conn=DBHelper.getConnection();
        try {
            return mapLayer.insert(conn);
        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean deleteMapInfo(MapInfo mapInfo) {
        Connection conn=DBHelper.getConnection();
        try {
            boolean result= mapInfo.delete(conn);
            if(result) {
                for (MapInfo info : mMapInfos) {
                    if (info.getMapId().equals(mapInfo.getMapId())) {
                        mMapInfos.remove(info);
                        break;
                    }
                }
                Iterator<MapLayer> iterator=mMapLayers.iterator();
                while (iterator.hasNext()){
                    MapLayer mapLayer=iterator.next();
                    if( mapLayer.getMapId().equals(mapInfo.getMapId())){
                        mapLayer.delete(conn);
                        mMapLayers.remove(mapLayer);
                    }
                }
            }
            return result;
        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean deleteLayerInfo(LayerInfo layerInfo) {
        Connection conn=DBHelper.getConnection();
        try {
            boolean result= layerInfo.delete(conn);
            if(result) {
                for (LayerInfo info : mLayerInfos) {
                    if (info.getLayerId().equals(layerInfo.getLayerId())) {
                        mLayerInfos.remove(info);
                        break;
                    }
                }
                for(MapLayer mapLayer:mMapLayers){
                    if( mapLayer.getStyleId().equals(layerInfo.getLayerId())){
                        mMapLayers.remove(mapLayer);
                        break;
                    }
                }
            }
            return result;
        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean deleteStyleInfo(StyleInfo styleInfo) {
        Connection conn=DBHelper.getConnection();
        try {
            boolean result= styleInfo.delete(conn);
            if(result) {
                for (StyleInfo info : mStyleInfos) {
                    if (info.getStyleId().equals(styleInfo.getStyleId())) {
                        mStyleInfos.remove(info);
                         break;
                    }
                }
                for(MapLayer mapLayer:mMapLayers){
                   if( mapLayer.getStyleId().equals(styleInfo.getStyleId())){
                       mMapLayers.remove(mapLayer);
                       break;
                   }
                }
            }
            return result;

        }finally {
            DBHelper.close(conn);
        }
    }

    @Override
    public boolean deleteMapLayer(MapLayer mapLayer) {
        Connection conn=DBHelper.getConnection();
        try {
            boolean result= mapLayer.delete(conn);
            if(result) {
                for(MapLayer layer:mMapLayers){
                    if( layer.getId().equals(mapLayer.getId())){
                        mMapLayers.remove(mapLayer);
                        break;
                    }
                }
            }
            return result;
        }finally {
            DBHelper.close(conn);
        }
    }
}
