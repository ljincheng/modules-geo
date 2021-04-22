package cn.booktable.appadmin.controller.wms;

import cn.booktable.geo.entity.GeoMapInfoEntity;
import lombok.Data;

import java.util.List;

/**
 * @author ljc
 */
@Data
public class WmsProjectMapInfoVO {
    private GeoMapInfoEntity mapInfo;
    private List<GeoMapInfoEntity> projectMap;
}
