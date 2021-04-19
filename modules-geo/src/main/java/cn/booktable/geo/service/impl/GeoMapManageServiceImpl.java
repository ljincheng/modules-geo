package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import cn.booktable.geo.entity.GeoStyleInfoEntity;
import cn.booktable.geo.service.GeoMapManageService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ljc
 */
public class GeoMapManageServiceImpl implements GeoMapManageService {


    @Override
    public List<GeoMapLayerEntity> fullMapLayersByMapId(String mapId) {
        Connection conn= DBHelper.getConnection();
        List<GeoMapLayerEntity> mapInfoList=null;
        try{
//            mapInfoList=mapLayerListByMapId(conn,mapId);
            mapInfoList=mapLayerFullColumnListByMapId(conn,mapId,null);
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(conn);
        }
        return mapInfoList;
    }

    @Override
    public GeoMapInfoEntity findBaseMapInfo(String mapId) {
        Connection conn= DBHelper.getConnection();
        GeoMapInfoEntity mapInfoEntity=null;
        try{
            mapInfoEntity=findMapInfoEntity(conn,mapId);
        }catch (Exception ex){
            throw new GeoException(ex.fillInStackTrace());
        }finally {
            DBHelper.close(conn);
        }
        return mapInfoEntity;
    }

    @Override
    public List<GeoMapInfoEntity> projectMapInfoList(String projectId) {
        Connection conn= DBHelper.getConnection();
        List<GeoMapInfoEntity> list=null;
        try{
            list=projectMapInfoListByProjectId(conn,projectId);
        }catch (Exception ex){
            throw new GeoException(ex.fillInStackTrace());
        }finally {
            DBHelper.close(conn);
        }
        return list;
    }

    @Override
    public GeoMapLayerEntity queryMapLayersByLayerId(String mapId, String layerId) {
        Connection conn= DBHelper.getConnection();
        GeoMapLayerEntity mapLayerEntity=null;
        try{
         List<GeoMapLayerEntity>   mapInfoList=mapLayerFullColumnListByMapId(conn,mapId,layerId);
         if(mapInfoList!=null && mapInfoList.size()>0){
             mapLayerEntity=mapInfoList.get(0);
         }
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(conn);
        }
        return mapLayerEntity;
    }

    private GeoMapInfoEntity findMapInfoEntity(Connection conn, String mapId){
        GeoMapInfoEntity mapInfoEntity=null;
        String sql="SELECT map_id, title, bbox,zoom,min_zoom,max_zoom,center,map_url,map_config,project_id,sub_title,project_order FROM geo_map_info WHERE  map_id=?";
        PreparedStatement ps=null;
        try {
            ps = conn.prepareStatement(sql);
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
                mapInfoEntity.setMapUrl(res.getString(8));
                mapInfoEntity.setMapConfig(res.getString(9));
                mapInfoEntity.setProjectId(res.getString(10));
                mapInfoEntity.setSubTitle(res.getString(11));
                mapInfoEntity.setProjectOrder(res.getInt(12));
            }
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
        return mapInfoEntity;
    }

