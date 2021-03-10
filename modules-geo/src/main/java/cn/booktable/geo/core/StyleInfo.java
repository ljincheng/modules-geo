package cn.booktable.geo.core;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.geotools.styling.Style;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author ljc
 */
public class StyleInfo implements TableObject{
    public static String DEFAULT_POINT = "point";
    public static String DEFAULT_LINE = "line";
    public static String DEFAULT_POLYGON = "polygon";
    public static String DEFAULT_RASTER = "raster";
    public static String DEFAULT_GENERIC = "generic";
    public static String[] COLUMNS=new String[]{"style_id","title","style_type","content","create_time","update_time"};
    public static String TABLENAME="gis_style_info";

    private String styleId;
    private String title;
    private String styleType;
    private String content;
    private Date createTime;
    private Date updateTime;


    @Override
    public void restore(ResultSet res) {
        try{
            this.styleId=res.getString(1);
            this.title=res.getString(2);
            this.styleType=res.getString(3);
            this.content=res.getString(4);
            this.createTime=res.getDate(5);
            this.updateTime=res.getDate(6);
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
            ps.setString(1, this.styleId);
            ps.setString(2, this.title);
            ps.setString(3, this.styleType);
            ps.setString(4, this.content);
            ps.setDate(5,now);
            ps.setDate(6,now);
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
            String sql = "DELETE FROM "+TABLENAME+" WHERE style_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, this.styleId);
            return ps.execute();
        }catch (SQLException ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
        }
    }

    public String getStyleId() {
        return styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStyleType() {
        return styleType;
    }

    public void setStyleType(String styleType) {
        this.styleType = styleType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
