package cn.booktable.geo.service;

import cn.booktable.geo.entity.GeoImageCacheEntity;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.entity.GeoMapLayerEntity;

import java.util.List;

/**
 * @author ljc
 */
public interface GeoMapManageService {

   List<GeoMapLayerEntity> fullMapLayersByMapId(String mapId);

   GeoMapInfoEntity findBaseMapInfo(String mapId);

   List<GeoMapInfoEntity> projectMapInfoList(String projectId);

   GeoMapLayerEntity queryMapLayersByLayerId(String mapId,String layerId);

   List<GeoMapLayerEntity> queryMapLayerByLayerSource(String mapId,String layerSource);

   /**
    * 创建项目地图
    * @param mapInfoEntity
    * @return
    */
   boolean createProjectMap(GeoMapInfoEntity mapInfoEntity);
   boolean modifyMapInfo(GeoMapInfoEntity mapInfoEntity);
   boolean deleteMapInfoByMapId(String mapId);
   boolean deleteMapInfoByProject(String projectId);
   boolean addMapLayer(GeoMapLayerEntity mapLayerEntity);
   boolean deleteMapLayer(String layerId);
   boolean modifyMapLayerOrder(String mapId,int oldIndex,int newIndex);

   boolean displayMapLayer(String layerId,int display);

}
