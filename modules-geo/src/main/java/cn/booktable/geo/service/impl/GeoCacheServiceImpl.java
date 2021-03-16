package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.entity.GeoImageCacheEntity;
import cn.booktable.geo.service.GeoCacheService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author ljc
 */
public class GeoCacheServiceImpl implements GeoCacheService {

    @Override
    public boolean saveCache(GeoImageCacheEntity imageCache) {

        Connection conn= DBHelper.getConnection();
        PreparedStatement ps=null;
        try{
            if(imageCache.getZ()!=null && imageCache.getX()!=null && imageCache.getY()!=null) {
                String sql = "INSERT INTO geo_image_cache ( cache_id, image_id, z, x, y, image_data ) VALUES " +
                        "(?,?,?,?,?,?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, imageCache.getCacheId());
                ps.setString(2, imageCache.getImageId());
                ps.setInt(3, imageCache.getZ());
                ps.setInt(4, imageCache.getX());
                ps.setInt(5, imageCache.getY());
                ps.setString(6, imageCache.getImageData());
            }else{
                String sql = "INSERT INTO geo_image_cache ( cache_id, image_id, image_data ) VALUES " +
                        "(?,?,?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, imageCache.getCacheId());
                ps.setString(2, imageCache.getImageId());
                ps.setString(3, imageCache.getImageData());
            }
            return ps.execute();
        }catch (Exception ex){
            throw new GeoException(ex);
        }finally {
            DBHelper.close(ps);
            DBHelper.close(conn);
        }
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