    private List<GeoMapInfoEntity> projectMapInfoListByProjectId(Connection conn,  String projectId){
        List<GeoMapInfoEntity> mapInfoList=new ArrayList<>();
        String sql="SELECT map_id, title, bbox,zoom,min_zoom,max_zoom,center,map_url,map_config,project_id,sub_title,project_order FROM geo_map_info WHERE  project_id=? order by  project_order desc";
        PreparedStatement ps=null;
        try {
            ps = conn.prepareStatement(sql);
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
                mapInfoEntity.setMapUrl(res.getString(8));
                mapInfoEntity.setMapConfig(res.getString(9));
                mapInfoEntity.setProjectId(res.getString(10));
                mapInfoEntity.setSubTitle(res.getString(11));
                mapInfoEntity.setProjectOrder(res.getInt(12));
                mapInfoList.add(mapInfoEntity);
            }
        }catch (Exception ex){
            throw new GeoException(ex.fillInStackTrace());
        }finally {
            DBHelper.close(ps);
        }
        return mapInfoList;
    }

    private List<GeoMapLayerEntity> mapLayerFullColumnListByMapId(Connection conn, String mapId,String layerSource) {
        List<GeoMapLayerEntity> mapInfoList=new ArrayList<GeoMapLayerEntity>();
        String sql="SELECT t2.id, t2.map_id,  t2.layer_order, t2.display, t2.style_id,t2.layer_source,t2.layer_type,t2.layer_filter,t2.envelope,t2.title \n" +
//                ",t1.title as l_i_title, t1.layer_name, t1.layer_type, t1.envelope, t1.layer_filter, t1.create_time as l_i_ctime, t1.update_time as l_i_utime\n" +
                ",t3.title as s_i_title, t3.style_type, t3.content \n" +
                "from geo_map_layer t2 \n" +
//                "left join geo_layer_info t1 on  t1.layer_id=t2.layer_id\n" +
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
//                obj.setLayerId(res.getString(3));
                obj.setLayerOrder(res.getInt(3));
                obj.setDisplay(res.getInt(4));
                obj.setStyleId(res.getString(5));
                obj.setLayerSource(res.getString(6));
                obj.setLayerType(res.getString(7));
                obj.setLayerFilter(res.getString(8));
                obj.setEnvelope(res.getString(9));
                obj.setTitle(res.getString(10));
//                GeoLayerInfoEntity layerInfo=new GeoLayerInfoEntity();
//                layerInfo.setLayerId(obj.getLayerId());
//                layerInfo.setTitle(res.getString(7));
//                layerInfo.setLayerName(res.getString(8));
//                layerInfo.setLayerType(res.getString(9));
//                layerInfo.setEnvelope(res.getString(10));
//                layerInfo.setLayerFilter(res.getString(11));
//                layerInfo.setCreateTime(res.getDate(12));
//                layerInfo.setUpdateTime(res.getDate(13));
//                obj.setLayerInfoEntity(layerInfo);
                GeoStyleInfoEntity styleInfo=new GeoStyleInfoEntity();
                styleInfo.setStyleId(obj.getStyleId());
                styleInfo.setTitle(res.getString(11));
                styleInfo.setStyleType(res.getString(12));
                styleInfo.setContent(res.getString(13));
//                styleInfo.setCreateTime(res.getDate(17));
//                styleInfo.setUpdateTime(res.getDate(18));
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


    private List<GeoMapLayerEntity> mapLayerListByMapId(Connection conn,String mapId,String layerSource) {
        List<GeoMapLayerEntity> mapInfoList=new ArrayList<GeoMapLayerEntity>();
        String sql="SELECT id, map_id,  layer_order, display, style_id,layer_source,layer_type,layer_filter,envelope,title  from geo_map_layer where map_id=?";
        PreparedStatement ps=null;
        try {
            String orderBy=" order by t2.map_id desc, t2.layer_order asc";
            if(layerSource==null || layerSource.length()==0) {
                sql=sql+orderBy;
                ps = conn.prepareStatement(sql);
                ps.setString(1, mapId);
            }else{
                sql=sql+" and  t1.layer_source=? "+orderBy;
                ps = conn.prepareStatement(sql);
                ps.setString(1, mapId);
                ps.setString(2, layerSource);
            }
            if (ps.execute()) {
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
                    mapInfoList.add(obj);
                }
            }
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
        return mapInfoList;
    }


    private List<GeoStyleInfoEntity> styleInfoListByMapId(Connection conn, String mapId) {
        List<GeoStyleInfoEntity> styleInfoList=new ArrayList<GeoStyleInfoEntity>();
        String sql="SELECT t3.style_id, t3.title, t3.style_type, t3.content, t3.create_time, t3.update_time\n" +
                "FROM geo_style_info t3\n" +
                "left join geo_map_layer t2 on t3.style_id=t2.style_id\n" +
                "WHERE t2.map_id=?";
        PreparedStatement ps=null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, mapId);
            if (ps.execute()) {
                ResultSet res = ps.executeQuery();
                while (res.next()) {
                    GeoStyleInfoEntity obj = new GeoStyleInfoEntity();
                    obj.setStyleId(res.getString(1));
                    obj.setTitle(res.getString(2));
                    obj.setStyleType(res.getString(3));
                    obj.setContent(res.getString(4));
                    obj.setCreateTime(res.getDate(5));
                    obj.setUpdateTime(res.getDate(6));
                    styleInfoList.add(obj);
                }
            }
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
        return styleInfoList;
    }


}
