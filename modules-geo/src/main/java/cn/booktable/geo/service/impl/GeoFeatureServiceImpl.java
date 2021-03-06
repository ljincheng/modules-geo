package cn.booktable.geo.service.impl;

import cn.booktable.geo.core.GeoException;
import cn.booktable.geo.core.GeoQuery;
import cn.booktable.geo.core.GeoFeature;
import cn.booktable.geo.core.QueryGenerator;
import cn.booktable.geo.entity.GeoMapLayerEntity;
import cn.booktable.geo.provider.GeoGeometryProvider;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import java.util.*;

/**
 * @author ljc
 */
public class GeoFeatureServiceImpl implements GeoFeatureService {
    GeoMapManageService geoMapManageService=null;
    static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2();
    private DataStore  mDataStore;

    public GeoFeatureServiceImpl(DataStore dataStore){
        mDataStore=dataStore;
        geoMapManageService=new GeoMapManageServiceImpl((JDBCDataStore) mDataStore);
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
    public boolean addFeature(GeoFeature getFeature) {

        try (Transaction transaction = new DefaultTransaction()) {
            String uuid=getFeature.getId();
            if(StringUtils.isBlank(uuid)){
                uuid=UUID.randomUUID().toString().replace("-","");
            }
            SimpleFeatureStore featureSource= (SimpleFeatureStore)mDataStore.getFeatureSource(getFeature.getLayerSource());
            featureSource.setTransaction(transaction);
            if(featureSource==null){
                throw new GeoException("???????????????");
            }
            Map<String, Object> atts = getFeature.getProperties();
            if(atts==null){
                atts=new HashMap<>();
            }
            SimpleFeatureType schema = featureSource.getSchema();

            Object[] attributes = new Object[schema.getAttributeCount()];
            atts.put(schema.getGeometryDescriptor().getLocalName(),GeoGeometryProvider.parserJsonFormat(getFeature.getGeometry()));
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
//                throw new GeoException("???????????????");
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
        assert(query!=null &&  StringUtils.isNotBlank(query.getLayerSource()));

//        GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerSource());
//        if(mapLayerEntity==null ){
//            return false;
//        }
        try  {
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerSource());
            if(featureSource==null){
                return false;
            }
//            String layerFilter= mapLayerEntity.getLayerFilter();
            Query readQuery= toFeatureQuery(query,null);
             try(Transaction transaction = new DefaultTransaction()) {
                 SimpleFeatureStore store = (SimpleFeatureStore) featureSource;
                 store.setTransaction(transaction);
                 store.removeFeatures(readQuery.getFilter());
                 transaction.commit();
             }

        } catch (Exception e) {
            throw new GeoException(e.fillInStackTrace());
        }
        return true;
    }



    @Override
    public GeoFeature findFeatureById(GeoQuery query) {
        assert(query!=null );
        assert (StringUtils.isNotBlank(query.getLayerSource()));
        assert (StringUtils.isNotBlank(query.getFeatureId()));
        GeoFeature geoFeature=null;
        String layerFilter=null;
        try {

            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerSource());
            if(featureSource==null){
                return null;
            }
//            GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerSource());
//            if(mapLayerEntity!=null ){
//                layerFilter= mapLayerEntity.getLayerFilter();
//            }
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
            throw new GeoException(e.fillInStackTrace());
        }

        return geoFeature;
    }

    @Override
    public List<GeoFeature> queryFeature(GeoQuery query) {
        assert(query!=null );
        assert (StringUtils.isNotBlank(query.getLayerSource()));
        assert (StringUtils.isNotBlank(query.getFilter()));
        List<GeoFeature> result=new ArrayList<>();
        try {
//
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerSource());
            if(featureSource==null){
                return null;
            }
//            String layerFilter=null;
//            GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerSource());
//            if(mapLayerEntity!=null ){
//                layerFilter= mapLayerEntity.getLayerFilter();
//            }
//            String layerFilter= mapLayerEntity.getLayerFilter();
            Query readQuery= toFeatureQuery(query,null);
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
    public List<GeoFeature> queryFeatureByMapLayerId(GeoQuery query) {
        assert(query!=null );
        assert (StringUtils.isNotBlank(query.getLayerId()));
        assert (StringUtils.isNotBlank(query.getFilter()));
        List<GeoFeature> result=new ArrayList<>();
        String layerFilter=null;
        try {
            GeoMapLayerEntity mapLayerEntity= geoMapManageService.queryMapLayersByLayerId(query.getMapId(),query.getLayerId());
            if(mapLayerEntity==null ){
                return null;
            }
            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(mapLayerEntity.getLayerSource());
            if(featureSource==null){
                return null;
            }
            layerFilter= mapLayerEntity.getLayerFilter();
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
    public void writeFeatureByMapLayerSource(GeoQuery query, Object output){
        assert(query!=null);
        assert(StringUtils.isNotBlank(query.getMapId()));
        assert(StringUtils.isNotBlank(query.getLayerSource()));
        assert(StringUtils.isNotBlank(query.getFilter()));
        try {

            SimpleFeatureSource featureSource= mDataStore.getFeatureSource(query.getLayerSource());
            if(featureSource==null){
                return;
            }
            String filter=query.getFilter();
            String mapFilter=null;
            List<GeoMapLayerEntity> mapLayerEntityList= geoMapManageService.queryMapLayerByLayerSource(query.getMapId(),query.getLayerSource());
            if(mapLayerEntityList!=null && mapLayerEntityList.size()>0 ){
                GeoMapLayerEntity mapLayerEntity=mapLayerEntityList.get(0);
                for(GeoMapLayerEntity layerEntity: mapLayerEntityList) {
                    String layerFilter = layerEntity.getLayerFilter();

                    if (StringUtils.isNotBlank(layerFilter)) {
                        if(mapFilter==null){
                            mapFilter="(" +layerFilter+" )";
                        }else {
                            mapFilter += " or (" + layerFilter+")";
                        }
                    }
                }
                if(StringUtils.isNotBlank(mapFilter)){
                    filter += " and "+mapFilter;
                }
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
            throw new GeoException(e.fillInStackTrace());
        }


    }

}
