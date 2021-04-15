package cn.booktable.geo.core;

import jdk.nashorn.internal.scripts.JD;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.jdbc.JDBCDataStore;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * @author ljc
 */
public class DBHelper {
    private static Map<String,Object> mDBParam=null;
    private static JDBCDataStore mDataStore;
    static {
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "mysql");
        params.put("host", "localhost");
        params.put("port", 3306);
        params.put("database", "geotools");
        params.put("user", "dev");
        params.put("passwd", "dev123");
        mDBParam=params;
        try {
            mDataStore = (JDBCDataStore) DataStoreFinder.getDataStore(mDBParam);
        }catch (IOException e){
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
    }

    public static DataStore dataStore(){
        return mDataStore;
    }


    public static DataSource getDatabase(){
        if(mDataStore!=null) {
            return mDataStore.getDataSource();
        }
        return null;
    }

    public static Connection getConnection(){
        try{
            DataSource dataSource=getDatabase();
            return dataSource.getConnection();
        }catch (SQLException e){
            e.printStackTrace();
            throw new GeoException(e.fillInStackTrace());
        }
    }

    public static  void close(Connection connection){
        try {
            if (connection != null) {
                connection.close();
            }
        }catch (Exception ex){

        }
    }

    public static  void close(Statement statement){
        try {
            if (statement != null) {
                statement.close();
            }
        }catch (Exception ex){

        }
    }

    public static  void dispose(){
        try {
            mDataStore.dispose();
        }catch (Exception ex){
        }
    }

    public static <T extends TableObject> List<T> queryList(Connection connection,String tableName,String[] columns,String orderBy,Class<T> t){

        ArrayList   mapInfoList=new ArrayList<T>();
        StringBuilder sql=new StringBuilder(100);
        sql.append("SELECT ");
        String columnStr= StringUtils.join(columns,",");
        sql.append(columnStr).append(" FROM ").append(tableName);
        if(StringUtils.isNotBlank(orderBy))
        {
            sql.append(" Order by ").append(orderBy);
        }

        Statement statement =null;
        try{
             statement =connection.createStatement();
             ResultSet result = statement.executeQuery(sql.toString()) ;
            while (result.next()) {
                   T obj= t.newInstance();
                    ( (TableObject)obj).restore(result);
                    mapInfoList.add(obj);

            }
        }catch (Exception ex){
            ex.printStackTrace();
            throw new GeoException(ex);
        }finally {
            close(statement);
        }
        return mapInfoList;
    }


    public static boolean execute(Connection connection,String sql){
        Statement statement =null;
        try{
            statement =connection.createStatement();
            return statement.execute(sql);
        }catch (Exception ex){
            ex.printStackTrace();
            throw new GeoException(ex);
        }finally {
            close(statement);
        }
    }

}
