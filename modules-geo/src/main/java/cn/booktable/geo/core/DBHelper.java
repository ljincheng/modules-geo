package cn.booktable.geo.core;

import org.geotools.data.Transaction;
import java.io.IOException;
import java.sql.*;

/**
 * @author ljc
 */
public class DBHelper {

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

    public static  void close(Transaction transaction){
        try {
            if (transaction != null) {
                transaction.close();
            }
        }catch (Exception ex){

        }
    }

    public static void rollback(Transaction transaction){
        try {
            if (transaction != null) {
                transaction.rollback();
            }
        }catch (IOException ex){
            throw new GeoException(ex.fillInStackTrace());
        }
    }


}
