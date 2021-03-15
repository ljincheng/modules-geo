package cn.booktable.appadmin.controller.geo;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.PaintParam;
import cn.booktable.geo.provider.TileModelProvider;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import cn.booktable.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;

/**
 * @author ljc
 */

@Controller
@RequestMapping("/geo/map/")
public class GeoMapController {

    GeoMapService mapService = new GeoMapServiceImpl();

    /**
     * 重载
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/reload")
    public JsonView<String> reload(HttpServletRequest request, HttpServletResponse response){
        JsonView<String> result=new JsonView<>();
        try {
            mapService.clearCache();
            result.setCode(JsonView.CODE_SUCCESS);
            result.setMsg("OK");
        }catch (Exception ex){
            ex.printStackTrace();
            result.setCode(JsonView.CODE_FAILE);
            result.setMsg(ex.getMessage());
        }
        return result;
    }

    /**
     * WMS服务
     * @param request
     * @param response
     * @param param
     */
    @RequestMapping("/wms")
    public void wms(HttpServletRequest request, HttpServletResponse response, PaintParam param){
        try {
            if(StringUtils.isBlank(param.getFormat())){
                param.setFormat("png");
            }
            if(StringUtils.isBlank(param.getMapId())){
                param.setMapId("T20210308-001");
            }
            if(StringUtils.isBlank(param.getArea())){
                param.setArea("256,256");
            }
            if(StringUtils.isBlank(param.getBbox())){
                param.setBbox("-180.0,-89.99892578125002,180.0,83.599609375");
            }

            BufferedImage image = mapService.paint(param);
//            response.setContentType("image/"+param.getFormat());
            response.setContentType("image/png");
            final ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, param.getFormat(), os);
            os.flush();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 瓦片
     * @param request
     * @param response
     * @param z
     * @param x
     * @param y
     */
    @RequestMapping("/image/{z}/{x}/{y}")
    public void image(HttpServletRequest request, HttpServletResponse response,@PathVariable("z") Double z,@PathVariable("x") Double x,@PathVariable("y") Double y ){
        PaintParam param=new PaintParam();
        try {
                param.setFormat("png");
                param.setMapId("T20210308-001");
                param.setArea("256,256");
           String bbox= TileModelProvider.instance().bbox(z,x,y);
                param.setBbox(bbox);
            BufferedImage image = mapService.paint(param);
            response.setContentType("image/png");
            final ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, param.getFormat(), os);
            os.flush();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
