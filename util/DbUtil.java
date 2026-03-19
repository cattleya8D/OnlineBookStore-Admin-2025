package com.bookstore.util;

import java.sql.*;

public class DbUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/bookstore_order_sys?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";          // 改成你自己的
    private static final String PASSWORD = "qwertyuiop";   // 改成你自己的

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }

    // 事务相关方法（后面订单会用到）
    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    public static void commit(Connection conn) throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public static void rollback(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}