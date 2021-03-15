package cn.booktable.geo.service;

import cn.booktable.geo.core.PaintParam;

import java.awt.image.BufferedImage;

/**
 * @author ljc
 */
public interface GeoMapService {


    void clearCache();

    BufferedImage paint(PaintParam param);

}
