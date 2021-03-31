package cn.booktable.geo.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.mysql.MySQLDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.jdbc.JDBCDataStore;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
/**
 * @author ljc
 */
public class ImportShp2Mysql {

    /**
     * 读取shp文件
     * @param shpfile
     * @return
     */
    public static SimpleFeatureSource readSHP( String shpfile){
        SimpleFeatureSource featureSource =null;
        try {
            File file = new File(shpfile);
            ShapefileDataStore shpDataStore = null;

            shpDataStore = new ShapefileDataStore(file.toURL());
            //设置编码
            Charset charset = Charset.forName("UTF-8");
            shpDataStore.setCharset(charset);
            String tableName = shpDataStore.getTypeNames()[0];
            featureSource =  shpDataStore.getFeatureSource (tableName);
        }catch (Exception e){
            e.printStackTrace();
        }
        return featureSource;
    }

    /**
     * 连接数据库
     * @param host
     * @param dataBase
     * @param port
     * @param userName
     * @param pwd
     * @return
     */
    public static JDBCDataStore  connnection2mysql(String host,String dataBase,int port,String userName,String pwd ){
        JDBCDataStore ds=null;
        DataStore dataStore=null;
        //连接数据库参数
        java.util.Map params = new java.util.HashMap();
        params.put(MySQLDataStoreFactory.DBTYPE.key, "mysql");
        params.put(MySQLDataStoreFactory.HOST.key, host);
        params.put(MySQLDataStoreFactory.PORT.key, port);
        params.put(MySQLDataStoreFactory.DATABASE.key, dataBase);
        params.put(MySQLDataStoreFactory.USER.key, userName);
        params.put(MySQLDataStoreFactory.PASSWD.key, pwd);
        try {
            dataStore=DataStoreFinder.getDataStore(params);
            if (dataStore!=null) {
                ds=(JDBCDataStore)dataStore;
                System.out.println(dataBase+"连接成功");
            }else{

                System.out.println(dataBase+"连接失败");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();

        }

        return ds;
    }

    public static JDBCDataStore createTable(JDBCDataStore ds, SimpleFeatureSource featureSource){
        SimpleFeatureType schema = featureSource.getSchema();
        try {
            //创建数据表
            ds.createSchema(schema);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ds;
    }

    public static void writeShp2Mysql(JDBCDataStore ds, SimpleFeatureSource featureSource ){
        SimpleFeatureType schema = featureSource.getSchema();
        //开始写入数据
        try {
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds .getFeatureWriter(schema.getTypeName().toLowerCase(), Transaction.AUTO_COMMIT);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures();
            SimpleFeatureIterator features = featureCollection.features();
            while (features.hasNext()) {
                writer.hasNext();
                SimpleFeature next = writer.next();
                SimpleFeature feature = features.next();
                for (int i = 0; i < feature.getAttributeCount(); i++) {
                    next.setAttribute(i,feature.getAttribute(i) );
                }
                writer.write();
            }
            writer.close();
            ds.dispose();
            System.out.println("导入成功");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // SimpleFeatureIterator itertor = featureSource.getFeatures() // .features(); //create the builder SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);



    }


    //测试代码
    public static void main(String[] args) {
        JDBCDataStore connnection2mysql = ImportShp2Mysql.connnection2mysql("localhost", "geotools", 3306, "dev", "dev123");
        SimpleFeatureSource featureSource = readSHP("/workspace/github/geotools/data/ne_50m_admin_0_countries/ne_50m_admin_0_countries.shp");
        JDBCDataStore ds = createTable(connnection2mysql, featureSource);
        writeShp2Mysql(ds, featureSource);
    }
}