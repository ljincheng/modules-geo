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
 * 图层信息
 * @author ljc
 */
@Data
public class GeoLayerInfoEntity {
    private String layerId;//唯一标识
    private String title;//图层描述
    private String layerName;//图层名称
    private String layerType;//图层类型
    private String envelope;//区域
    private String layerFilter;//图层过滤
    private Date createTime;//创建时间
    private Date updateTime;//更新时间
}
