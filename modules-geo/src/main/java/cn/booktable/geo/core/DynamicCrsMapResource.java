package cn.booktable.geo.core;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author ljc
 */
public class DynamicCrsMapResource {

    private static final long serialVersionUID = 1L;

    private final CoordinateReferenceSystem crs;

    public DynamicCrsMapResource(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public ByteArrayOutputStream resImage(String bbox,int width,int height,String formatName){
        ByteArrayOutputStream output = null;
        if (bbox != null) {

            try {
                CRSAreaMapBuilder builder =new CRSAreaMapBuilder(width, height);
                Envelope envelope = parseEnvelope(bbox);
                RenderedImage image = builder.createMapFor(crs, envelope);
                output = new ByteArrayOutputStream();
                ImageIO.write(image, formatName, output);
            } catch (Exception e) {
                output = null;
                e.printStackTrace();
            }
        }
        return output;
    }

    public Envelope parseEnvelope(String bboxStr) {
        String[] split = bboxStr.split(",");
        double minx = Double.valueOf(split[0]);
        double miny = Double.valueOf(split[1]);
        double maxx = Double.valueOf(split[2]);
        double maxy = Double.valueOf(split[3]);
        return new Envelope(minx, maxx, miny, maxy);
    }

}