package cn.booktable.geo.service;

import cn.booktable.geo.core.PaintParam;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

/**
 * 地图服务
 * @author ljc
 */
public interface GeoMapService {


    /**
     * 重载缓存
     */
    void reload(Boolean clearCacheImage);

    /**
     * 地图输出
     * @param param
     * @param output
     */
    void paint(PaintParam param, OutputStream output);

}
