package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import cn.booktable.geo.entity.GeoStyleInfoEntity;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.utils.FeatureUtil;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author ljc
 */
public class GeoMapManageServiceImpl implements GeoMapManageService {
    private JDBCDataStore mDataStore;
    public static final String TB_MAP_INFO="geo_map_info";
    public static final String TB_MAP_LAYER="geo_map_layer";
    public GeoMapManageServiceImpl(JDBCDataStore dataStore){
        mDataStore=dataStore;
    }

    @Override
    public List<GeoMapLayerEntity> fullMapLayersByMapId(String mapId) {
        List<GeoMapLayerEntity> mapInfoList=null;
//        Transaction tran =null;
//        Connection conn=null;
        try (Transaction tran =new DefaultTransaction();Connection conn=mDataStore.getConnection(tran)) {
//            tran = new DefaultTransaction();
//            conn= mDataStore.getConnection(tran);
            mapInfoList=mapLayerFullColumnListByMapId(conn,mapId,null);
            tran.commit();
        }catch (Exception ex){
//            DBHelper.rollback(tran);
            throw new GeoException(ex);
//        }finally {
//            DBHelper.close(conn);
//            DBHelper.close(tran);
        }
        return mapInfoList;
    }

    @Override
    public GeoMapInfoEntity findBaseMapInfo(String mapId) {
        GeoMapInfoEntity mapInfoEntity=null;
        try (Transaction tran=new DefaultTransaction(); Connection conn=mDataStore.getConnection(tran)) {
            mapInfoEntity=findMapInfoEntity(conn,mapId);
            tran.commit();
        }catch (Exception ex){
            throw new GeoException(ex.fillInStackTrace());
        }
        return mapInfoEntity;
    }

    @Override
    public List<GeoMapInfoEntity> projectMapInfoList(String projectId) {

        List<GeoMapInfoEntity> list=null;
        try  (Transaction tran=new DefaultTransaction(); Connection conn=mDataStore.getConnection(tran)){
            list=projectMapInfoListByProjectId(conn,projectId);
            tran.commit();
        }catch (Exception ex){
            throw new GeoException(ex.fillInStackTrace());
        }
        return list;
    }

    @Override
    public GeoMapLayerEntity queryMapLayersByLayerId(String mapId, String layerId) {
        GeoMapLayerEntity mapLayerEntity=null;
        try{
            try (Transaction tran=new DefaultTransaction(); Connection conn=mDataStore.getConnection(tran)) {
                mapLayerEntity = mapLayerFullColumnListByLayerId(conn, mapId, layerId);
                tran.commit();
            }
        }catch (Exception ex){
            throw new GeoException(ex);
        }
        return mapLayerEntity;
    }

    @Override
    public List<GeoMapLayerEntity> queryMapLayerByLayerSource(String mapId, String layerSource) {
        List<GeoMapLayerEntity>  mapInfoList=null;
        try (Transaction tran=new DefaultTransaction(); Connection conn=mDataStore.getConnection(tran)) {
             mapInfoList=mapLayerFullColumnListByMapId(conn,mapId,layerSource);
            tran.commit();
        }catch (Exception ex){
            throw new GeoException(ex);
        }
        return mapInfoList;
    }

    @Override
    public boolean createProjectMap(GeoMapInfoEntity mapInfoEntity) {
        boolean result=false;
        try (Transaction transaction = new DefaultTransaction()) {
            Map<String,Object> atts=new HashMap<>();
            atts.put("map_id",mapInfoEntity.getMapId());
            atts.put("project_id",mapInfoEntity.getProjectId());
            atts.put("bbox",mapInfoEntity.getBbox());
            atts.put("zoom",mapInfoEntity.getZoom());
            atts.put("min_zoom",mapInfoEntity.getMinZoom());
            atts.put("max_zoom",mapInfoEntity.getMaxZoom());
            atts.put("center",mapInfoEntity.getCenter());
            atts.put("title",mapInfoEntity.getTitle());
            atts.put("sub_title",mapInfoEntity.getSubTitle());
            atts.put("project_order",mapInfoEntity.getProjectOrder());
            String id=mapInfoEntity.getMapId();

            FeatureUtil.addFeature(transaction,mDataStore,TB_MAP_INFO,atts,id);
            if(mapInfoEntity.getMapLayers()!=null ){
                int len=mapInfoEntity.getMapLayers().size();
                for(GeoMapLayerEntity layerEntity:mapInfoEntity.getMapLayers()){
                    Map<String,Object> layerAtts=new HashMap<>();
                    layerAtts.put("map_id",layerEntity.getMapId());
                    layerAtts.put("id",layerEntity.getId());
                    layerAtts.put("layer_order",layerEntity.getLayerOrder());
                    layerAtts.put("display",layerEntity.getDisplay());
                    layerAtts.put("style_id",layerEntity.getStyleId());
                    layerAtts.put("layer_source",layerEntity.getLayerSource());
                    layerAtts.put("layer_type",layerEntity.getLayerType());
                    layerAtts.put("layer_filter",layerEntity.getLayerFilter());
                    layerAtts.put("envelope",layerEntity.getEnvelope());
                    layerAtts.put("title",layerEntity.getTitle());
                    String layerId=layerEntity.getId();
                    FeatureUtil.addFeature(transaction,mDataStore,TB_MAP_LAYER,layerAtts,layerId);
                }
            }
            transaction.commit();
            result=true;
        } catch (Exception e) {
            throw new GeoException(e.fillInStackTrace());
        }
        return result;
    }


