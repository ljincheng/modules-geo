package cn.booktable.geo.test;

import cn.booktable.geo.core.DBHelper;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.temporal.TEquals;

/**
 * @author ljc
 */
public class DataFilterTest {

    //点上的面数据。
    //@Test
    public void testContainsFilter() throws Exception {

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // should match only "r2"
        GeometryFactory gf = new GeometryFactory();
//        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
//        Point point= gf.createPoint(new Coordinate(-138.33984375, 50.888671875));
//        Point point= gf.createPoint(new Coordinate(-130.25390625 ,13.798828125));
        Point point= gf.createPoint(new Coordinate(-85.25390625, 46.494140625));
//        LinearRing shell =
//                gf.createLinearRing(sf.create(new double[] {2, -1, 2, 5, 4, 5, 4, -1, 2, -1}, 2));
//        Polygon polygon = gf.createPolygon(shell, null);
//        Contains cs = ff.contains(ff.property("geom"),ff.literal(point));

        TEquals cs2 =ff.tequals(ff.property("building_id"),ff.literal("b001"));
        Query query=new Query();
        query.setFilter(cs2);
//        query.setFilter(ff.equal(ff.property("building_id"),ff.literal("b001")));
        FeatureCollection features =  DBHelper.dataStore().getFeatureSource("gis_parking_polygon").getFeatures(query);
        checkSingleResult(features, "parking_no");
    }

    protected void checkSingleResult(FeatureCollection features, String name) {
        if(features.size()>0) {
            try (FeatureIterator fr = features.features()) {
                while (fr.hasNext()) {
                    SimpleFeature f = (SimpleFeature) fr.next();
                    System.out.println("#####:parking_no="+f.getAttribute(name));
//                    assertNotNull(f);
//                    assertEquals(name, f.getAttribute(aname("name")));
//                    assertFalse(fr.hasNext());
                }
            }
        }
    }
}
