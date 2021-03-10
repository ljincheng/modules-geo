package cn.booktable.geo.service;

import cn.booktable.geo.core.LayerInfo;
import cn.booktable.geo.core.MapInfo;
import cn.booktable.geo.core.MapLayer;
import cn.booktable.geo.core.StyleInfo;

import java.util.List;

/**
 * @author ljc
 */
public interface GeoDataService {

    List<MapInfo> mapInfoList();
    List<LayerInfo> layerInfoList();
    List<StyleInfo> styleInfoList();
    List<MapLayer> mapLayerList();

    boolean addMapInfo(MapInfo mapInfo);
    boolean addLayerInfo(LayerInfo layerInfo);
    boolean addStyleInfo(StyleInfo styleInfo);
    boolean addMapLayer(MapLayer mapLayer);

    boolean deleteMapInfo(MapInfo mapInfo);
    boolean deleteLayerInfo(LayerInfo layerInfo);
    boolean deleteStyleInfo(StyleInfo styleInfo);
    boolean deleteMapLayer(MapLayer mapLayer);

}
