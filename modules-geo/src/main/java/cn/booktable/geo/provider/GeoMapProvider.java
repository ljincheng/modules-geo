package cn.booktable.geo.provider;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.impl.GeoCacheServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import org.geotools.data.DataStore;

/**
 * @author ljc
 */
public class GeoMapProvider {

    private static GeoMapProvider mInstance=null;
    private GeoMapManageService mGeoMapManageService;
    private DataStore mDataStore;
    private static GeoCacheService mGeoCacheService;

    public static GeoMapProvider instance(){
        assert Thread.holdsLock(GeoMapProvider.class);
        if(mInstance==null){
            mInstance=new GeoMapProvider();
            GeoMapManageService geoMapManageService=new GeoMapManageServiceImpl();
            mGeoCacheService=new GeoCacheServiceImpl();
            mInstance.mGeoMapManageService=geoMapManageService;
            mInstance.mDataStore= DBHelper.dataStore();
        }
        return mInstance;
    }

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
