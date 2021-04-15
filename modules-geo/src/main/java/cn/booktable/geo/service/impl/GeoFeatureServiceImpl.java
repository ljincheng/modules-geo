package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.DBHelper;
import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.QueryGenerator;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.jdbc.JDBCFeatureReader;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;

import java.util.*;

/**
 * @author ljc
 */
public class GeoFeatureServiceImpl implements GeoFeatureService {
    GeoMapManageService geoMapManageService=new GeoMapManageServiceImpl();
    static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();
    private DataStore  mDataStore;
    {
        mDataStore= DBHelper.dataStore();
    }

    private Query toFeatureQuery(GeoQuery geoQuery,String layerFilter){
        try {
            Query readQuery = new Query();
            String filter=geoQuery.getFilter();
            String featureId=geoQuery.getFeatureId();
            Filter readFilter=null;
            if(StringUtils.isNotBlank(layerFilter)){
                readFilter=CQL.toFilter(layerFilter);
            }
            if(StringUtils.isNotBlank(filter) ){
                if(readFilter==null) {
                    readFilter = CQL.toFilter(filter);
                }else{
                    readFilter=FF.and(CQL.toFilter(filter),readFilter);
                }
            }
            if(StringUtils.isNotBlank(featureId)){
                String[] featureIds=featureId.split(",");
                if(featureIds.length>0){
                    Set<FeatureId> selected = new HashSet<FeatureId>();
                    for(String id:featureIds) {
                        selected.add(FF.featureId(id));
                    }
                    if(readFilter==null) {
                        readFilter = FF.id(selected);
                    }else{
                        readFilter=FF.and(FF.id(selected),readFilter);
                    }
                }
            }


            if (readFilter!=null) {
                readQuery.setFilter(readFilter);
            }
            return readQuery;
        }catch (CQLException e){
            throw new GeoException(e.fillInStackTrace());
        }
    }

