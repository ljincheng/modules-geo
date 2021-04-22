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

    public static Filter toFilter(String filter){
        Filter queryFilter=null;
        try {
                return  CQL.toFilter(filter);
        }catch (CQLException e){
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
    }
}
