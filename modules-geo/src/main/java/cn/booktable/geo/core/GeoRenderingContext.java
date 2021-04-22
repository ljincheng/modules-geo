package cn.booktable.geo.core;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author ljc
 */
public final class GeoRenderingContext extends StreamingRenderer implements GTRenderer {


    public void setMapContent(GeoMapContent mapContent){
        super.setMapContent(mapContent);
    }

    public BufferedImage paint(ReferencedEnvelope mapBounds,Rectangle imageBounds,String format) {
//        map.getViewport().setScreenArea(imageBounds);
//        map.getViewport().setBounds(mapBounds);

        BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D gr = image.createGraphics();
        if ("png".equals(format)) {
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
