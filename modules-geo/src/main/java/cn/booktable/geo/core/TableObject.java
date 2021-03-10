package cn.booktable.geo.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface TableObject {
    void restore(ResultSet res);
    boolean insert(Connection conn);
    boolean delete(Connection conn);
}
