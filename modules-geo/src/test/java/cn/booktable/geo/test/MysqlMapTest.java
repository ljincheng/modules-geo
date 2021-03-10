package cn.booktable.geo.test;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
public class MysqlMapTest {

    public static void saveImage(final MapContent map, final String file, final int imageWidth) {

        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);

        Rectangle imageBounds = null;
        ReferencedEnvelope mapBounds = null;
        try {
            mapBounds = map.getMaxBounds();
            double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
            imageBounds = new Rectangle(
                    0, 0, imageWidth, (int) Math.round(imageWidth * heightToWidth));

        } catch (Exception e) {
            // failed to access map layers
            throw new RuntimeException(e);
        }

        BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_RGB);

        Graphics2D gr = image.createGraphics();
        gr.setPaint(Color.WHITE);
        gr.fill(imageBounds);

        try {
            renderer.paint(gr, imageBounds, mapBounds);
            File fileToSave = new File(file);
            ImageIO.write(image, "jpeg", fileToSave);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws Exception {
        // postgisExample start
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "mysql");
        params.put("host", "localhost");
        params.put("port", 3306);
//        params.put("schema", "public");
        params.put("database", "geotools");
        params.put("user", "root");
        params.put("passwd", "");

        DataStore dataStore= DataStoreFinder.getDataStore(params);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource("ne_50m_admin_0_countries");
        MapContent map = new MapContent();
        map.setTitle("Quickstart");

//        Style style = SLD.createSimpleStyle(featureSource.getSchema());

        Style style= SLD.createPolygonStyle(Color.BLUE,Color.RED,1);
//        SimpleFeatureIterator iterator= featureSource.getFeatures().features();
//        while (iterator.hasNext()){
//            SimpleFeature simpleFeature= iterator.next();
//            simpleFeature.getType().
//        }
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        String imgPath="/workspace/temp/geo/test-out-3.jpeg";
        MysqlMapTest.saveImage(map,imgPath,256);
//        try{
//            double[] bbox = new double[]{};
//            double x1 = bbox[0], y1 = bbox[1],
//                    x2 = bbox[2], y2 = bbox[3];
//            int width = 256,
//                    height=256;
//            String imgPath="/workspace/temp/geo/test-out.png";
//
//            // 设置输出范围
//            CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
//            ReferencedEnvelope mapArea = new ReferencedEnvelope(x1, x2, y1, y2, crs);
//            // 初始化渲染器
//            StreamingRenderer sr = new StreamingRenderer();
//            sr.setMapContent(map);
//            // 初始化输出图像
//            BufferedImage bi = new BufferedImage(width, height,
//                    BufferedImage.TYPE_INT_ARGB);
//            Graphics g = bi.getGraphics();
//            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                    RenderingHints.VALUE_ANTIALIAS_ON);
//            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//            Rectangle rect = new Rectangle(0, 0, width, height);
//            // 绘制地图
//            sr.paint((Graphics2D) g, rect, mapArea);
//            //将BufferedImage变量写入文件中。
//            ImageIO.write(bi,"png",new File(imgPath));
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
        System.out.println("ok");
        // postgisExample end
    }
}
