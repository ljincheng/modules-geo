package cn.booktable.geo.utils;

import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.QueryGenerator;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ljc
 */
public class FeatureUtil {
    static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();

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


    public static boolean deleteFeature(JDBCDataStore mDataStore, String featureName,String filter){
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(featureName);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            SimpleFeatureType schema = featureSource.getSchema();
            featureSource.removeFeatures(QueryGenerator.toFilter(filter));
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    public static boolean deleteFeatureById(JDBCDataStore mDataStore, String featureName,String id){
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(featureName);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }

            Set<FeatureId> selected = new HashSet<FeatureId>();
            selected.add(FF.featureId(id));
            SimpleFeatureType schema = featureSource.getSchema();
            featureSource.removeFeatures(FF.id(selected));
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    public static boolean modifyFeature(JDBCDataStore mDataStore, String featureName,String[] names,Object[] values,String filter){
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(featureName);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            SimpleFeatureType schema = featureSource.getSchema();
            featureSource.modifyFeatures(names,values,QueryGenerator.toFilter(filter));
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    public static boolean modifyFeatureById(JDBCDataStore mDataStore, String featureName,String[] names,Object[] values,String id){
        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(featureName);
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            Set<FeatureId> selected = new HashSet<FeatureId>();
            selected.add(FF.featureId(id));
            SimpleFeatureType schema = featureSource.getSchema();
            featureSource.modifyFeatures(names,values,FF.id(selected));
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }
}
