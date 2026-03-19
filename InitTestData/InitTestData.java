import com.bookstore.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class initTestData {
}

public static void main(String[] args) {
    try (Connection conn = DbUtil.getConnection()) {
        System.out.println("数据库连接成功！当前数据库：" + conn.getCatalog());

        // 后面继续你的插入代码...
        // 插入测试用户（4个角色）
        String sqlUser = "INSERT INTO Users (username, password_hash, role, real_name, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setString(1, "admin1"); ps.setString(2, "123456"); ps.setString(3, "admin"); ps.setString(4, "管理员A"); ps.setString(5, "13800000001");
            ps.addBatch();
            ps.setString(1, "op1"); ps.setString(2, "123456"); ps.setString(3, "operator"); ps.setString(4, "运营小王"); ps.setString(5, "13800000002");
            ps.addBatch();
            ps.setString(1, "wh1"); ps.setString(2, "123456"); ps.setString(3, "warehouse"); ps.setString(4, "仓管老李"); ps.setString(5, "13800000003");
            ps.addBatch();
            ps.setString(1, "cs1"); ps.setString(2, "123456"); ps.setString(3, "support"); ps.setString(4, "客服小张"); ps.setString(5, "13800000004");
            ps.addBatch();
            ps.executeBatch();
        }

        // 插入测试分类（简单3个）
        String sqlCat = "INSERT INTO BookCategories (category_name, description) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlCat)) {
            ps.setString(1, "计算机/互联网"); ps.setString(2, "编程、算法、数据库等");
            ps.addBatch();
            ps.setString(1, "小说文学"); ps.setString(2, "中外经典小说");
            ps.setString(1, "经济管理"); ps.setString(2, "理财、职场、管理学");
            ps.addBatch();
            ps.executeBatch();
        }

        System.out.println("测试数据插入成功（用户+分类）");
    } catch (SQLException e) {
        System.err.println("连接失败！请检查 URL/USER/PASSWORD");
        e.printStackTrace();
    }
}