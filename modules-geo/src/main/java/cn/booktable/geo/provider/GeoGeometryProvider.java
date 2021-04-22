package cn.booktable.geo.provider;

import cn.booktable.geo.core.GeoException;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author ljc
 */
public class GeoGeometryProvider {

    private static WKTReader wktParser = new WKTReader();
    private static GeometryJSON gjson = new GeometryJSON();

    public static Geometry parser(String geom)  {
        try {
            return wktParser.read(geom);
        }catch (Exception e){
            throw new GeoException(e);
        }
    }

    public static String getBBoxString(String geom)  {
        try {
            Geometry geometry=parserJsonFormat(geom);
            if(geometry==null){
                return null;
            }
            Envelope envelope=geometry.getEnvelopeInternal();
            return String.format("%.4f,%.4f,%.4f,%.4f",envelope.getMinX(),envelope.getMinY(),envelope.getMaxX(),envelope.getMaxY());
        }catch (Exception e){
            throw new GeoException(e);
        }
    }

    public static Geometry bboxParser(double minX,double minY,double maxX,double maxY){
        double minx=Math.min(minX,maxX);
        double maxx=Math.max(minX,maxX);
        double miny=Math.min(minY,maxY);
        double maxy=Math.max(minY,maxY);
        StringBuilder coords=new StringBuilder();
        coords.append("POLYGON((").append(minx).append(" ").append(miny).append(",");
        coords.append(minx).append(" ").append(maxy).append(",")
                .append(maxx).append(" ").append(maxy).append(",")
                .append(maxx).append(" ").append(miny).append(",")
                .append(minx).append(" ").append(miny).append("))");
       return parser(coords.toString());
    }

    public static Geometry parserJsonFormat(String json){
        try {
            if (StringUtils.isNotBlank(json)) {
                return gjson.read(new StringReader(json));
            }
        }catch (IOException e){
            throw new GeoException(e.fillInStackTrace());
        }

        return null;
    }

    public static String parserJsonString(Geometry geometry){
            if (geometry!=null) {
                return gjson.toString(geometry);
            }
        return null;
    }
}
