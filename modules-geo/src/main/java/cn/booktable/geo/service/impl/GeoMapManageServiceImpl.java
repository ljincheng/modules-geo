package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import cn.booktable.geo.entity.GeoStyleInfoEntity;
import cn.booktable.geo.service.GeoMapManageService;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ljc
 */
public class GeoMapManageServiceImpl implements GeoMapManageService {
    private JDBCDataStore mDataStore;
    public GeoMapManageServiceImpl(JDBCDataStore dataStore){
        mDataStore=dataStore;
    }

    @Override
    public List<GeoMapLayerEntity> fullMapLayersByMapId(String mapId) {
        List<GeoMapLayerEntity> mapInfoList=null;
        Transaction tran =null;
        Connection conn=null;
        try  {
            tran = new DefaultTransaction();
            conn= mDataStore.getConnection(tran);
            mapInfoList=mapLayerFullColumnListByMapId(conn,mapId,null);
            tran.commit();
        }catch (Exception ex){
            DBHelper.rollback(tran);
            throw new GeoException(ex);
        }finally {
            DBHelper.close(conn);
            DBHelper.close(tran);
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
        try (Transaction tran=new DefaultTransaction(); Connection conn=mDataStore.getConnection(tran)) {
             List<GeoMapLayerEntity>   mapInfoList=mapLayerFullColumnListByMapId(conn,mapId,layerId);
             tran.commit();
             if(mapInfoList!=null && mapInfoList.size()>0){
                 mapLayerEntity=mapInfoList.get(0);
             }

        }catch (Exception ex){
            throw new GeoException(ex);
        }
        return mapLayerEntity;
    }

    @Override
    public boolean createProjectMap(GeoMapInfoEntity mapInfoEntity) {
        boolean result=false;
        try (Transaction tran=new DefaultTransaction(); Connection conn=mDataStore.getConnection(tran)){
            result= this.createProjectMap(conn,mapInfoEntity);
            tran.commit();
        }catch (Exception ex){
            throw new GeoException(ex);
        }
        return result;
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
        try (PreparedStatement ps=conn.prepareStatement(sql)){
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
        }
        return styleInfoList;
    }


    public boolean createProjectMap(Connection conn,GeoMapInfoEntity mapInfo) {
        boolean result=false;
        String sql1="INSERT INTO geo_map_info (map_id, project_id, project_order, title, sub_title, bbox, zoom, min_zoom, max_zoom, center, create_time, update_time)\n" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        String sql2="INSERT INTO geo_map_layer (id, map_id, layer_order, display, style_id, layer_source, layer_type, layer_filter, title, envelope)\n" +
                "VALUES(?, ?, ?, ?,?, ?, ?, ?, ?, ?);";
        PreparedStatement ps=null;
        try {
            ps = conn.prepareStatement(sql1);
            java.sql.Date now=new java.sql.Date(new Date().getTime());
            ps.setString(1, mapInfo.getMapId());
            ps.setString(2, mapInfo.getProjectId());
            ps.setInt(3, mapInfo.getProjectOrder());
            ps.setString(4, mapInfo.getTitle());
            ps.setString(5, mapInfo.getSubTitle());
            ps.setString(6,mapInfo.getBbox());
            ps.setInt(7,mapInfo.getZoom());
            ps.setInt(8,mapInfo.getMinZoom());
            ps.setInt(9,mapInfo.getMaxZoom());
            ps.setString(10,mapInfo.getCenter());
            ps.setDate(11, now);
            ps.setDate(12, now);
            if (ps.execute()) {

                if(mapInfo.getMapLayers()!=null && mapInfo.getMapLayers().size()>0){

                    for(GeoMapLayerEntity mapLayer: mapInfo.getMapLayers()) {

                        ps = conn.prepareStatement(sql2);
                        ps.setString(1,mapLayer.getId());
                        ps.setString(2, mapInfo.getMapId());
                        ps.setInt(3, mapLayer.getLayerOrder());
                        ps.setInt(4, mapLayer.getDisplay());
                        ps.setString(5, mapLayer.getStyleId());
                        ps.setString(6, mapLayer.getLayerSource());
                        ps.setString(7, mapLayer.getLayerType());
                        ps.setString(8, mapLayer.getLayerFilter());
                        ps.setString(9, mapLayer.getTitle());
                        ps.setString(10, mapLayer.getEnvelope());
                        if (!ps.execute()) {
                            throw new GeoException("保存图层失败");
                        }
                    }
                }
                result=true;
            }
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
        return result;
    }

}
