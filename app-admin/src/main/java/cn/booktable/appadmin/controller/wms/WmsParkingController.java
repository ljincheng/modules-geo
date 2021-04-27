package cn.booktable.appadmin.controller.wms;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.List;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/wms/parking")
public class WmsParkingController {

    private static Logger logger= LoggerFactory.getLogger(WmsParkingController.class);
    private static final String TB_PARKING_POLYGON="geo_parking_polygon";
    @Autowired
    private GeoFeatureService geoFeatureService;
    @Autowired
    private GeoCacheService geoCacheService;
    /**
     * 坐标查询
     * @param request
     * @param response
     * @param mapId 地图ID
     * @param p 坐标点
     * @return
     */
    @RequestMapping("/query/{mapId}")
    @ResponseBody
    public String coordQuery(HttpServletRequest request, HttpServletResponse response, @PathVariable("mapId") String mapId, String p){
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
            geoFeatureService.writeFeatureByMapLayerSource(geoQuery,outputStream);
            outputStream.flush();

        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;
    }

    @RequestMapping("/add/{mapId}")
    public JsonView<Boolean> addFeature(HttpServletRequest request, HttpServletResponse response, @PathVariable("mapId") String mapId, @RequestBody GeoFeature feature){
        JsonView<Boolean> result=new JsonView<>();
        try {
            feature.setMapId(mapId);
            feature.setLayerSource(TB_PARKING_POLYGON);
            feature.getProperties().put("map_id",mapId);
            boolean res= geoFeatureService.addFeature(feature);
            if(res){
//                GeoQuery geoQuery=new GeoQuery();
//                geoQuery.setFilter("BBOX(geom,"+ GeoGeometryProvider.getBBoxString(feature.getGeometry())+")");
                geoCacheService.deleteCache("BBOX(geom,"+ GeoGeometryProvider.getBBoxString(feature.getGeometry())+")");
                return JsonView.ok("OK",res);
            }
            return JsonView.error("操作失败");
        }catch (Exception ex){
            logger.error("删除失败",ex);
            return JsonView.error("系统异常");
        }


    }

    @RequestMapping("/delete/{mapId}")
    public JsonView<Integer> deleteFeature(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId, @RequestBody GeoQueryVO queryBo){

        try {
            AssertUtils.isNotBlank(queryBo.getFeatureId(),"条件不能为空");
//            AssertUtils.isNotBlank(queryBo.getLayerSource(),"图层不能为空");
            GeoQuery query=new GeoQuery();
            query.setMapId(mapId);
            query.setLayerSource(TB_PARKING_POLYGON);
            query.setFilter("mapId='"+mapId+"'");
            query.setFeatureId(queryBo.getFeatureId());
            List<GeoFeature> featureList=geoFeatureService.queryFeature(query);
            if(featureList!=null && featureList.size()>0){
                geoFeatureService.deleteFeature(query);
                for(GeoFeature feature:featureList) {
//                    GeoQuery geoQuery = new GeoQuery();
//                    geoQuery.setFilter("BBOX(geom," + GeoGeometryProvider.getBBoxString(feature.getGeometry()) + ")");
                    geoCacheService.deleteCache("BBOX(geom," + GeoGeometryProvider.getBBoxString(feature.getGeometry()) + ")");
                }
                return JsonView.ok(featureList.size());

            }
            return JsonView.error("操作失败");
        }catch (Exception ex){
           logger.error("删除失败",ex);
            return JsonView.error("系统异常");
        }

    }
}
