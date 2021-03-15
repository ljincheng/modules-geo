package cn.booktable.geo.core;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author ljc
 */
public final class GeoRenderer extends StreamingRenderer implements GTRenderer {


    public void setMapContent(GeoMapContent mapContent){
        super.setMapContent(mapContent);
    }

    public BufferedImage paint(PaintParam param) {
        //坐标范围
        String[] split = param.getBbox().split(",");
        double minx = Double.valueOf(split[0]);
        double miny = Double.valueOf(split[1]);
        double maxx = Double.valueOf(split[2]);
        double maxy = Double.valueOf(split[3]);

        //图片大小
        String[] wh = param.getArea().split(",");
        int w = Integer.valueOf(wh[0]);
        int h = Integer.valueOf(wh[1]);

        ReferencedEnvelope mapBounds = new ReferencedEnvelope(minx, maxx, miny, maxy, DefaultGeographicCRS.WGS84);
       Rectangle imageBounds = new Rectangle(0, 0, w, h);

//        map.getViewport().setScreenArea(imageBounds);
//        map.getViewport().setBounds(mapBounds);

        BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D gr = image.createGraphics();
        if ("png".equals(param.getFormat())) {
            gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.01f));
            gr.setPaint(Color.WHITE);
            gr.fill(imageBounds);
            gr.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        } else {
            gr.setPaint(Color.WHITE);
            gr.fill(imageBounds);
        }

        gr.setColor(Color.RED);
        paint(gr, imageBounds, mapBounds);
        return image;
    }
}
