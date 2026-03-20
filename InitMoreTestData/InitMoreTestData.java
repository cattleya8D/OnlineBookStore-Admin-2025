package com.bookstore.InitMoreTestData;

import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InitMoreTestData {

    private static final Random random = new Random();

    public static void main(String[] args) {
        try (Connection conn = DbUtil.getConnection()) {
            System.out.println("开始批量插入更多测试数据...");

            conn.setAutoCommit(false);

            try {
                insertCategoriesIfNeeded(conn);
                insertBooks(conn);
                insertCustomers(conn);
                insertOrdersAndDetailsAndShipping(conn);
                insertReviews(conn);

                conn.commit();
                System.out.println("所有测试数据插入完毕！请在 Workbench 检查表记录数量。");

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

    // 获取分类ID（按名称查）
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
        String sql = "INSERT INTO Books (isbn, title, author, publisher, publish_year, price, stock, category_id, description) " +
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

            ps.executeBatch();
            System.out.println("30本图书插入完成");
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

    // 3. 插入15个客户（如果已存在可跳过或清空）
    private static void insertCustomers(Connection conn) throws SQLException {
        String sql = "INSERT INTO Customers (name, phone, address, email) VALUES (?, ?, ?, ?)";
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
            ps.executeBatch();
            System.out.println("15个客户插入完成");
        }
    }

    // 辅助方法：获取所有真实存在的ID
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
        return BigDecimal.valueOf(50.00); // 兜底
    }

    // 4. 插入订单 + 明细 + 物流
    private static void insertOrdersAndDetailsAndShipping(Connection conn) throws SQLException {
        String sqlOrder = "INSERT INTO Orders (customer_id, user_id, order_date, total_amount, status, payment_method, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO OrderDetails (order_id, book_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlShipping = "INSERT INTO ShippingRecords (order_id, tracking_number, ship_date, status, company) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
             PreparedStatement psShipping = conn.prepareStatement(sqlShipping)) {

            String[] statuses = {"待付款", "已付款", "已发货", "已完成", "已取消"};
            String[] payments = {"支付宝", "微信支付", "银行卡", "货到付款"};
            String[] companies = {"顺丰速运", "京东物流", "圆通快递", "中通快递", "韵达快递"};

            List<Long> customerIds = getAllCustomerIds(conn);
            List<Long> bookIds = getAllBookIds(conn);

            if (customerIds.isEmpty()) throw new SQLException("Customers表为空，无法插入订单");
            if (bookIds.isEmpty()) throw new SQLException("Books表为空，无法插入明细");

            for (int i = 0; i < 20; i++) {
                long customerId = customerIds.get(random.nextInt(customerIds.size()));
                long userId = random.nextInt(4) + 1; // 假设用户ID 1~4
                LocalDateTime orderDate = LocalDateTime.now().minusDays(random.nextInt(60) + 1);
                String status = statuses[random.nextInt(5)];

                psOrder.setLong(1, customerId);
                psOrder.setLong(2, userId);
                psOrder.setObject(3, orderDate);
                psOrder.setBigDecimal(4, BigDecimal.ZERO); // 先占位
                psOrder.setString(5, status);
                psOrder.setString(6, payments[random.nextInt(4)]);
                psOrder.setString(7, status.equals("已取消") ? "用户主动取消" : "正常订单");
                psOrder.execute();

                long orderId = getLastInsertId(psOrder);
                if (orderId == -1) throw new SQLException("无法获取新订单ID");

                BigDecimal total = BigDecimal.ZERO;
                int detailCount = 2 + random.nextInt(3);
                for (int j = 0; j < detailCount; j++) {
                    long bookId = bookIds.get(random.nextInt(bookIds.size()));
                    int qty = 1 + random.nextInt(5);
                    BigDecimal unitPrice = getBookPrice(conn, bookId);
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

                    psDetail.setLong(1, orderId);
                    psDetail.setLong(2, bookId);
                    psDetail.setInt(3, qty);
                    psDetail.setBigDecimal(4, unitPrice);
                    psDetail.setBigDecimal(5, subtotal);
                    psDetail.addBatch();

                    total = total.add(subtotal);
                }
                psDetail.executeBatch();

                // 更新订单总金额
                try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE Orders SET total_amount = ? WHERE order_id = ?")) {
                    psUpdate.setBigDecimal(1, total);
                    psUpdate.setLong(2, orderId);
                    psUpdate.executeUpdate();
                }

                if (status.equals("已发货") || status.equals("已完成")) {
                    psShipping.setLong(1, orderId);
                    psShipping.setString(2, "SF" + (100000 + random.nextInt(900000)));
                    psShipping.setObject(3, orderDate.plusDays(1 + random.nextInt(5)));
                    psShipping.setString(4, status.equals("已完成") ? "已签收" : "运输中");
                    psShipping.setString(5, companies[random.nextInt(5)]);
                    psShipping.execute();
                }
            }
            System.out.println("20条订单、对应明细及物流插入完成");
        }
    }

    // 5. 插入评价（只针对已完成订单）
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

    private static long getLastInsertId(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getLong(1);
        }
        return -1;
    }
}