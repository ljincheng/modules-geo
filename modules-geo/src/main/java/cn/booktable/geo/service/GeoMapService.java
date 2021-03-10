package cn.booktable.geo.service;

import cn.booktable.geo.core.MapInfo;
import cn.booktable.geo.core.PaintParam;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author ljc
 */
public interface GeoMapService {

    List<MapInfo> mapInfos();

    void reload();

    BufferedImage paint(PaintParam param);

}
