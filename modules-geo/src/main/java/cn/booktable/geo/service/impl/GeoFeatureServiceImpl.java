package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.entity.GeoGeometryEntity;
import cn.booktable.geo.service.GeoFeatureService;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCFeatureReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author ljc
 */
public class GeoFeatureServiceImpl implements GeoFeatureService {

    private JDBCDataStore mDataStore;
    {
        mDataStore= (JDBCDataStore)DBHelper.dataStore();
    }

    @Override
    public GeoGeometryEntity createGeometry(String layerName, Geometry geometry) {
        try {
            SimpleFeatureSource featureSource = mDataStore.getFeatureSource(layerName);
            if (featureSource == null) {
                throw new GeoException("图层不存在");
            }
            SimpleFeatureType schema = featureSource.getSchema();
            List<AttributeDescriptor> attrList= schema.getAttributeDescriptors();
            GeometryDescriptor descriptor = schema.getGeometryDescriptor();
            Map<String,Object> properties=new HashMap<>();
            if(attrList!=null){
                for(AttributeDescriptor attr:attrList){
                    properties.put(attr.getName().toString(),null);
                }
            }
            GeoGeometryEntity geometryEntity=new GeoGeometryEntity();
            geometryEntity.setProperties(properties);
            geometryEntity.setName(descriptor.getName().toString());
            geometryEntity.setGeometry(geometry);
            return geometryEntity;
        }catch (Exception ex){
            throw new GeoException(ex);
        }
    }

    @Override
    public boolean addGeometry(String layerName,GeoGeometryEntity geometryEntity) {
        try {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(layerName);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            SimpleFeatureType schema = featureSource.getSchema();
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer =mDataStore.getFeatureWriterAppend(schema.getTypeName().toLowerCase(), Transaction.AUTO_COMMIT);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures();
            SimpleFeature next = writer.next();
            Map<String, Object> atts = geometryEntity.getProperties();
//            GeometryDescriptor descriptor = schema.getGeometryDescriptor();
//            atts.put(descriptor.getName().toString(), geometryEntity.getGeometry());
            for (Map.Entry<String, Object> entry : atts.entrySet()) {
                next.setAttribute(entry.getKey(), entry.getValue());
            }

            writer.write();
            writer.close();
        } catch (Exception e) {
           throw new GeoException(e);
        }
        return true;
    }

    @Override
    public boolean updateGeometry(String layerName,GeoGeometryEntity geometryEntity) {
        return false;
    }

    @Override
    public boolean deleteGeometry(String layerName,String id) {
        return false;
    }

    @Override
    public boolean queryGeometry(String layerName,GeoGeometryEntity geometryEntity) {
        return false;
    }

//    private SimpleFeature toSimpleFeature(SimpleFeature next,GeoGeometryEntity geometryEntity){
//       Map<String,Object> attrs=geometryEntity.getAttributes();
//       next.setAttributes(geometryEntity.getAttributes());
//    }
}