    @Override
    public boolean addFeature(GeoFeature geometryEntity) {
        try (Transaction transaction = new DefaultTransaction()) {
            String uuid=geometryEntity.getId();
            if(StringUtils.isBlank(uuid)){
                uuid=UUID.randomUUID().toString().replace("-","");
            }
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(geometryEntity.getLayerName());
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("图层不存在");
            }
            Map<String, Object> atts = geometryEntity.getProperties();
            SimpleFeatureType schema = featureSource.getSchema();

            Object[] attributes = new Object[schema.getAttributeCount()];
            atts.put(schema.getGeometryDescriptor().getLocalName(),GeoGeometryProvider.parserJsonFormat(geometryEntity.getGeometry()));
            for (int i = 0; i < attributes.length; i++) {
                AttributeDescriptor descriptor = schema.getDescriptor(i);
                attributes[i] =atts.get(descriptor.getLocalName());
            }
            SimpleFeature feature =SimpleFeatureBuilder.build(schema, attributes, uuid);
            feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
            feature.getUserData().put(Hints.PROVIDED_FID, uuid);
            SimpleFeatureCollection collection = DataUtilities.collection(feature);
            featureSource.addFeatures(collection);
            transaction.commit();

        } catch (Exception e) {
            e.printStackTrace();
           throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    @Override
    public boolean updateFeature(GeoQuery query, GeoFeature geometryEntity) {
        assert (query!=null && QueryGenerator.hasFilter(query));
        try {
//            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerName());
//            if(featureSource==null){
//                throw new GeoException("图层不存在");
//            }
//
//            Filter filter= QueryGenerator.getFilter(query);
//            Map<String, Object> atts = geometryEntity.getProperties();
//            SimpleFeatureType schema = featureSource.getSchema();
//            FeatureWriter<SimpleFeatureType, SimpleFeature> writer =mDataStore.getFeatureWriter(schema.getTypeName(),filter, Transaction.AUTO_COMMIT);
//            while (writer.hasNext()) {
//                SimpleFeature next = writer.next();
//                GeometryDescriptor geomCol = schema.getGeometryDescriptor();
//                List<AttributeDescriptor> descList = schema.getAttributeDescriptors();
//                for (AttributeDescriptor att : descList) {
//                    next.setAttribute(att.getLocalName(), atts.get(att.getLocalName()));
//                }
//                next.setAttribute(geomCol.getLocalName(), GeoGeometryProvider.parserJsonFormat(geometryEntity.getGeometry()));
//                writer.write();
//            }
//            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }

    @Override
    public boolean deleteFeature(GeoQuery query) {
        assert(query!=null &&  QueryGenerator.hasFilter(query));

        GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerId());
        if(mapLayerEntity==null || mapLayerEntity.getLayerInfoEntity()==null && QueryGenerator.hasFilter(query)){
            return false;
        }

        try (Transaction transaction = new DefaultTransaction()) {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(mapLayerEntity.getLayerInfoEntity().getLayerName());
            if(featureSource==null){
                return false;
            }
            String layerFilter= mapLayerEntity.getLayerInfoEntity().getLayerFilter();
            Query readQuery= toFeatureQuery(query,layerFilter);
             try {
                 SimpleFeatureStore store = (SimpleFeatureStore) featureSource;
                 store.setTransaction(transaction);
                 store.removeFeatures(readQuery.getFilter());
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
    public GeoFeature findFeatureById(GeoQuery query) {
        assert(query!=null );
        assert (StringUtils.isNotBlank(query.getLayerId()));
        assert (StringUtils.isNotBlank(query.getFilter()));
        GeoFeature geoFeature=null;
        try {
            GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerId());
            if(mapLayerEntity==null || mapLayerEntity.getLayerInfoEntity()==null && QueryGenerator.hasFilter(query)){
                return null;
            }
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(mapLayerEntity.getLayerInfoEntity().getLayerName());
            if(featureSource==null){
                return null;
            }
            String layerFilter= mapLayerEntity.getLayerInfoEntity().getLayerFilter();
            Query readQuery= toFeatureQuery(query,layerFilter);
            DefaultFeatureResults results = new DefaultFeatureResults(featureSource, readQuery);
            if(results.getCount()>0){
                FeatureIterator<SimpleFeature> featureIterator= results.collection().features();
                if (featureIterator.hasNext()){
                    SimpleFeature feature= featureIterator.next();
                    geoFeature=new GeoFeature();
                    geoFeature.setId(feature.getIdentifier().getID());
                    Map<String,Object> proMap=new HashMap<>();
                    Iterator<Property> pit= feature.getProperties().iterator();
                    while (pit.hasNext()){
                        Property pr=pit.next();
                        proMap.put(pr.getName().getLocalPart(),pr.getValue());
                    }
                    geoFeature.setProperties(proMap);
                    Object geometryObj=feature.getDefaultGeometry();
                    if(geometryObj!=null) {
                        geoFeature.setGeometry(GeoGeometryProvider.parserJsonString((Geometry) geometryObj));
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }

        return geoFeature;
    }

    @Override
    public List<GeoFeature> queryFeature(GeoQuery query) {
        assert(query!=null );
        assert (StringUtils.isNotBlank(query.getLayerId()));
        assert (StringUtils.isNotBlank(query.getFilter()));
        List<GeoFeature> result=new ArrayList<>();
        try {
            GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerId());
            if(mapLayerEntity==null || mapLayerEntity.getLayerInfoEntity()==null && QueryGenerator.hasFilter(query)){
                return null;
            }
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(mapLayerEntity.getLayerInfoEntity().getLayerName());
            if(featureSource==null){
                return null;
            }

            String layerFilter= mapLayerEntity.getLayerInfoEntity().getLayerFilter();
            Query readQuery= toFeatureQuery(query,layerFilter);
            DefaultFeatureResults results = new DefaultFeatureResults(featureSource, readQuery);
            if(results.getCount()>0){
                FeatureIterator<SimpleFeature> featureIterator= results.collection().features();
                while (featureIterator.hasNext()){
                    SimpleFeature feature= featureIterator.next();
                    GeoFeature geoFeature=new GeoFeature();
                    geoFeature.setId(feature.getIdentifier().getID());
                    Map<String,Object> proMap=new HashMap<>();
                    Iterator<Property> pit= feature.getProperties().iterator();
                    while (pit.hasNext()){
                        Property pr=pit.next();
                        proMap.put(pr.getName().getLocalPart(),pr.getValue());
                    }
                    geoFeature.setProperties(proMap);
                    Object geometryObj=feature.getDefaultGeometry();
                    if(geometryObj!=null) {
                        geoFeature.setGeometry(GeoGeometryProvider.parserJsonString((Geometry) geometryObj));
                    }
                    result.add(geoFeature);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }

        return result;
    }


    @Override
    public void writeFeature(GeoQuery query, Object output){
        assert(query!=null);
        assert(StringUtils.isNotBlank(query.getMapId()));
        assert(StringUtils.isNotBlank(query.getLayerId()));
        assert(StringUtils.isNotBlank(query.getFilter()));
//        List<SimpleFeature> result=new ArrayList<>();
        try {
            GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerId());
            if(mapLayerEntity==null || mapLayerEntity.getLayerInfoEntity()==null && QueryGenerator.hasFilter(query)){
                return ;
            }
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(mapLayerEntity.getLayerInfoEntity().getLayerName());
            if(featureSource==null){
                return;
            }
            String filter=query.getFilter();
            String layerFilter= mapLayerEntity.getLayerInfoEntity().getLayerFilter();
            if(layerFilter!=null && layerFilter.trim().length()>0){
                filter +=" and "+layerFilter;
            }

            query.setFilter(filter);
            Query readQuery= new Query();
            readQuery.setFilter(CQL.toFilter(filter));
            DefaultFeatureResults results = new DefaultFeatureResults(featureSource, readQuery);
            if(results.getCount()>0) {
                FeatureJSON fj = new FeatureJSON();
                fj.writeFeatureCollection(results.collection(), output);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }


    }

}
