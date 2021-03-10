package cn.booktable.geo.test;

import cn.booktable.geo.core.MapInfo;
import cn.booktable.geo.provider.MapInfoProvider;
import cn.booktable.geo.core.PaintParam;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author ljc
 */
public class GeoMapServiceImplTest {

    private void saveImage(String file,BufferedImage image,String format){
        try {
            File fileToSave = new File(file);
            ImageIO.write(image, format, fileToSave);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    @Test
    public void reander(){
        MapInfo mapInfo= MapInfoProvider.instance().findMapInfo("T20210308-001");
        PaintParam param=new PaintParam();
        param.setArea("250,250");
//        param.setBbox("-180,-90,180,90");
        param.setBbox(mapInfo.getBbox());
        param.setMapId(mapInfo.getMapId());
        param.setFormat("png");
        GeoMapService mapService=new GeoMapServiceImpl();
        BufferedImage image= mapService.paint(param);
        saveImage("/workspace/temp/geo/test-out-1.png",image,param.getFormat());
    }

}
