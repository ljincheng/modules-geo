package cn.booktable.geo.utils;

import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
public class FeatureUtil {

    public static boolean addFeature(JDBCDataStore mDataStore, String featureName, Map<String, Object> atts,String id) {
        boolean result=false;
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource=(SimpleFeatureStore)mDataStore.getFeatureSource(featureName);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            SimpleFeatureType schema = featureSource.getSchema();
            Object[] attributes = new Object[schema.getAttributeCount()];
            for (int i = 0; i < attributes.length; i++) {
                AttributeDescriptor descriptor = schema.getDescriptor(i);
                attributes[i] =atts.get(descriptor.getLocalName());
            }
            SimpleFeature feature = SimpleFeatureBuilder.build(schema, attributes,id);
            if(StringUtils.isNotBlank(id)) {
                feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
                feature.getUserData().put(Hints.PROVIDED_FID, id);
            }
            SimpleFeatureCollection collection = DataUtilities.collection(feature);
            featureSource.addFeatures(collection);
            transaction.commit();
            result=true;
        } catch (Exception e) {
            throw new GeoException(e.fillInStackTrace());
        }
        return result;
    }
}
