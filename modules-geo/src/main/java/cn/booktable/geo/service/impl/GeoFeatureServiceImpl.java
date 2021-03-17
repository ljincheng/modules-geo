package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.QueryGenerator;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.service.GeoFeatureService;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;

import java.util.*;

/**
 * @author ljc
 */
public class GeoFeatureServiceImpl implements GeoFeatureService {

    private DataStore  mDataStore;
    {
        mDataStore= DBHelper.dataStore();
    }

    @Override
    public GeoFeature createGeometry(String layerName, Geometry geometry) {
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
                    properties.put(attr.getLocalName(),null);
                }
            }
            GeoFeature geometryEntity=new GeoFeature();
            geometryEntity.setProperties(properties);
            geometryEntity.setName(descriptor.getName().toString());
            geometryEntity.setGeometry(geometry);
            return geometryEntity;
        }catch (Exception ex){
            throw new GeoException(ex);
        }
    }

    @Override
    public boolean addFeature(String layerName, GeoFeature geometryEntity) {
        try {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(layerName);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            Map<String, Object> atts = geometryEntity.getProperties();
            SimpleFeatureType schema = featureSource.getSchema();
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer =mDataStore.getFeatureWriterAppend(schema.getTypeName(), Transaction.AUTO_COMMIT);
            SimpleFeature next = writer.next();
            GeometryDescriptor geomCol = schema.getGeometryDescriptor();
             List<AttributeDescriptor> descList=schema.getAttributeDescriptors();
             for(AttributeDescriptor att:descList){
                 next.setAttribute(att.getLocalName(),atts.get(att.getLocalName()));
             }
            next.setAttribute(geomCol.getLocalName(), GeoGeometryProvider.getGeometry(geometryEntity));
            writer.write();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
           throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    @Override
    public boolean updateFeature(GeoQuery query, GeoFeature geometryEntity) {
        assert (QueryGenerator.hasLayerName(query) && QueryGenerator.hasFilter(query));
        try {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerName());
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }

            Filter filter= QueryGenerator.getFilter(query);
            Map<String, Object> atts = geometryEntity.getProperties();
            SimpleFeatureType schema = featureSource.getSchema();
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer =mDataStore.getFeatureWriter(schema.getTypeName(),filter, Transaction.AUTO_COMMIT);
            while (writer.hasNext()) {
                SimpleFeature next = writer.next();
                GeometryDescriptor geomCol = schema.getGeometryDescriptor();
                List<AttributeDescriptor> descList = schema.getAttributeDescriptors();
                for (AttributeDescriptor att : descList) {
                    next.setAttribute(att.getLocalName(), atts.get(att.getLocalName()));
                }
                next.setAttribute(geomCol.getLocalName(), GeoGeometryProvider.getGeometry(geometryEntity));
                writer.write();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    @Override
    public boolean deleteFeature(GeoQuery query) {
        assert(query!=null && QueryGenerator.hasLayerName(query) && QueryGenerator.hasFilter(query));
        Transaction transaction =null;
        try {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerName());
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            Filter queryFilter= QueryGenerator.getFilter(query);
             transaction = new DefaultTransaction("remove-"+query.getLayerName());
             try {
                 SimpleFeatureStore store = (SimpleFeatureStore) featureSource;
                 store.setTransaction(transaction);
                 store.removeFeatures(queryFilter);
                 transaction.commit();
             }catch (Exception ex){
                 transaction.rollback();
             }

        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    @Override
    public List<GeoFeature> queryFeature(GeoQuery query) {
        assert(query!=null && QueryGenerator.hasLayerName(query));
        List<GeoFeature> result=new ArrayList<>();
        try {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerName());
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            SimpleFeatureType schema = featureSource.getSchema();
            Query readQuery= QueryGenerator.toQuery(query);
            FeatureReader<SimpleFeatureType, SimpleFeature> reader =mDataStore.getFeatureReader(readQuery, Transaction.AUTO_COMMIT);
            Map<String,Object> proMap=new HashMap<>();
            while (reader.hasNext()) {
                SimpleFeature next = reader.next();
                GeoFeature featureEntity=new GeoFeature();
                Iterator<Property> pit= next.getProperties().iterator();

                while (pit.hasNext()){
                    Property pr= pit.next();
                    proMap.put(pr.getName().getLocalPart(),pr.getValue());
                }
                featureEntity.setProperties(proMap);
                Object geom=next.getDefaultGeometry();
                if(geom!=null && geom instanceof Geometry) {
                    featureEntity.setGeometry((Geometry)geom);
                }
                featureEntity.setName(next.getDefaultGeometryProperty().getName().getLocalPart());
                result.add(featureEntity);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return result;
    }

}
