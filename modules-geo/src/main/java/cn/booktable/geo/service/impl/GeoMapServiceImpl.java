package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.*;
import cn.booktable.geo.provider.GeoMapProvider;
import cn.booktable.geo.service.GeoMapService;
import java.awt.image.BufferedImage;

/**
 * @author ljc
 */
public class GeoMapServiceImpl implements GeoMapService {

    private  GeoMapContent map=null;
    private GeoMapProvider mGeoMapProvider=null;

   {
        mGeoMapProvider=GeoMapProvider.instance();
        map=new GeoMapContent(mGeoMapProvider);
    }

    @Override
    public void clearCache() {
        map.cleanCache();
    }


    @Override
    public BufferedImage paint(PaintParam param) {
        map.addLayers(param.getMapId());
        GeoRenderer renderer=new GeoRenderer();
        renderer.setMapContent(map);
        return renderer.paint(param);
    }

}
