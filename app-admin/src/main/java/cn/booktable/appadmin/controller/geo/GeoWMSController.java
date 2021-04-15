package cn.booktable.appadmin.controller.geo;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.GeoFeatureRequest;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoCacheServiceImpl;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
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
    GeoCacheService geoCacheService=new GeoCacheServiceImpl();

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

    /**
     * 坐标查询
     * @param request
     * @param response
     * @param mapId 地图ID
     * @param layerId 图层ID
     * @param p 坐标点
     * @return
     */
    @RequestMapping("/coordQuery/{mapId}")
    @ResponseBody
    public String coordQuery(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, String layerId, String p){
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
                return null;
            }
            OutputStream outputStream=response.getOutputStream();
            mGeoFeatureService.writeFeature(geoQuery,outputStream);
            outputStream.flush();

        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;
    }

    @RequestMapping("/addFeature/{mapId}")
//    @ResponseBody
    public JsonView<Boolean> addFeature(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, @RequestBody GeoFeature feature){
        JsonView<Boolean> result=new JsonView<>();
        try {
            boolean res= mGeoFeatureService.addFeature(feature);
            if(res){
                GeoQuery geoQuery=new GeoQuery();
                geoQuery.setFilter("BBOX(geom,"+GeoGeometryProvider.getBBoxString(feature.getGeometry())+")");
                geoCacheService.deleteCache(geoQuery);
                result.setCode(JsonView.CODE_SUCCESS);
                result.setData(res);
                return result;
            }
            result.setCode(JsonView.CODE_FAILE);
            result.setMsg("操作失败");
        }catch (Exception ex){
            ex.printStackTrace();
            result.setCode(JsonView.CODE_FAILE);
            result.setMsg(ex.getMessage());
        }
        return result;

    }

    @RequestMapping("/deleteFeature/{mapId}")
    public JsonView<Integer> deleteFeature(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, @RequestBody GeoQueryBo queryBo){
        JsonView<Integer> result=new JsonView<>();
        try {
            AssertUtils.isNotBlank(queryBo.getFeatureId(),"条件不能为空");
            AssertUtils.isNotBlank(queryBo.getLayerId(),"图层不能为空");
            GeoQuery query=new GeoQuery();
            query.setMapId(mapId);
            query.setLayerId(queryBo.getLayerId());
            query.setFilter(queryBo.getFilter());
            query.setFeatureId(queryBo.getFeatureId());
           List<GeoFeature> featureList= mGeoFeatureService.queryFeature(query);
            if(featureList!=null && featureList.size()>0){
                mGeoFeatureService.deleteFeature(query);
                for(GeoFeature feature:featureList) {
                    GeoQuery geoQuery = new GeoQuery();
                    geoQuery.setFilter("BBOX(geom," + GeoGeometryProvider.getBBoxString(feature.getGeometry()) + ")");
                    geoCacheService.deleteCache(geoQuery);
                }
                result.setCode(JsonView.CODE_SUCCESS);
                result.setData(featureList.size());
                return result;
            }
            result.setCode(JsonView.CODE_FAILE);
            result.setMsg("操作失败");
        }catch (Exception ex){
            ex.printStackTrace();
            result.setCode(JsonView.CODE_FAILE);
            result.setMsg(ex.getMessage());
        }
        return result;

    }
}
