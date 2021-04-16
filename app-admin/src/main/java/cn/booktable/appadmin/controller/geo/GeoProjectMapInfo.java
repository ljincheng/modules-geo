package cn.booktable.appadmin.controller.geo;

import cn.booktable.geo.entity.GeoMapInfoEntity;
import lombok.Data;

import java.util.List;

/**
 * @author ljc
 */
@Data
public class GeoProjectMapInfo {
    private GeoMapInfoEntity mapInfo;
    private List<GeoMapInfoEntity> projectMap;

}
