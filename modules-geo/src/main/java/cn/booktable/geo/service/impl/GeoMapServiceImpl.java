package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.*;
import cn.booktable.geo.provider.FeatureSourceProvider;
import cn.booktable.geo.provider.MapInfoProvider;
import cn.booktable.geo.service.GeoMapService;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author ljc
 */
public class GeoMapServiceImpl implements GeoMapService {



    @Override
    public List<MapInfo> mapInfos() {
        return MapInfoProvider.instance().mapInfoList();
    }

    private MapContent getMapContent(String mapId){

       List<Layer> layers= FeatureSourceProvider.instance().getLayers(mapId);
       if(layers!=null && layers.size()>0){
           MapContent mapContent=new MapContent();
           mapContent.addLayers(layers);
           return mapContent;
       }
       return null;

    }

    @Override
    public void reload() {
        FeatureSourceProvider.reload();
    }

    @Override
    public BufferedImage paint(PaintParam param) {
        GTRenderer renderer = new StreamingRenderer();
        MapContent map=getMapContent(param.getMapId());
        if(map==null){
            throw new GeoException("地图不存在");
        }
        renderer.setMapContent(map);

        //坐标范围
        String[] split = param.getBbox().split(",");
        double minx = Double.valueOf(split[0]);
        double miny = Double.valueOf(split[1]);
        double maxx = Double.valueOf(split[2]);
        double maxy = Double.valueOf(split[3]);

        //图片大小
        String[] wh=param.getArea().split(",");
        int w=Integer.valueOf(wh[0]);
        int h=Integer.valueOf(wh[1]);

        ReferencedEnvelope mapBounds =new ReferencedEnvelope(minx,maxx,miny,maxy, null);
//        ReferencedEnvelope mapBounds =map.getMaxBounds();
        System.out.println(String.format("\r\n==========println=============\r\n map-bbox=%s",mapBounds.toString()));
        Rectangle imageBounds =  new Rectangle(0, 0, w, h);

//        map.getViewport().setScreenArea(imageBounds);
//        map.getViewport().setBounds(mapBounds);
        BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = image.createGraphics();
        if("png".equals(param.getFormat())) {
            gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.01f));
            // set background color for assertion
            gr.setPaint(Color.WHITE);
            gr.fill(imageBounds);
            gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }else {
            gr.setPaint(Color.WHITE);
            gr.fill(imageBounds);
        }

        gr.setColor(Color.RED);
        renderer.paint(gr, imageBounds, mapBounds);
        return image;
    }
}
