package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.*;
import cn.booktable.geo.entity.GeoImageCacheEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.provider.TileModelProvider;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoMapService;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
public class GeoMapServiceImpl implements GeoMapService {

    private GeoCacheService geoCacheService=null;
    public static Map<String,GeoMapContent> mMapContentMap=new HashMap<>();
    private boolean openCache=true;
    private JDBCDataStore mDataStore=null;
    private GeoEngine mGeoEngine=null;


    public GeoMapServiceImpl(GeoEngine geoEngine){
        mGeoEngine=geoEngine;
        mDataStore=geoEngine.getDataStore();
        geoCacheService=new GeoCacheServiceImpl(mDataStore);
    }

    @Override
    public void reload(Boolean clearCacheImage) {
//        GeoQuery query=new GeoQuery();
        for(String mapId: mMapContentMap.keySet()){
            GeoMapContent map=mMapContentMap.get(mapId);
            map.cleanCache();
            if(clearCacheImage!=null && clearCacheImage){
//                query.setFilter("map_id='"+mapId+"'");
                geoCacheService.deleteCacheByMapId(mapId);
            }
        }
    }


    private GeoMapContent getMapContentByMapId(String mapId){
        GeoMapContent map= mMapContentMap.get(mapId);
        if(map==null){
            map=new GeoMapContent(mGeoEngine);
            mMapContentMap.put(mapId,map);
        }
        return map;
    }
    @Override
    public void paint(PaintParam param, OutputStream output) {
       try {
           //坐标范围
           String bbox= TileModelProvider.instance().bbox(param.getZ(),param.getX(),param.getY());
           param.setBbox(bbox);
           String[] split = bbox.split(",");
           double minx = Double.valueOf(split[0]);
           double miny = Double.valueOf(split[1]);
           double maxx = Double.valueOf(split[2]);
           double maxy = Double.valueOf(split[3]);
           //图片大小
           int[] tileSize=TileModelProvider.instance().getTileSize();
           int w = tileSize[0];
           int h = tileSize[1];

           GeoImageCacheEntity imageCacheEntity=new GeoImageCacheEntity();
           imageCacheEntity.setMapId(param.getMapId());
           imageCacheEntity.setCacheId(param.getMapId()+"-"+param.getZ()+"-"+param.getX()+"-"+param.getY()+"."+param.getFormat());
           GeoImageCacheEntity imageCache=geoCacheService.findCache(imageCacheEntity.getCacheId());
           if(imageCache==null || imageCache.getImageData().length()==0) {
               ReferencedEnvelope mapBounds = new ReferencedEnvelope(minx, maxx, miny, maxy, DefaultGeographicCRS.WGS84);
               Rectangle imageBounds = new Rectangle(0, 0, w, h);

               GeoMapContent map= getMapContentByMapId(param.getMapId());
               map.addLayers(param.getMapId());
               GeoRenderingContext renderer = new GeoRenderingContext();
               renderer.setMapContent(map);
               BufferedImage image = renderer.paint(mapBounds, imageBounds, param.getFormat());
               ImageIO.write(image, param.getFormat(), output);
               imageCacheEntity.setImageData(imageToBase64(image,param.getFormat()));
               imageCacheEntity.setGeom(GeoGeometryProvider.bboxParser(minx,miny,maxx,maxy));
               geoCacheService.saveCache(imageCacheEntity);
           }else{
               byte[] bytes1 =Base64.getDecoder().decode(imageCache.getImageData());
               ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
               BufferedImage image = ImageIO.read(bais);
               ImageIO.write(image, param.getFormat(), output);
           }
       }catch (IOException ex){
           throw new GeoException(ex);
       }
    }

//    private String imageToBase64(BufferedImage image,String format)throws IOException{
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageIO.write(image, format, baos);
//       return  Base64.getEncoder().encodeToString(baos.toByteArray());
//    }

    private   String imageToBase64(final BufferedImage img, final String formatName)throws IOException
    {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(img, formatName, os);
         return Base64.getEncoder().encodeToString(os.toByteArray());

    }

}
