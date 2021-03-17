package cn.booktable.geo.provider;

import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.GeoFeature;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

/**
 * @author ljc
 */
public class GeoGeometryProvider {

    private static WKTReader wktParser = new WKTReader();

    public static Geometry parser(String geom)  {
        try {
            return wktParser.read(geom);
        }catch (Exception e){
            throw new GeoException(e);
        }
    }

    public static Geometry getGeometry(GeoFeature feature){
        Object geom= feature.getGeometry();
        if(geom!=null){
            if(geom instanceof String){
                return parser((String)geom);
            }else {
                return (Geometry)geom;
            }
        }
        return null;
    }
}