    @Override
    public boolean modifyMapInfo(GeoMapInfoEntity mapInfo) {
        GeoMapInfoEntity oldMapInfo= findBaseMapInfo(mapInfo.getMapId());
        if(oldMapInfo!=null) {
            String[] names=new String[]{"project_id","bbox","zoom","min_zoom","max_zoom","center","title","sub_title","project_order"};
            Object[] values=new Object[]{mapInfo.getProjectId(),mapInfo.getBbox(),mapInfo.getZoom(),mapInfo.getMinZoom(),mapInfo.getMaxZoom(),mapInfo.getCenter(),mapInfo.getTitle(),mapInfo.getSubTitle(),mapInfo.getProjectOrder()};
            return FeatureUtil.modifyFeatureById(mDataStore, TB_MAP_INFO, names, values, mapInfo.getMapId());
        }
        return false;
    }

    @Override
    public boolean deleteMapInfoByMapId(String mapId) {
        boolean result=false;
        try (Transaction transaction = new DefaultTransaction()) {
            deleteMapInfoByMapId(transaction,mapId);
            transaction.commit();
            result= true;
        }catch (IOException ex){
            throw new GeoException(ex);
        }
        return result;
    }

    @Override
    public boolean deleteMapInfoByProject(String projectId) {
        boolean result=false;
        List<GeoMapInfoEntity> maps= projectMapInfoList(projectId);
        if(maps==null || maps.size()<0){
            return result;
        }
        try (Transaction transaction = new DefaultTransaction()) {
            for(GeoMapInfoEntity map:maps){
                deleteMapInfoByMapId(transaction,map.getMapId());
            }
            transaction.commit();
            result= true;
        }catch (IOException ex){
            throw new GeoException(ex);
        }
        return result;
    }

    private void deleteMapInfoByMapId(Transaction transaction ,String mapId)throws IOException {
            List<GeoMapLayerEntity> layers = queryMapLayerByLayerSource(mapId, null);
            if (layers != null) {
                for (GeoMapLayerEntity layer : layers) {
                    FeatureUtil.deleteFeatureById(transaction,mDataStore, TB_MAP_LAYER, layer.getId());
                }
            }
            FeatureUtil.deleteFeatureById(transaction,mDataStore, TB_MAP_INFO, mapId);
    }

