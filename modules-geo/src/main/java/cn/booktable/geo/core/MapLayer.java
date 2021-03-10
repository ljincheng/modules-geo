package cn.booktable.geo.core;

import io.swagger.models.auth.In;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 地图图层
 * @author ljc
 */
public class MapLayer implements TableObject{
    private String id;
    private String mapId;
    private String layerId;
    private Integer layerOrder;
    private Integer display;
    private String styleId;
    public static String[] COLUMNS=new String[]{"id","map_id","layer_id","layer_order","display","style_id"};
    public static String TABLENAME="gis_map_layer";

    @Override
    public void restore(ResultSet res) {
        try{
            this.id=res.getString(1);
            this.mapId=res.getString(2);
            this.layerId=res.getString(3);
            this.layerOrder=res.getInt(4);
            this.display=res.getInt(5);
            this.styleId=res.getString(6);
        }catch (SQLException ex){
            throw new GeoException(ex);
        }
    }

    @Override
    public boolean insert(Connection conn) {
        PreparedStatement ps=null;
        try {
            long nowTime=new Date().getTime();
            java.sql.Date now=new java.sql.Date(nowTime);
            String sql = "INSERT INTO " + TABLENAME + "("+ StringUtils.join(COLUMNS,",")+")VALUES(?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.id);
            ps.setString(2, this.mapId);
            ps.setString(3, this.layerId);
            ps.setInt(4, this.layerOrder);
            ps.setInt(5, this.display);
            ps.setString(6, this.styleId);
            return  ps.execute();
        }catch (SQLException ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
    }

    @Override
    public boolean delete(Connection conn) {
        PreparedStatement ps=null;
        try {
            long nowTime=new Date().getTime();
            java.sql.Date now=new java.sql.Date(nowTime);
            String sql = "DELETE FROM "+TABLENAME+" WHERE id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.id);
            return ps.execute();
        }catch (SQLException ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public Integer getLayerOrder() {
        return layerOrder;
    }

    public void setLayerOrder(Integer layerOrder) {
        this.layerOrder = layerOrder;
    }

    public Integer getDisplay() {
        return display;
    }

    public void setDisplay(Integer display) {
        this.display = display;
    }

    public String getStyleId() {
        return styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }
}
