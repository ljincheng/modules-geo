package cn.booktable.geo.entity;

import lombok.Data;
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
    private Date createTime;
    private Date updateTime;
    private String projectId;
    private String subTitle;
    private Integer projectOrder;

    private List<GeoMapLayerEntity> mapLayers;
}
