package cn.booktable.geo.core;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author ljc
 */
public class MapInfo implements TableObject{
    private String mapId;
    private String title;
    private String bbox;
    private Date createTime;
    private Date updateTime;
    public static String[] COLUMNS=new String[]{"map_id","title","bbox","create_time","update_time"};
    public static String TABLENAME="gis_map_info";


    @Override
    public void restore(ResultSet res) {

        try {
            this.mapId = res.getString(1);
            this.title=res.getString(2);
            this.bbox=res.getString(3);
            this.createTime=res.getDate(4);
            this.updateTime=res.getDate(5);
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
            String sql = "INSERT INTO " + TABLENAME + "("+ StringUtils.join(COLUMNS,",")+")VALUES(?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.getMapId());
            ps.setString(2, this.getTitle());
            ps.setString(3, this.getBbox());
            ps.setDate(4,now);
            ps.setDate(5,now);
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
            String sql = "DELETE FROM "+TABLENAME+" WHERE map_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.mapId);
            return ps.execute();
        }catch (SQLException ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
