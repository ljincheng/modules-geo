package cn.booktable.geo.core;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 图层信息
 * @author ljc
 */
public class LayerInfo implements TableObject{
    private String layerId;
    private String title;//描述表达图层。
    private String layerName;//图层名称
    private String layerType;//类型
    private String bbox;//坐标范围
    private Date createTime;
    private Date updateTime;

    public static String[] COLUMNS=new String[]{"layer_id","title","layer_name","layer_type","bbox","create_time","update_time"};
    public static String TABLENAME="gis_layer_info";

    @Override
    public void restore(ResultSet res) {
        try {
            this.layerId = res.getString(1);
            this.title=res.getString(2);
            this.layerName=res.getString(3);
            this.layerType=res.getString(4);
            this.bbox=res.getString(5);
            this.createTime=res.getDate(6);
            this.updateTime=res.getDate(7);
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
            String sql = "INSERT INTO " + TABLENAME + "("+ StringUtils.join(COLUMNS,",")+")VALUES(?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.getLayerId());
            ps.setString(2, this.getTitle());
            ps.setString(3, this.getLayerName());
            ps.setString(4, this.getLayerType());
            ps.setString(5, this.getBbox());
            ps.setDate(6,now);
            ps.setDate(7,now);
            return ps.execute();
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
            String sql = "DELETE FROM "+TABLENAME+" WHERE layer_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.layerId);
            return ps.execute();
        }catch (SQLException ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerType() {
        return layerType;
    }

    public void setLayerType(String layerType) {
        this.layerType = layerType;
    }

    public String getBbox() {
        return bbox;
    }

    public void setBbox(String bbox) {
        this.bbox = bbox;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
