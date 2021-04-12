package cn.booktable.appadmin.controller.geo;

import cn.booktable.core.view.JsonView;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.GeoFeatureRequest;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.PaintParam;
import cn.booktable.geo.entity.GeoMapInfoEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.provider.TileModelProvider;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import cn.booktable.geo.utils.GeoRequestUtils;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    GeoMapService mapService = new GeoMapServiceImpl();
    GeoMapManageService geoMapManageService = new GeoMapManageServiceImpl();
    GeoFeatureService mGeoFeatureService=new GeoFeatureServiceImpl();


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
     */
    @RequestMapping("/fs")
    @ResponseBody
    public JsonView<List<GeoFeature>> fs(HttpServletRequest request, HttpServletResponse response, String layerName, String type, String geometry, Map<String,Object> properties,String filter){
        JsonView<List<GeoFeature>> result=new JsonView<>();
        try {
            GeoFeatureRequest freq=new GeoFeatureRequest();
            freq.setType(type);
            freq.setLayerName(layerName);
            GeoFeature feature=new GeoFeature();
            feature.setGeometry(geometry);
            if(properties!=null){
                feature.setProperties(properties);
            }
            freq.setFeature(feature);
            GeoQuery geoQuery=new GeoQuery();
            freq.setQuery(geoQuery);
            AssertUtils.isNotBlank(freq.getType(),"操作类型为能不空");
            if(GeoRequestUtils.TYPE_ADD.equals(freq.getType())) {
                AssertUtils.notNull(freq.getFeature(),"图形不能为空");
                AssertUtils.isNotBlank(freq.getLayerName(),"图层不能为空");
               boolean res= mGeoFeatureService.addFeature(freq.getLayerName(),freq.getFeature());
               if(res) {
                   result.setCode(JsonView.CODE_SUCCESS);
                   result.setMsg("OK");
               }else {
                   result.setCode(JsonView.CODE_FAILE);
                   result.setMsg("失败");
               }
            }else if(GeoRequestUtils.TYPE_UPDATE.equals(freq.getType())){
                AssertUtils.notNull(freq.getFeature(),"图形不能为空");
                AssertUtils.notNull(freq.getQuery(),"查询条件不能为空");
                AssertUtils.isNotBlank(freq.getLayerName(),"图层不能为空");
                freq.getQuery().setLayerName(freq.getLayerName());
                boolean res= mGeoFeatureService.updateFeature(freq.getQuery(),freq.getFeature());
                if(res) {
                    result.setCode(JsonView.CODE_SUCCESS);
                    result.setMsg("OK");
                }else {
                    result.setCode(JsonView.CODE_FAILE);
                    result.setMsg("失败");
                }
            }else if(GeoRequestUtils.TYPE_DELETE.equals(freq.getType())){
                AssertUtils.notNull(freq.getQuery(),"查询条件不能为空");
                AssertUtils.isNotBlank(freq.getLayerName(),"图层不能为空");
                freq.getQuery().setLayerName(freq.getLayerName());
                boolean res=mGeoFeatureService.deleteFeature(freq.getQuery());
                if(res) {
                    result.setCode(JsonView.CODE_SUCCESS);
                    result.setMsg("OK");
                }else {
                    result.setCode(JsonView.CODE_FAILE);
                    result.setMsg("失败");
                }
            }else if(GeoRequestUtils.TYPE_QUERY.equals(freq.getType())){
                AssertUtils.notNull(freq.getQuery(),"查询条件不能为空");
                AssertUtils.isNotBlank(freq.getLayerName(),"图层不能为空");
                freq.getQuery().setLayerName(freq.getLayerName());
                freq.getQuery().setFilter(filter);
//                mGeoFeatureService.writeFeature(freq.getQuery(),response.getOutputStream());
                List<GeoFeature> res= mGeoFeatureService.queryFeature(freq.getQuery());
                    result.setCode(JsonView.CODE_SUCCESS);
                    result.setMsg("OK");
                    result.setData(res);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    @RequestMapping("/query")
    @ResponseBody
    public String query(HttpServletRequest request, HttpServletResponse response, String layerName, String type, String geometry, Map<String,Object> properties,String filter){
        try {
            GeoFeatureRequest freq=new GeoFeatureRequest();
            freq.setType(type);
            freq.setLayerName(layerName);
            GeoFeature feature=new GeoFeature();
            feature.setGeometry(geometry);
            if(properties!=null){
                feature.setProperties(properties);
            }
            freq.setFeature(feature);
            GeoQuery geoQuery=new GeoQuery();
            freq.setQuery(geoQuery);
            AssertUtils.isNotBlank(freq.getType(),"操作类型为能不空");

            AssertUtils.notNull(freq.getQuery(),"查询条件不能为空");
            AssertUtils.isNotBlank(freq.getLayerName(),"图层不能为空");
            freq.getQuery().setLayerName(freq.getLayerName());
            freq.getQuery().setFilter(filter);
            OutputStream outputStream=response.getOutputStream();
            mGeoFeatureService.writeFeature(freq.getQuery(),outputStream);
            outputStream.flush();

        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
        }
        return null;

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
    public void image(HttpServletRequest request, HttpServletResponse response,@PathVariable("mapId") String mapId,@PathVariable("z") Double z,@PathVariable("x") Double x,@PathVariable("y") Double y ){
        PaintParam param=new PaintParam();
        try {
                param.setFormat("png");
                param.setMapId(mapId);
//                param.setMapId("T20210308-001");
                param.setArea("256,256");
           String bbox= TileModelProvider.instance().bbox(z,x,y);
                param.setBbox(bbox);
            response.setContentType("image/png");
            final ServletOutputStream os = response.getOutputStream();
            mapService.paint(param,os);
            os.flush();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



}
