package cn.booktable.appadmin.controller.geo;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.GeoEngine;
import cn.booktable.geo.core.GeoEngineImpl;
import cn.booktable.geo.core.GeoFeature;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/geo/wms/")
public class GeoWMSController {

    private static Logger logger= LoggerFactory.getLogger(GeoWMSController.class);
    private static final String TB_PARKING_POLYGON="geo_parking_polygon";

//    GeoMapService mapService = new GeoMapServiceImpl();
//    GeoMapManageService geoMapManageService = new GeoMapManageServiceImpl();
//    GeoFeatureService mGeoFeatureService=new GeoFeatureServiceImpl();
//    GeoCacheService geoCacheService=new GeoCacheServiceImpl();

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
     * 获取地图
     * @param id 地图ID
     * @return
     */
    @RequestMapping("/map/{id}")
    public JsonView<GeoProjectMapInfoBo> mapInfo(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response){
        JsonView<GeoProjectMapInfoBo> view=new JsonView<>();
        try {
            List<GeoMapInfoEntity> projectMap=null;
            String token=request.getHeader("token");
            if(StringUtils.isBlank(token) || !token.equals("test001")){
                view.setMsg("身份验证失败");
                view.setCode(JsonView.CODE_FAILE);
                return view;
            }
            response.setHeader("token",token);
            GeoProjectMapInfoBo mapInfo=new GeoProjectMapInfoBo();
            GeoMapInfoEntity mapInfoEntity =getGeoEngine().getGeoMapManageService().findBaseMapInfo(id);
            mapInfo.setMapInfo(mapInfoEntity);
            if(mapInfoEntity!=null){
                if(StringUtils.isNotBlank( mapInfoEntity.getProjectId())) {
                  projectMap= getGeoEngine().getGeoMapManageService().projectMapInfoList(mapInfoEntity.getProjectId());
                  mapInfo.setProjectMap(projectMap);
                }
            }
            if(projectMap==null){
                projectMap=new ArrayList<>();
                mapInfo.setProjectMap(projectMap);
            }
            view.setData(mapInfo);
            view.setCode(JsonView.CODE_SUCCESS);
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
    @RequestMapping("/queryParking/{mapId}")
    @ResponseBody
    public String coordQuery(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId,  String p){
        try {
            AssertUtils.isNotBlank(p,"坐标不能为空");
//            AssertUtils.isNotBlank(layerSource,"图层不能为空");
            AssertUtils.isTrue(p.indexOf(",")>0,"坐标格式不正确");
            String[] split = p.split(",");
            int pointNum=split.length;
            AssertUtils.isTrue(pointNum%2 == 0,"坐标格式不正确");
            GeoQuery geoQuery=new GeoQuery();
            geoQuery.setLayerSource(TB_PARKING_POLYGON);
            geoQuery.setMapId(mapId);
            if(split.length==2) {
                geoQuery.setFilter("CONTAINS(geom,POINT(" + p.replace(",", " ") + "))");
            }else if(split.length==4){
                geoQuery.setFilter("BBOX(geom," + p+ ")");
            }else{
                return null;
            }
            OutputStream outputStream=response.getOutputStream();
            getGeoEngine().getGeoFeatureService().writeFeature(geoQuery,outputStream);
            outputStream.flush();

        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;
    }

    @RequestMapping("/addParking/{mapId}")
    public JsonView<Boolean> addFeature(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, @RequestBody GeoFeature feature){
        JsonView<Boolean> result=new JsonView<>();
        try {
            feature.setMapId(mapId);
            feature.setLayerSource(TB_PARKING_POLYGON);
            feature.getProperties().put("map_id",mapId);
            boolean res= getGeoEngine().getGeoFeatureService().addFeature(feature);
            if(res){
                GeoQuery geoQuery=new GeoQuery();
                geoQuery.setFilter("BBOX(geom,"+GeoGeometryProvider.getBBoxString(feature.getGeometry())+")");
                getGeoEngine().getGeoCacheService().deleteCache(geoQuery);
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

    @RequestMapping("/deleteParking/{mapId}")
    public JsonView<Integer> deleteFeature(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, @RequestBody GeoQueryBo queryBo){
        JsonView<Integer> result=new JsonView<>();
        try {
            AssertUtils.isNotBlank(queryBo.getFeatureId(),"条件不能为空");
//            AssertUtils.isNotBlank(queryBo.getLayerSource(),"图层不能为空");
            GeoQuery query=new GeoQuery();
            query.setMapId(mapId);
            query.setLayerSource(TB_PARKING_POLYGON);
            query.setFilter(queryBo.getFilter());
            query.setFeatureId(queryBo.getFeatureId());
            GeoFeatureService geoFeatureService=getGeoEngine().getGeoFeatureService();
            GeoCacheService geoCacheService=getGeoEngine().getGeoCacheService();
           List<GeoFeature> featureList=geoFeatureService.queryFeature(query);
            if(featureList!=null && featureList.size()>0){
                geoFeatureService.deleteFeature(query);
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

    @RequestMapping("/addForm/{mapId}")
    @ResponseBody
    public String addForm(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId){
         StringBuilder html=new StringBuilder();
        try {
            html.append("<input type=\"hidden\" name=\"map_id\" value='").append(mapId).append("' />")
                    .append("<input type=\"text\" name=\"parking_no\" placeholder=\"车位编号\">")
                    .append("<input type=\"text\" name=\"building_id\" placeholder=\"楼栋ID\">")
            .append("<select  name=\"sale_status\"><option value=''>请选择</option><option value='1'>已售</option><option value='2'>未售</option></select>");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return html.toString();

    }
}
