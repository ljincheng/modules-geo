package cn.booktable.geo.core;

import cn.booktable.geo.entity.GeoLayerInfoEntity;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import cn.booktable.geo.entity.GeoStyleInfoEntity;
import cn.booktable.geo.provider.GeoMapProvider;
import org.apache.commons.lang3.StringUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.Envelope2D;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.styling.SLDParser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author ljc
 */
public final class GeoMapContent  extends MapContent {

    private GeoMapProvider mGeoMapProvider;
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    private static Map<String,List<Layer>> mMapLayerMap=new HashMap<>();
    private static long mCacheTime=0l;
    private static long CACHETIME_MAX=1000l * 60*10;

    public GeoMapContent(GeoMapProvider geoDaoProvider){
        this.mGeoMapProvider=geoDaoProvider;
    }

    public void cleanCache(){
        synchronized (GeoMapContent.class) {
            mMapLayerMap.clear();
        }
    }
    public int addLayers(String mapId){
        synchronized (GeoMapContent.class) {
            int result = 0;
            long now = System.currentTimeMillis();
            List<Layer> layers = null;
            boolean readCache = (now - mCacheTime < CACHETIME_MAX);
            mCacheTime = now;
            if (readCache) {
                layers = mMapLayerMap.get(mapId);

            }
            if (layers == null || layers.size() == 0) {
                layers = getLayers(mapId);
                if (layers != null && layers.size() > 0) {
                    mMapLayerMap.put(mapId, layers);
                }
            }
            if (layers != null && layers.size() > 0) {
                result = this.addLayers(layers);
            }
            return result;
        }
    }


    private List<GeoMapLayerEntity> mapLayerListByMapId(String mapId){
        List<GeoMapLayerEntity> mapLayers=mGeoMapProvider.getGeoMapManageService().fullMapLayerListByMapId(mapId);
        return mapLayers;
    }

    private List<Layer> getLayers(String mapId){


           List<Layer> layerList = new ArrayList<>();
            List<GeoMapLayerEntity> mapLayers = mapLayerListByMapId(mapId);
            if (mapLayers == null) {
                return layerList;
            }
            //排序
            Collections.sort(mapLayers, new Comparator<GeoMapLayerEntity>() {
                @Override
                public int compare(GeoMapLayerEntity u1, GeoMapLayerEntity u2) {
                    int u1V = u1.getLayerOrder() == null ? 0 : u1.getLayerOrder().intValue();
                    int u2V = u2.getLayerOrder() == null ? 0 : u2.getLayerOrder().intValue();
                    int diff = u1V - u2V;
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    }
                    return 0;
                }
            });
            for (GeoMapLayerEntity mapLayer : mapLayers) {
                Layer layer=toLayer(mapLayer);
                if(layer!=null){
                    layerList.add(layer);
                }
            }

            return layerList;

    }

    private Layer toLayer(GeoMapLayerEntity mapLayer) {
        Layer layer = null;
        try {
            GeoLayerInfoEntity layerInfo = mapLayer.getLayerInfoEntity();
            GeoStyleInfoEntity styleInfo = mapLayer.getStyleInfoEntity();
            if (layerInfo != null) {
                StyleType layerType = StyleGenerator.getStyleType(layerInfo.getLayerType());
                Style style = null;
                if (layerType != null && layerType.compareTo(StyleType.RASTER) == 0) {
                    style = styleInfo == null || StringUtils.isBlank(styleInfo.getStyleType()) ? StyleGenerator.instance().rasterStyle() : getStyle(styleInfo);
                    final GridCoverage2D coverage = readCoverage(layerInfo);
                    GridCoverageLayer gridLayer = new GridCoverageLayer(coverage, style);
                    layer = gridLayer;
                } else if (styleInfo != null) {
                    FeatureSource fs =mGeoMapProvider.getDataStore().getFeatureSource(layerInfo.getLayerName());
                    style = getStyle(styleInfo);
                    if (fs != null && style != null) {
                        FeatureLayer featureLayer = new FeatureLayer(fs, style);
                        if (StringUtils.isNotBlank(layerInfo.getLayerFilter())) {
                            Query query = new Query();
                            try {
                                query.setFilter(CQL.toFilter(layerInfo.getLayerFilter()));
                            } catch (Exception ex) {
                                throw new GeoException(ex);
                            }
                            featureLayer.setQuery(query);
                        }
                        layer = featureLayer;
                    }

                }

                if (layer != null && (mapLayer.getDisplay() == null || mapLayer.getDisplay().intValue() == 0)) {

                    layer.setVisible(false);
                }
            }
        }catch (IOException ex){
            throw new GeoException(ex);
        }
        return layer;

    }

    private Style getStyle(GeoStyleInfoEntity styleInfo){
        Style style= null;
        int type=0;
        if(StringUtils.isNotBlank(styleInfo.getContent())) {
            int cursor=0;
            int size=styleInfo.getContent().length();
            while (cursor<size){
                char c=styleInfo.getContent().charAt(cursor);
                if(' ' == c){
                    cursor++;
                }else if('{' ==c){
                    type=1;
                    break;
                }else  if('<' == c){
                    type=2;
                    break;
                }else {
                    break;
                }
            }

            if(type==2) {
                SLDParser parser = new SLDParser(styleFactory, new ByteArrayInputStream(styleInfo.getContent().getBytes()));
                Style[] styles = parser.readXML();
                if (styles.length > 0) {
                    return styles[0];
                }
            }else if(type==1) {
                StyleGenerator styleGenerator=StyleGenerator.instance();
                StyleType styleType=StyleGenerator.getStyleType(styleInfo.getStyleType());
                style=styleGenerator.toStyle(styleInfo.getContent(),styleType);
            }
        }else {
            StyleGenerator styleGenerator=StyleGenerator.instance();
            StyleType styleType=StyleGenerator.getStyleType(styleInfo.getStyleType());
            style=styleGenerator.defaultStyle(styleType);
        }
        return style;
    }

    private  GridCoverage2D readCoverage(GeoLayerInfoEntity layerInfo) {
        try {
            String[] split = layerInfo.getEnvelope().split(",");
            double minx = Double.valueOf(split[0]);
            double miny = Double.valueOf(split[1]);
            double with = Double.valueOf(split[2]);
            double height = Double.valueOf(split[3]);
            BufferedImage bi = ImageIO.read(new File(layerInfo.getLayerName()));
            GridCoverageFactory factory = new GridCoverageFactory();
            CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
            Envelope2D envelope =  new Envelope2D(crs, minx, miny, with, height);
            return factory.create(layerInfo.getLayerId(), bi, envelope);
        }catch (Exception ex){
            throw new GeoException(ex);
        }
    }


}
