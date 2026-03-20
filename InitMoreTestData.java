package com.bookstore.test;

import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class InitMoreTestData {

    private static final Random random = new Random();

    public static void main(String[] args) {
        try (Connection conn = DbUtil.getConnection()) {
            System.out.println("开始批量插入更多测试数据...");

            // 清空表（保持 true，确保干净）
            boolean resetTables = true;
            if (resetTables) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    stmt.executeUpdate("TRUNCATE TABLE Reviews");
                    stmt.executeUpdate("TRUNCATE TABLE ShippingRecords");
                    stmt.executeUpdate("TRUNCATE TABLE OrderDetails");
                    stmt.executeUpdate("TRUNCATE TABLE Orders");
                    stmt.executeUpdate("TRUNCATE TABLE Customers");
                    stmt.executeUpdate("TRUNCATE TABLE Books");
                    stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                    System.out.println("已清空测试相关表");
                }
            }

            conn.setAutoCommit(false);

            try {
                insertCategoriesIfNeeded(conn);
                insertBooks(conn);
                insertCustomers(conn);
                insertOrdersAndDetailsOnly(conn);  // 只订单 + 明细
                insertReviews(conn);

                conn.commit();
                System.out.println("所有测试数据插入完毕！（ShippingRecords 表为空，真实业务通过 OrderService.shipOrder 创建物流）");

            } catch (Exception e) {
                conn.rollback();
                System.err.println("插入失败，进行回滚");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 1. 确保分类存在
    private static void insertCategoriesIfNeeded(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO BookCategories (category_name, description) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "计算机/互联网"); ps.setString(2, "编程、算法、数据库等"); ps.addBatch();
            ps.setString(1, "小说文学"); ps.setString(2, "中外经典小说"); ps.addBatch();
            ps.setString(1, "经济管理"); ps.setString(2, "理财、职场、管理学"); ps.addBatch();
            ps.executeBatch();
            System.out.println("分类数据检查/插入完成");
        }
    }

    // 获取分类ID
    private static int getCategoryId(Connection conn, String name) throws SQLException {
        String sql = "SELECT category_id FROM BookCategories WHERE category_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("分类不存在: " + name);
    }

    // 2. 插入30本书
    private static void insertBooks(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO Books (isbn, title, author, publisher, publish_year, price, stock, category_id, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // 计算机类
            addBook(ps, "9787111213826", "Java核心技术 卷I", "Cay S. Horstmann", "机械工业出版社", 2020, 129.00, 100, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787115546036", "Spring实战 第6版", "Craig Walls", "人民邮电出版社", 2022, 119.00, 80, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787121402289", "MySQL必知必会", "Ben Forta", "电子工业出版社", 2021, 69.00, 150, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787111686668", "Python编程 从入门到实践", "Eric Matthes", "人民邮电出版社", 2023, 89.00, 120, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787115546043", "Effective Java 第3版", "Joshua Bloch", "机械工业出版社", 2019, 139.00, 90, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787121387012", "算法导论 原书第4版", "Thomas H. Cormen", "机械工业出版社", 2022, 199.00, 60, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787115546050", "深入理解计算机系统", "Randal E. Bryant", "机械工业出版社", 2021, 149.00, 70, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787121402296", "大数据技术原理与应用", "林子雨", "人民邮电出版社", 2020, 79.00, 110, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787111686675", "Head First 设计模式", "Eric Freeman", "中国电力出版社", 2018, 99.00, 85, getCategoryId(conn, "计算机/互联网"));
            addBook(ps, "9787121387029", "计算机网络 自顶向下方法", "James F. Kurose", "机械工业出版社", 2023, 109.00, 95, getCategoryId(conn, "计算机/互联网"));

            // 小说类
            addBook(ps, "9787020164745", "活着", "余华", "人民文学出版社", 2017, 29.00, 200, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787532776771", "百年孤独", "加西亚·马尔克斯", "南海出版公司", 2019, 55.00, 120, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787020093403", "平凡的世界", "路遥", "人民文学出版社", 2018, 68.00, 180, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787536692930", "三体 全三册", "刘慈欣", "重庆出版社", 2020, 168.00, 50, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787020123456", "围城", "钱钟书", "人民文学出版社", 2015, 39.00, 140, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787544767897", "解忧杂货店", "东野圭吾", "南海出版公司", 2021, 49.00, 130, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787020145678", "白夜行", "东野圭吾", "南海出版公司", 2019, 59.00, 110, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787539987651", "追风筝的人", "卡勒德·胡赛尼", "上海人民出版社", 2018, 45.00, 160, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787020167890", "挪威的森林", "村上春树", "上海译文出版社", 2022, 52.00, 90, getCategoryId(conn, "小说文学"));
            addBook(ps, "9787544289016", "小王子", "圣-埃克苏佩里", "人民文学出版社", 2016, 25.00, 250, getCategoryId(conn, "小说文学"));

            // 经济管理类
            addBook(ps, "9787115546067", "富爸爸穷爸爸", "罗伯特·清崎", "四川人民出版社", 2020, 58.00, 100, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787511345678", "原则", "瑞·达利欧", "中信出版社", 2019, 98.00, 70, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787508691237", "从优秀到卓越", "吉姆·柯林斯", "中信出版社", 2021, 89.00, 80, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787111686682", "精益创业", "埃里克·莱斯", "中信出版社", 2018, 69.00, 95, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787508694568", "鞋狗", "菲尔·奈特", "中信出版社", 2022, 79.00, 85, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787115546074", "影响力", "罗伯特·西奥迪尼", "机械工业出版社", 2020, 88.00, 75, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787508697899", "原子习惯", "詹姆斯·克利尔", "中信出版社", 2023, 59.00, 120, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787111686699", "黑天鹅", "纳西姆·尼古拉斯·塔勒布", "中信出版社", 2019, 99.00, 65, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787508692340", "穷查理宝典", "查理·芒格", "中信出版社", 2021, 128.00, 55, getCategoryId(conn, "经济管理"));
            addBook(ps, "9787115546081", "思考，快与慢", "丹尼尔·卡尼曼", "中信出版社", 2022, 109.00, 60, getCategoryId(conn, "经济管理"));

            int[] counts = ps.executeBatch();
            int inserted = 0;
            for (int count : counts) {
                if (count > 0) inserted += count;
            }
            System.out.println("图书插入完成，本次新增 " + inserted + " 本（重复的已忽略）");
        }
    }

    private static void addBook(PreparedStatement ps, String isbn, String title, String author, String publisher,
                                int year, double price, int stock, int categoryId) throws SQLException {
        ps.setString(1, isbn);
        ps.setString(2, title);
        ps.setString(3, author);
        ps.setString(4, publisher);
        ps.setInt(5, year);
        ps.setBigDecimal(6, BigDecimal.valueOf(price));
        ps.setInt(7, stock);
        ps.setInt(8, categoryId);
        ps.setString(9, title + " - 经典图书推荐");
        ps.addBatch();
    }

    // 3. 插入15个客户
    private static void insertCustomers(Connection conn) throws SQLException {
        String sql = "INSERT IGNORE INTO Customers (name, phone, address, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String[] names = {"张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十", "刘十一", "陈十二", "杨十三", "黄十四", "林十五", "徐十六", "何十七"};
            String[] phones = {"13912345678", "13887654321", "13611112222", "13733334444", "13555556666", "13477778888", "13399990000", "13222223333", "13144445555", "13066667777", "13988889999", "13800001111", "13722223333", "13644445555", "13566667777"};
            String[] addresses = {"北京市朝阳区", "上海市浦东新区", "广州市天河区", "深圳市南山区", "成都市高新区", "杭州市西湖区", "南京市鼓楼区", "武汉市洪山区", "西安市雁塔区", "重庆市渝北区", "天津市和平区", "苏州市工业园区", "长沙市岳麓区", "青岛市市南区", "大连市中山区"};

            for (int i = 0; i < 15; i++) {
                ps.setString(1, names[i]);
                ps.setString(2, phones[i]);
                ps.setString(3, addresses[i] + "某小区" + (i + 1) + "号楼");
                ps.setString(4, "user" + (i + 1) + "@qq.com");
                ps.addBatch();
            }

            int[] counts = ps.executeBatch();
            int inserted = 0;
            for (int count : counts) {
                if (count > 0) inserted += count;
            }
            System.out.println("客户插入完成，本次新增 " + inserted + " 个（重复手机号已忽略）");
        }
    }

    // 4. 只插入订单 + 明细（不涉及物流）
    private static void insertOrdersAndDetailsOnly(Connection conn) throws SQLException {
        List<Long> bookIds = getAllBookIds(conn);
        List<Long> customerIds = getAllCustomerIds(conn);
        List<Long> userIds = getAllUserIds(conn);

        if (bookIds.isEmpty() || customerIds.isEmpty() || userIds.isEmpty()) {
            System.out.println("缺少图书/客户/用户数据，无法生成订单");
            return;
        }

        // 修正：与 ENUM 完全一致（删掉 "已签收"，避免不匹配报错）
        String[] statuses = {
                "待付款",
                "已付款",
                "已发货",
                "已完成",
                "已取消"
        };

        String sqlOrder = "INSERT INTO Orders (customer_id, user_id, order_date, total_amount, status, payment_method, notes) " +
                "VALUES (?, ?, NOW(), ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO OrderDetails (order_id, book_id, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {

            for (int i = 0; i < 50; i++) {
                long customerId = customerIds.get(random.nextInt(customerIds.size()));
                long userId = userIds.get(random.nextInt(userIds.size()));
                String orderStatus = statuses[random.nextInt(statuses.length)].trim();

                // 调试打印（确认值）
                System.out.println("订单 " + (i+1) + " | 状态='" + orderStatus + "' (length=" + orderStatus.length() + ")");

                String paymentMethod = random.nextBoolean() ? "支付宝" : "微信支付";
                String notes = "批量测试订单 " + (i + 1);

                int itemCount = 1 + random.nextInt(4);
                BigDecimal totalAmount = BigDecimal.ZERO;

                List<Map<String, Object>> items = new ArrayList<>();
                for (int j = 0; j < itemCount; j++) {
                    long bookId = bookIds.get(random.nextInt(bookIds.size()));
                    BigDecimal unitPrice = getBookPrice(conn, bookId);
                    int quantity = 1 + random.nextInt(3);
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

                    totalAmount = totalAmount.add(subtotal);

                    Map<String, Object> item = new HashMap<>();
                    item.put("bookId", bookId);
                    item.put("quantity", quantity);
                    item.put("unitPrice", unitPrice);
                    item.put("subtotal", subtotal);
                    items.add(item);
                }

                psOrder.setLong(1, customerId);
                psOrder.setLong(2, userId);
                psOrder.setBigDecimal(3, totalAmount);
                psOrder.setString(4, orderStatus);
                psOrder.setString(5, paymentMethod);
                psOrder.setString(6, notes);
                psOrder.executeUpdate();

                long orderId = getLastInsertId(psOrder);

                for (Map<String, Object> item : items) {
                    psDetail.setLong(1, orderId);
                    psDetail.setLong(2, (Long) item.get("bookId"));
                    psDetail.setInt(3, (Integer) item.get("quantity"));
                    psDetail.setBigDecimal(4, (BigDecimal) item.get("unitPrice"));
                    psDetail.setBigDecimal(5, (BigDecimal) item.get("subtotal"));
                    psDetail.addBatch();
                }
                psDetail.executeBatch();

                System.out.println("订单创建成功: ID=" + orderId + ", 状态=" + orderStatus);
            }

            System.out.println("订单 + 明细插入完成（ShippingRecords 表为空）");
        }
    }

    // 5. 插入评价
    private static void insertReviews(Connection conn) throws SQLException {
        List<Long> completedOrderIds = getCompletedOrderIds(conn);
        if (completedOrderIds.isEmpty()) {
            System.out.println("警告：当前无已完成订单，跳过评价插入");
            return;
        }

        List<Long> bookIds = getAllBookIds(conn);
        List<Long> customerIds = getAllCustomerIds(conn);

        String sql = "INSERT INTO Reviews (order_id, book_id, customer_id, rating, comment_text, review_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < 10; i++) {
                long orderId = completedOrderIds.get(random.nextInt(completedOrderIds.size()));
                long bookId = bookIds.get(random.nextInt(bookIds.size()));
                long customerId = customerIds.get(random.nextInt(customerIds.size()));
                int rating = 3 + random.nextInt(3);
                String[] comments = {"质量很好，值得推荐", "发货快，包装仔细", "内容充实，学习到了很多", "性价比高，下次还买", "书本印刷清晰，赞！"};

                ps.setLong(1, orderId);
                ps.setLong(2, bookId);
                ps.setLong(3, customerId);
                ps.setInt(4, rating);
                ps.setString(5, comments[random.nextInt(comments.length)]);
                ps.setObject(6, LocalDateTime.now().minusDays(random.nextInt(30)));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("10条评价插入完成");
        }
    }

    // 辅助方法
    private static long getLastInsertId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getLong(1);
        }
        return -1;
    }

    private static List<Long> getAllBookIds(Connection conn) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT book_id FROM Books")) {
            while (rs.next()) ids.add(rs.getLong("book_id"));
        }
        return ids;
    }

    private static List<Long> getAllCustomerIds(Connection conn) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT customer_id FROM Customers")) {
            while (rs.next()) ids.add(rs.getLong("customer_id"));
        }
        return ids;
    }

    private static List<Long> getCompletedOrderIds(Connection conn) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT order_id FROM Orders WHERE status = '已完成'")) {
            while (rs.next()) ids.add(rs.getLong("order_id"));
        }
        return ids;
    }

    private static BigDecimal getBookPrice(Connection conn, long bookId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT price FROM Books WHERE book_id = ?")) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("price");
            }
        }
        return BigDecimal.valueOf(50.00);
    }

    private static List<Long> getAllUserIds(Connection conn) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id FROM Users")) {
            while (rs.next()) {
                ids.add(rs.getLong("user_id"));
            }
        }
        return ids;
    }
}