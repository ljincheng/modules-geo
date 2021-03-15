package cn.booktable.geo.entity;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.TableObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author ljc
 */
@Data
public class GeoStyleInfoEntity {
    public static String DEFAULT_POINT = "point";
    public static String DEFAULT_LINE = "line";
    public static String DEFAULT_POLYGON = "polygon";
    public static String DEFAULT_RASTER = "raster";
    public static String DEFAULT_GENERIC = "generic";

    private String styleId;
    private String title;
    private String styleType;
    private String content;
    private Date createTime;
    private Date updateTime;

}
