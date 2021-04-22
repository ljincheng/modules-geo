package cn.booktable.appadmin.config;

import cn.booktable.geo.core.GeoEngine;
import cn.booktable.geo.core.GeoEngineImpl;
import cn.booktable.geo.service.GeoCacheService;
import cn.booktable.geo.service.GeoFeatureService;
import cn.booktable.geo.service.GeoMapManageService;
import cn.booktable.geo.service.GeoMapService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author ljc
 */
@Configuration
public class GeoWMSConfig {

    @Bean
    public GeoEngine geoEngine(DataSource dataSource){
        return new GeoEngineImpl(dataSource);
    }

    @Bean
    public GeoMapService geoMapService(GeoEngine geoEngine){
        return geoEngine.getGeoMapService();
    }

    @Bean
    public GeoCacheService geoCacheService(GeoEngine geoEngine){
        return geoEngine.getGeoCacheService();
    }

    @Bean
    public GeoFeatureService geoFeatureService(GeoEngine geoEngine){
        return geoEngine.getGeoFeatureService();
    }

    @Bean
    public GeoMapManageService geoMapManageService(GeoEngine geoEngine){
        return geoEngine.getGeoMapManageService();
    }

}