    private GeoMapInfoEntity findMapInfoEntity(Connection conn, String mapId){
        GeoMapInfoEntity mapInfoEntity=null;
        String sql="SELECT map_id, title, bbox,zoom,min_zoom,max_zoom,center,project_id,sub_title,project_order FROM geo_map_info WHERE  map_id=?";
        try ( PreparedStatement ps= conn.prepareStatement(sql)){
            ps.setString(1, mapId);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                mapInfoEntity=new GeoMapInfoEntity();
                mapInfoEntity.setMapId(res.getString(1));
                mapInfoEntity.setTitle(res.getString(2));
                mapInfoEntity.setBbox(res.getString(3));
                mapInfoEntity.setZoom(res.getInt(4));
                mapInfoEntity.setMinZoom(res.getInt(5));
                mapInfoEntity.setMaxZoom(res.getInt(6));
                mapInfoEntity.setCenter(res.getString(7));
                mapInfoEntity.setProjectId(res.getString(8));
                mapInfoEntity.setSubTitle(res.getString(9));
                mapInfoEntity.setProjectOrder(res.getInt(10));
            }
        }catch (Exception ex){
            throw new GeoException(ex);
//        }finally {
//            DBHelper.close(ps);
        }
        return mapInfoEntity;
    }

    private List<GeoMapInfoEntity> projectMapInfoListByProjectId(Connection conn,  String projectId){
        List<GeoMapInfoEntity> mapInfoList=new ArrayList<>();
        String sql="SELECT map_id, title, bbox,zoom,min_zoom,max_zoom,center,project_id,sub_title,project_order FROM geo_map_info WHERE  project_id=? order by  project_order desc";
        try (PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1, projectId);
            ResultSet res = ps.executeQuery();
            while (res.next()) {
                GeoMapInfoEntity mapInfoEntity=new GeoMapInfoEntity();
                mapInfoEntity.setMapId(res.getString(1));
                mapInfoEntity.setTitle(res.getString(2));
                mapInfoEntity.setBbox(res.getString(3));
                mapInfoEntity.setZoom(res.getInt(4));
                mapInfoEntity.setMinZoom(res.getInt(5));
                mapInfoEntity.setMaxZoom(res.getInt(6));
                mapInfoEntity.setCenter(res.getString(7));
                mapInfoEntity.setProjectId(res.getString(8));
                mapInfoEntity.setSubTitle(res.getString(9));
                mapInfoEntity.setProjectOrder(res.getInt(10));
                mapInfoList.add(mapInfoEntity);
            }
        }catch (Exception ex){
            throw new GeoException(ex.fillInStackTrace());
        }
        return mapInfoList;
    }

    private GeoMapLayerEntity mapLayerFullColumnListByLayerId(Connection conn, String mapId,String layerId) {
        GeoMapLayerEntity mapLayerEntity=null;
        String sql="SELECT t2.id, t2.map_id,  t2.layer_order, t2.display, t2.style_id,t2.layer_source,t2.layer_type,t2.layer_filter,t2.envelope,t2.title \n" +
                ",t3.title as s_i_title, t3.style_type, t3.content \n" +
                "from geo_map_layer t2 \n" +
                "left join geo_style_info t3 on t3.style_id=t2.style_id\n" +
                "where t2.map_id=? and t2.id=?";
        PreparedStatement ps=null;
        try {

                ps = conn.prepareStatement(sql);
                ps.setString(1, mapId);
                ps.setString(2, layerId);

            ResultSet res = ps.executeQuery();
            while (res.next()) {
                GeoMapLayerEntity obj = new GeoMapLayerEntity();
                obj.setId(res.getString(1));
                obj.setMapId(res.getString(2));
                obj.setLayerOrder(res.getInt(3));
                obj.setDisplay(res.getInt(4));
                obj.setStyleId(res.getString(5));
                obj.setLayerSource(res.getString(6));
                obj.setLayerType(res.getString(7));
                obj.setLayerFilter(res.getString(8));
                obj.setEnvelope(res.getString(9));
                obj.setTitle(res.getString(10));
                GeoStyleInfoEntity styleInfo=new GeoStyleInfoEntity();
                styleInfo.setStyleId(obj.getStyleId());
                styleInfo.setTitle(res.getString(11));
                styleInfo.setStyleType(res.getString(12));
                styleInfo.setContent(res.getString(13));
                obj.setStyleInfoEntity(styleInfo);
                mapLayerEntity= obj;
            }

        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
        return mapLayerEntity;
    }

    private List<GeoMapLayerEntity> mapLayerFullColumnListByMapId(Connection conn, String mapId,String layerSource) {
        List<GeoMapLayerEntity> mapInfoList=new ArrayList<GeoMapLayerEntity>();
        String sql="SELECT t2.id, t2.map_id,  t2.layer_order, t2.display, t2.style_id,t2.layer_source,t2.layer_type,t2.layer_filter,t2.envelope,t2.title \n" +
                ",t3.title as s_i_title, t3.style_type, t3.content \n" +
                "from geo_map_layer t2 \n" +
                "left join geo_style_info t3 on t3.style_id=t2.style_id\n" +
                "where t2.map_id=? ";
        PreparedStatement ps=null;
        try {
            String orderBy=" order by t2.map_id desc, t2.layer_order asc";
            if(layerSource==null || layerSource.length()==0) {
                sql=sql+orderBy;
                ps = conn.prepareStatement(sql);
                ps.setString(1, mapId);
            }else{
                sql=sql+" and  t2.layer_source=?"+orderBy;
                ps = conn.prepareStatement(sql);
                ps.setString(1, mapId);
                ps.setString(2, layerSource);
            }

            ResultSet res = ps.executeQuery();
            while (res.next()) {
                GeoMapLayerEntity obj = new GeoMapLayerEntity();
                obj.setId(res.getString(1));
                obj.setMapId(res.getString(2));
                obj.setLayerOrder(res.getInt(3));
                obj.setDisplay(res.getInt(4));
                obj.setStyleId(res.getString(5));
                obj.setLayerSource(res.getString(6));
                obj.setLayerType(res.getString(7));
                obj.setLayerFilter(res.getString(8));
                obj.setEnvelope(res.getString(9));
                obj.setTitle(res.getString(10));
                GeoStyleInfoEntity styleInfo=new GeoStyleInfoEntity();
                styleInfo.setStyleId(obj.getStyleId());
                styleInfo.setTitle(res.getString(11));
                styleInfo.setStyleType(res.getString(12));
                styleInfo.setContent(res.getString(13));
                obj.setStyleInfoEntity(styleInfo);
                mapInfoList.add(obj);
            }

        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
        return mapInfoList;
    }


    @Override
    public boolean addMapLayer(GeoMapLayerEntity mapLayerEntity) {
        Map<String, Object> atts = new HashMap<>();
        atts.put("id",mapLayerEntity.getId());
        atts.put("map_id",mapLayerEntity.getMapId());
        atts.put("display",mapLayerEntity.getDisplay());
        atts.put("envelope",mapLayerEntity.getEnvelope());
        atts.put("layer_filter",mapLayerEntity.getLayerFilter());
        atts.put("layer_order",mapLayerEntity.getLayerOrder());
        atts.put("layer_source",mapLayerEntity.getLayerSource());
        atts.put("layer_type",mapLayerEntity.getLayerType());
        atts.put("style_id",mapLayerEntity.getStyleId());
        atts.put("title",mapLayerEntity.getTitle());
        return FeatureUtil.addFeature(mDataStore,TB_MAP_LAYER,atts,mapLayerEntity.getId());
    }

    @Override
    public boolean deleteMapLayer(String layerId) {
        return FeatureUtil.deleteFeatureById(mDataStore,TB_MAP_LAYER,layerId);
    }

    @Override
    public boolean modifyMapLayerOrder(String mapId, int oldIndex, int newIndex) {
        List<GeoMapLayerEntity> layerEntityList=fullMapLayersByMapId(mapId);
        if(layerEntityList!=null && layerEntityList.size()>0){
            int num=layerEntityList.size();

            if(num>oldIndex && num >newIndex){
                GeoMapLayerEntity oldLayer=layerEntityList.get(oldIndex);
                GeoMapLayerEntity newLayer=layerEntityList.get(newIndex);

                boolean hasChange=false;
                List<GeoMapLayerEntity> newLayerList=new ArrayList<>();
                if(oldIndex > newIndex){
                    hasChange=true;
                    // >0,1,2,3,4^,5,6,7,8
                    for(int i=0;i<num;i++){
                        if(i<newIndex){
                            newLayerList.add(layerEntityList.get(i));
                        }else if(i==newIndex){
                            newLayerList.add(oldLayer);
                            newLayerList.add(layerEntityList.get(i));
                        }else {
                            int index=i<oldIndex?i:(i+1);
                            if(index<num) {
                                newLayerList.add(layerEntityList.get(index));
                            }
                        }
                    }
                }else if(oldIndex<newIndex){
                    hasChange=true;
                    // 1,2,^3,4,5,6,7,>8
                    for(int i=0;i<num;i++){
                        if(i<oldIndex){
                            newLayerList.add(layerEntityList.get(i));
                        }else if(i==newIndex){
                            newLayerList.add(oldLayer);

                        }else {
                            if((i+1)<num) {
                                newLayerList.add(layerEntityList.get(i + 1));
                            }
                        }
                    }
                }
//                layerEntityList.add(newIndex,oldLayer);

                String[] names=new String[]{"layer_order"};
                if(hasChange) {
                    for(int i=0,k=newLayerList.size();i<k;i++) {
                        FeatureUtil.modifyFeatureById(mDataStore, TB_MAP_LAYER, names, new Object[]{i}, newLayerList.get(i).getId());
                    }
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public boolean displayMapLayer(String layerId, int display) {
        return FeatureUtil.modifyFeatureById(mDataStore,TB_MAP_LAYER,new String[]{"display"},new Object[]{display},layerId);
    }
}
