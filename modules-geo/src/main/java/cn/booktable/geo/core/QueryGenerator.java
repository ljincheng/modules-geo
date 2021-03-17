package cn.booktable.geo.core;

import org.apache.commons.lang3.StringUtils;
import org.geotools.data.Query;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;

/**
 * @author ljc
 */
public class QueryGenerator {

    public static boolean hasFilter(GeoQuery query){
        if(query==null || query.getFilter()==null){
            return false;
        }
        Object filter=query.getFilter();
        if(filter instanceof  String){
            if(StringUtils.isBlank((String)filter)){
                return false;
            }
        }
        return true;
    }

    public static boolean hasLayerName(GeoQuery query){
        if(query==null || query.getLayerName()==null){
            return false;
        }
        if(StringUtils.isBlank(query.getLayerName())){
            return false;
        }
        return true;
    }

    public static Filter getFilter(GeoQuery query){
        Filter queryFilter=null;
        try {
            Object filter = query.getFilter();
            if (filter instanceof String) {
                queryFilter = CQL.toFilter((String) filter);
            } else if (filter instanceof Filter) {
                queryFilter = (Filter) filter;
            }
        }catch (CQLException e){
            e.printStackTrace();
            throw new GeoException(e);
        }
        return queryFilter;
    }

    public static Query toQuery(GeoQuery query){
        Query result=new Query(query.getLayerName());
        if(hasFilter(query)) {
            result.setFilter(getFilter(query));
        }
        return result;
    }
}
