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
import java.util.List;

/**
 * @author ljc
 */
@Data
public class GeoMapInfoEntity {
    private String mapId;
    private String title;
    private String bbox;
    private Integer zoom;
    private Integer minZoom;
    private Integer maxZoom;
    private String center;
    private String mapUrl;
    private String mapConfig;
    private Date createTime;
    private Date updateTime;

    private List<GeoMapLayerEntity> mapLayers;
}
