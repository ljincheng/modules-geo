package cn.booktable.appadmin.controller.geo;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.*;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.provider.TileModelProvider;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoCacheServiceImpl;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import cn.booktable.geo.utils.GeoRequestUtils;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author ljc
 */

@Controller
@RequestMapping("/geo/map/")
public class GeoMapController {
    private static Logger logger= LoggerFactory.getLogger(GeoMapController.class);

    GeoEngine mGeoEngine=null;
    @Autowired
    DataSource dataSource;

    private GeoEngine getGeoEngine(){
        if(this.mGeoEngine==null){
            this.mGeoEngine=new GeoEngineImpl(dataSource);
        }
        return this.mGeoEngine;
    }


    /**
     * 重载
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/reload")
    public JsonView<String> reload(HttpServletRequest request, HttpServletResponse response,Boolean clearCache){
        JsonView<String> result=new JsonView<>();
        try {
            getGeoEngine().getGeoMapService().reload(clearCache);
            result.setCode(JsonView.CODE_SUCCESS);
            result.setMsg("OK");
        }catch (Exception ex){
            ex.printStackTrace();
            result.setCode(JsonView.CODE_FAILE);
            result.setMsg(ex.getMessage());
        }
        return result;
    }

    @RequestMapping("/clearCache")
    public JsonView<String> clearCache(HttpServletRequest request, HttpServletResponse response){
        JsonView<String> result=new JsonView<>();
        try {
            getGeoEngine().getGeoCacheService().clearAll();
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
     * 瓦片
     * @param request
     * @param response
     * @param z
     * @param x
     * @param y
     */
    @RequestMapping("/image/{mapId}/{z}/{x}/{y}.png")
    public void image(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId,@PathVariable("z") Integer z,@PathVariable("x") Integer x,@PathVariable("y") Integer y ){
        PaintParam param=new PaintParam();
        try {
                param.setFormat("png");
                param.setMapId(mapId);
                param.setZ(z);
                param.setX(x);
                param.setY(y);
            response.setContentType("image/png");
            final ServletOutputStream os = response.getOutputStream();
            getGeoEngine().getGeoMapService().paint(param,os);
            os.flush();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



}
