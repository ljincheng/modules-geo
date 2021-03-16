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
     * 清理缓存
     */
    void clearCache();

    /**
     * 地图输出
     * @param param
     * @param output
     */
    void paint(PaintParam param, OutputStream output);

}
