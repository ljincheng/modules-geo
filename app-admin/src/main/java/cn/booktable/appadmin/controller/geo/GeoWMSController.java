package cn.booktable.appadmin.controller.geo;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.GeoFeatureRequest;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/geo/wms/")
public class GeoWMSController {

    private static Logger logger= LoggerFactory.getLogger(GeoWMSController.class);

    GeoMapService mapService = new GeoMapServiceImpl();
    GeoMapManageService geoMapManageService = new GeoMapManageServiceImpl();
    GeoFeatureService mGeoFeatureService=new GeoFeatureServiceImpl();

    /**
     * 获取地图
     * @param id 地图ID
     * @return
     */
    @RequestMapping("/map/{id}")
    public JsonView<GeoMapInfoEntity> mapInfo(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response){
        JsonView<GeoMapInfoEntity> view=new JsonView<>();
        try {

            String token=request.getHeader("token");
            if(StringUtils.isBlank(token) || !token.equals("test001")){
                view.setMsg("身份验证失败");
                view.setCode(JsonView.CODE_FAILE);
                return view;
            }
            response.setHeader("token",token);
            GeoMapInfoEntity mapInfoEntity = geoMapManageService.findBaseMapInfo(id);
            if(mapInfoEntity!=null){
                view.setData(mapInfoEntity);
                view.setCode(JsonView.CODE_SUCCESS);
            }else{
                view.setMsg("找不到数据");
                view.setCode(JsonView.CODE_FAILE);
            }
        }catch (Exception ex){
            logger.error("获取地图异常",ex);
            view.setMsg("获取数据异常");
            view.setCode(JsonView.CODE_FAILE);
        }
        return view;
    }

    @RequestMapping("/pointQuery/{mapId}")
    @ResponseBody
    public String pointQuery(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, String layerId, String type, String p){
        try {
            AssertUtils.isNotBlank(p,"坐标不能为空");
            AssertUtils.isNotBlank(layerId,"图层不能为空");
            AssertUtils.isTrue(p.indexOf(",")>0,"坐标格式不正确");
            String[] split = p.split(",");
            int pointNum=split.length;
            AssertUtils.isTrue(pointNum%2 == 0,"坐标格式不正确");
            GeoQuery geoQuery=new GeoQuery();
            geoQuery.setLayerId(layerId);
            geoQuery.setMapId(mapId);
            if(split.length==2) {
                geoQuery.setFilter("CONTAINS(geom,POINT(" + p.replace(",", " ") + "))");
            }else if(split.length==4){
                geoQuery.setFilter("BBOX(geom," + p+ ")");
            }else{
                StringBuilder coords=new StringBuilder();
                int i=0;
                while (i < pointNum) {
                    if(i==0){
                        coords.append(split[i]).append(" ").append(split[i + 1]);
                    }else {
                        coords.append(",").append(split[i]).append(" ").append(split[i + 1]);
                    }
                    i = i + 2;
                }
                if(!(split[0].equals(split[pointNum-2]) && split[1].equals(split[pointNum-1]))){
                    coords.append(",").append(split[0]).append(" ").append(split[1]);
                }
                geoQuery.setFilter("CONTAINS(geom,POLYGON((" + p+ ")))");
            }
            AssertUtils.isNotBlank(geoQuery.getLayerId(),"图层ID不能为空");
            OutputStream outputStream=response.getOutputStream();
            mGeoFeatureService.writeFeature(geoQuery,outputStream);
            outputStream.flush();

        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;
    }

    @RequestMapping("/rectQuery/{mapId}")
    @ResponseBody
    public String rectQuery(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, String layerId, String type, String p){
        try {
            AssertUtils.isNotBlank(p,"坐标不能为空");
            AssertUtils.isNotBlank(layerId,"图层不能为空");
            AssertUtils.isTrue(p.indexOf(",")>0,"坐标格式不正确");
            GeoQuery geoQuery=new GeoQuery();
            geoQuery.setLayerId(layerId);
            geoQuery.setMapId(mapId);
            geoQuery.setFilter("CONTAINS(geom,POINT("+p.replace(","," ")+"))");
            AssertUtils.isNotBlank(geoQuery.getLayerId(),"图层ID不能为空");
            OutputStream outputStream=response.getOutputStream();
            mGeoFeatureService.writeFeature(geoQuery,outputStream);
            outputStream.flush();

        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;
    }
}
