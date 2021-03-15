package cn.booktable.geo.service;

import cn.booktable.geo.entity.GeoMapLayerEntity;

import java.util.List;

/**
 * @author ljc
 */
public interface GeoMapManageService {

   List<GeoMapLayerEntity> fullMapLayerListByMapId(String mapId);

}
