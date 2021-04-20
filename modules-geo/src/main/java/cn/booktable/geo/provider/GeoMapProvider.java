package cn.booktable.geo.provider;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoCacheServiceImpl;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import org.geotools.data.DataStore;

import javax.sql.DataSource;

/**
 * @author ljc
 */
public class GeoMapProvider {

    private static GeoMapProvider mInstance=null;
    private GeoMapManageService mGeoMapManageService;
    private static GeoMapService mGeoMapService;
    private static GeoFeatureService mGeoFeatureService;
    private static GeoCacheService mGeoCacheService;
    private DataStore mDataStore;

//    public static GeoMapProvider instance(DataSource dataSource){
//        assert Thread.holdsLock(GeoMapProvider.class);
//        if(mInstance==null){
////            DBHelper.connect(dataSource);
//            mInstance=new GeoMapProvider();
//            GeoMapManageService geoMapManageService=new GeoMapManageServiceImpl();
//            mGeoMapService=new GeoMapServiceImpl();
//            mGeoFeatureService=new GeoFeatureServiceImpl();
//            mGeoCacheService=new GeoCacheServiceImpl();
//            mInstance.mGeoMapManageService=geoMapManageService;
//            mInstance.mDataStore= DBHelper.dataStore();
//        }
//        return mInstance;
//    }

    public GeoMapProvider(){}
    public GeoMapProvider(GeoMapManageService service){
        mGeoMapManageService=service;
    }

    public GeoMapManageService mapManageService(){
        return  mGeoMapManageService;
    }

    public GeoCacheService cacheService(){
        return mGeoCacheService;
    }

    public DataStore getDataStore(){
        return mDataStore;
    }

}
