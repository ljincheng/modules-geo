package cn.booktable.appadmin.controller.wms;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.PaintParam;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/wms/map")
public class WmsMapController {
    private static Logger logger= LoggerFactory.getLogger(WmsMapController.class);

    @Autowired
    private GeoMapManageService geoMapManageService;
    @Autowired
    private GeoMapService geoMapService;
    @Autowired
    private GeoCacheService geoCacheService;

    @RequestMapping("/project/{id}")
    public JsonView<WmsProjectMapInfoVO> project(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response){
        try {
            List<GeoMapInfoEntity> projectMap=null;
            String token=request.getHeader("token");
            if(StringUtils.isBlank(token) || !token.equals("test001")){
                return JsonView.error("身份验证失败");

            }
            response.setHeader("token",token);
            WmsProjectMapInfoVO mapInfo=new WmsProjectMapInfoVO();
            GeoMapInfoEntity mapInfoEntity =geoMapManageService.findBaseMapInfo(id);
            mapInfo.setMapInfo(mapInfoEntity);
            if(mapInfoEntity!=null){
                if(StringUtils.isNotBlank( mapInfoEntity.getProjectId())) {
                    projectMap=geoMapManageService.projectMapInfoList(mapInfoEntity.getProjectId());
                    mapInfo.setProjectMap(projectMap);
                }
            }
            if(projectMap==null){
                projectMap=new ArrayList<>();
                mapInfo.setProjectMap(projectMap);
            }
            return JsonView.ok(mapInfo);

        }catch (Exception ex){
            logger.error("获取地图异常",ex);
            return JsonView.error("获取数据异常");
        }
    }


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
            geoMapService.paint(param,os);
            os.flush();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * 重载
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/reload")
    public JsonView<String> reload(HttpServletRequest request, HttpServletResponse response,Boolean clearCache){
        try {
           geoMapService.reload(clearCache);
            return JsonView.ok("OK");
        }catch (Exception ex){
            logger.error("重载异常",ex);
            return JsonView.error("系统异常");
        }
    }

    @RequestMapping("/clearCache")
    public JsonView<String> clearCache(HttpServletRequest request, HttpServletResponse response){
        try {
            geoCacheService.clearAll();
            return JsonView.ok("OK");
        }catch (Exception ex){
          logger.error("清理缓存异常",ex);
          return JsonView.error("系统异常");
        }

    }

    @RequestMapping("/layerOrderChange/{mapId}")
    public JsonView<String> layerOrderChange(HttpServletRequest request, HttpServletResponse response, @PathVariable("mapId") String mapId, @RequestBody WmsLayerOrderChangeVO orderChangeVO){
        try {
           geoMapManageService.modifyMapLayerOrder(mapId,orderChangeVO.getOldIndex(),orderChangeVO.getNewIndex());
            return JsonView.ok("OK");
        }catch (Exception ex){
            logger.error("重载异常",ex);
            return JsonView.error("系统异常");
        }
    }
}
