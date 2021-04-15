package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.QueryGenerator;
import cn.booktable.geo.entity.GeoImageCacheEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.service.GeoCacheService;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author ljc
 */
public class GeoCacheServiceImpl implements GeoCacheService {
    private static final String TYPENAME_IMAGECACHE="geo_image_cache";

    private DataStore mDataStore;
    {
        mDataStore= DBHelper.dataStore();
    }
//    @Override
//    public boolean saveCache(GeoImageCacheEntity imageCache) {
//
//        Connection conn= DBHelper.getConnection();
//        PreparedStatement ps=null;
//        try{
//            if(imageCache.getZ()!=null && imageCache.getX()!=null && imageCache.getY()!=null) {
//                String sql = "INSERT INTO geo_image_cache ( cache_id, image_id, z, x, y, image_data ) VALUES " +
//                        "(?,?,?,?,?,?)";
//                ps = conn.prepareStatement(sql);
//                ps.setString(1, imageCache.getCacheId());
//                ps.setString(2, imageCache.getImageId());
//                ps.setInt(3, imageCache.getZ());
//                ps.setInt(4, imageCache.getX());
//                ps.setInt(5, imageCache.getY());
//                ps.setString(6, imageCache.getImageData());
//            }else{
//                String sql = "INSERT INTO geo_image_cache ( cache_id, image_id, image_data ) VALUES " +
//                        "(?,?,?)";
//                ps = conn.prepareStatement(sql);
//                ps.setString(1, imageCache.getCacheId());
//                ps.setString(2, imageCache.getImageId());
//                ps.setString(3, imageCache.getImageData());
//            }
//            return ps.execute();
//        }catch (Exception ex){
//            throw new GeoException(ex);
//        }finally {
//            DBHelper.close(ps);
//            DBHelper.close(conn);
//        }
//    }

    @Override
    public boolean saveCache(GeoImageCacheEntity cacheEntity){
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(TYPENAME_IMAGECACHE);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            Map<String, Object> atts = new HashMap<>();
            atts.put("cache_id",cacheEntity.getCacheId());
            atts.put("image_id",cacheEntity.getImageId());
            atts.put("image_data",cacheEntity.getImageData());
            atts.put("geom",cacheEntity.getGeom());
            SimpleFeatureType schema = featureSource.getSchema();
            Object[] attributes = new Object[schema.getAttributeCount()];
            for (int i = 0; i < attributes.length; i++) {
                AttributeDescriptor descriptor = schema.getDescriptor(i);
                attributes[i] =atts.get(descriptor.getLocalName());
            }
            SimpleFeature feature = SimpleFeatureBuilder.build(schema, attributes, cacheEntity.getCacheId());
            feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
            feature.getUserData().put(Hints.PROVIDED_FID,  cacheEntity.getCacheId());
            SimpleFeatureCollection collection = DataUtilities.collection(feature);
            featureSource.addFeatures(collection);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    public boolean deleteCache(GeoQuery query){
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(TYPENAME_IMAGECACHE);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
//            query.setLayerName(TYPENAME_IMAGECACHE);
            Query readQuery=new Query();// QueryGenerator.toQuery(query);
            readQuery.setFilter(QueryGenerator.toFilter(query.getFilter()));
            SimpleFeatureType schema = featureSource.getSchema();
            featureSource.removeFeatures(readQuery.getFilter());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    @Override
    public GeoImageCacheEntity findCache(String cacheId) {
        Connection conn= DBHelper.getConnection();
        PreparedStatement ps=null;
        try{
            String sql="select cache_id, image_id, z, x, y, image_data  from geo_image_cache where cache_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, cacheId);
            ResultSet res = ps.executeQuery();
            if(res.next()){
                GeoImageCacheEntity imageCache=new GeoImageCacheEntity();
                imageCache.setCacheId(res.getString(1));
                imageCache.setImageId(res.getString(2));
                imageCache.setZ(res.getInt(3));
                imageCache.setX(res.getInt(4));
                imageCache.setY(res.getInt(5));
                imageCache.setImageData(res.getString(6));
                return imageCache;
            }

        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
            DBHelper.close(conn);
        }
        return null;
    }
}
