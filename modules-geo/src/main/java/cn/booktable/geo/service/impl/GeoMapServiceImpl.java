package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.*;
import cn.booktable.geo.entity.GeoImageCacheEntity;
import cn.booktable.geo.provider.GeoMapProvider;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author ljc
 */
public class GeoMapServiceImpl implements GeoMapService {

    private  GeoMapContent map=null;
    private GeoMapProvider mMapProvider=null;
    private boolean openCache=true;

   {
       mMapProvider=GeoMapProvider.instance();
        map=new GeoMapContent(mMapProvider);
    }

    @Override
    public void clearCache() {
        map.cleanCache();
    }


    @Override
    public void paint(PaintParam param, OutputStream output) {
       try {
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

           GeoImageCacheEntity imageCacheEntity=new GeoImageCacheEntity();
           imageCacheEntity.setImageId(param.getMapId());
           imageCacheEntity.setCacheId(param.getMapId()+"-"+param.getArea()+"-"+param.getBbox()+"-"+param.getFormat());
           GeoImageCacheEntity imageCache=mMapProvider.cacheService().findCache(imageCacheEntity.getCacheId());
           if(imageCache==null || imageCache.getImageData().length()==0) {
               ReferencedEnvelope mapBounds = new ReferencedEnvelope(minx, maxx, miny, maxy, DefaultGeographicCRS.WGS84);
               Rectangle imageBounds = new Rectangle(0, 0, w, h);

               map.addLayers(param.getMapId());
               GeoRenderingContext renderer = new GeoRenderingContext();
               renderer.setMapContent(map);
               BufferedImage image = renderer.paint(mapBounds, imageBounds, param.getFormat());
               ImageIO.write(image, param.getFormat(), output);
               imageCacheEntity.setImageData(imageToBase64(image,param.getFormat()));
               mMapProvider.cacheService().saveCache(imageCacheEntity);
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
