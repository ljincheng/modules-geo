package cn.booktable.geo.test;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.jdbc.JDBCDataStore;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
public class SQLDialectTest {

    public static void main(String[] args) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "mysql");
        params.put("host", "localhost");
        params.put("port", 3306);
//        params.put("schema", "public");
        params.put("database", "geotools");
        params.put("user", "root");
        params.put("passwd", "");

        DataStore dataStore= DataStoreFinder.getDataStore(params);
        if(dataStore instanceof  JDBCDataStore) {
            JDBCDataStore jdbcDataStore=(JDBCDataStore)dataStore;
          String sql="SELECT map_id, title, bbox, create_time, update_time FROM gis_map_info";
            DataSource dataSource= jdbcDataStore.getDataSource();
            try (Statement statement = dataSource.getConnection().createStatement();
                 ResultSet result = statement.executeQuery(sql)) {
                if (result.next()) {

                    String mapId=result.getString(1);
                    String title=result.getString(2);
                    System.out.println(String.format("mapId=%s,title=%s",mapId,title));
                }
            }

        }
    }
}
