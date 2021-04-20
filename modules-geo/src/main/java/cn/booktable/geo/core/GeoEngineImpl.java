package cn.booktable.geo.core;

import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import cn.booktable.geo.service.impl.GeoCacheServiceImpl;
import cn.booktable.geo.service.impl.GeoFeatureServiceImpl;
import cn.booktable.geo.service.impl.GeoMapManageServiceImpl;
import cn.booktable.geo.service.impl.GeoMapServiceImpl;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.mysql.MySQLDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
public class GeoEngineImpl implements GeoEngine{
    private static Map<String, Object> mDataStoreParams=new HashMap<>();
    private JDBCDataStore mDataStore;
    private GeoMapManageService mGeoMapManageService;
    private GeoMapService mGeoMapService;
    private GeoFeatureService mGeoFeatureService;
    private GeoCacheService mGeoCacheService;

    public GeoEngineImpl(DataSource dataSource){
        initDataStore(dataSource);
        mGeoFeatureService=new GeoFeatureServiceImpl(mDataStore);
        mGeoCacheService=new GeoCacheServiceImpl(mDataStore);
        mGeoMapManageService=new GeoMapManageServiceImpl(mDataStore);
        mGeoMapService=new GeoMapServiceImpl(this);
    }

    private  void initDataStore(DataSource dataSource){
        try{
            mDataStoreParams.put(JDBCDataStoreFactory.DATASOURCE.key, dataSource);
            mDataStoreParams.put(JDBCDataStoreFactory.BATCH_INSERT_SIZE.key, 2000);
            mDataStore = (JDBCDataStore) DataStoreFinder.getDataStore(mDataStoreParams);
            if (mDataStore == null) {
                JDBCDataStoreFactory factory = new MySQLDataStoreFactory();
                mDataStore = factory.createDataStore(mDataStoreParams);
            }
        } catch (Exception e) {
            throw new GeoException(e.fillInStackTrace());
        }
    }

    @Override
    public JDBCDataStore getDataStore() {
        return this.mDataStore;
    }

    @Override
    public GeoMapService getGeoMapService() {
        return this.mGeoMapService;
    }

    @Override
    public GeoFeatureService getGeoFeatureService() {
        return this.mGeoFeatureService;
    }

    @Override
    public GeoCacheService getGeoCacheService() {
        return this.mGeoCacheService;
    }

    @Override
    public GeoMapManageService getGeoMapManageService() {
        return this.mGeoMapManageService;
    }
}
