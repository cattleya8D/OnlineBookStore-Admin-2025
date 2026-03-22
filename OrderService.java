package com.bookstore.service;

import com.bookstore.entity.OrderDetail;
import com.bookstore.dao.BookDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.entity.Book;
import com.bookstore.entity.ShippingRecord;
import com.bookstore.util.DbUtil;
import com.bookstore.entity.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class OrderService {

    private final OrderDao orderDao = new OrderDao();
    private final BookDao bookDao = new BookDao();

    /**
     * 创建订单（含多本图书明细） - 使用事务保证库存扣减与订单一致
     * @param customerId 客户ID
     * @param userId 操作员ID
     * @param items 订单明细：Map<bookId, quantity>
     * @param paymentMethod 支付方式
     * @param notes 备注
     * @return 新订单ID（失败返回-1）
     */
    public long createOrder(long customerId, long userId, Map<Long, Integer> items, String paymentMethod, String notes) {
        Connection conn = null;
        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);  // 开启事务

            // 1. 计算总金额 & 扣库存（先扣，失败就回滚）
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                long bookId = entry.getKey();
                int qty = entry.getValue();

                Book book = bookDao.getBookById(bookId);
                if (book == null || book.stock() < qty) {
                    throw new SQLException("图书不存在或库存不足: bookId=" + bookId);
                }

                // 扣库存
                if (!orderDao.deductStock(bookId, qty)) {
                    throw new SQLException("扣减库存失败: bookId=" + bookId);
                }

                totalAmount = totalAmount.add(book.price().multiply(BigDecimal.valueOf(qty)));
            }

            // 2. 插入订单主表
            long orderId = orderDao.createOrder(customerId, userId, totalAmount, "待付款", paymentMethod, notes);
            if (orderId == -1) {
                throw new SQLException("插入订单主表失败");
            }

            // 3. 插入明细
            for (Map.Entry<Long, Integer> entry : items.entrySet()) {
                long bookId = entry.getKey();
                int qty = entry.getValue();
                Book book = bookDao.getBookById(bookId);  // 再次获取最新价格（防并发）
                BigDecimal unitPrice = book.price();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

                if (!orderDao.addOrderDetail(orderId, bookId, qty, unitPrice, subtotal)) {
                    throw new SQLException("插入订单明细失败");
                }
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 改变订单状态（带简单校验）
    public boolean changeOrderStatus(long orderId, String newStatus) {
        // 简单状态机校验（可扩展为更严格的）
        String[] validTransitions = {
                "待付款→已付款", "已付款→已发货", "已发货→已完成", "待付款→已取消", "已付款→已取消"
        };
        // 这里可以查当前状态再判断是否允许变更（课设简化先不做）
        return orderDao.updateOrderStatus(orderId, newStatus);
    }

    // 取消订单（事务：改状态 + 恢复库存）
    public boolean cancelOrder(long orderId) {
        Connection conn = null;
        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 检查订单是否存在且可取消（课设简化：假设可取消）
            // 2. 获取所有明细，恢复库存
            List<OrderDetail> details = orderDao.getOrderDetails(orderId);
            for (OrderDetail detail : details) {
                if (!orderDao.restoreStock(detail.bookId(), detail.quantity())) {
                    throw new SQLException("恢复库存失败: bookId=" + detail.bookId());
                }
            }

            // 3. 更新订单状态为“已取消”
            if (!orderDao.updateOrderStatus(orderId, "已取消")) {
                throw new SQLException("更新订单状态失败");
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // 发货操作（改订单状态为已发货 + 创建物流记录）
    public boolean shipOrder(long orderId, String trackingNumber, String company) {
        Connection conn = null;
        try {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 改订单状态为已发货
            if (!orderDao.updateOrderStatus(orderId, "已发货")) {
                throw new SQLException("更新订单状态为已发货失败");
            }

            // 2. 创建物流记录（默认 PENDING）
            if (!orderDao.createShipping(orderId, trackingNumber, company)) {
                throw new SQLException("创建物流记录失败");
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // 更新物流状态（只有“已发货”订单才能更新物流）
    public boolean updateShipping(long orderId, String newStatus) {
        // 先查订单当前状态
        Order order = orderDao.getOrderById(orderId);  // 你需要先在OrderDao加这个getOrderById方法
        if (order == null || !"已发货".equals(order.status())) {
            System.out.println("只能对已发货订单更新物流状态");
            return false;
        }

        // 简单校验：状态只能往前走（已揽件→运输中→已签收）
        ShippingRecord shipping = orderDao.getShippingByOrderId(orderId);
        if (shipping == null) return false;

        String current = shipping.status();
        if ("已签收".equals(current)) {
            System.out.println("订单已签收，不能再改物流状态");
            return false;
        }
        if ("运输中".equals(current) && !"已签收".equals(newStatus)) {
            System.out.println("运输中只能更新为已签收");
            return false;
        }

        return orderDao.updateShippingStatus(orderId, newStatus);
    }

    /**
     * 按月份统计销售额（已完成/已签收订单）
     * @param year 指定年份（如2025），null表示所有年
     * @return Map<月份如"2025-03", 销售额>
     */
    public Map<String, BigDecimal> getMonthlySales(Integer year) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String sql = """
            SELECT DATE_FORMAT(o.order_date, '%Y-%m') AS month, 
                   SUM(o.total_amount) AS total
            FROM Orders o
            WHERE o.status IN ('已完成', '已签收')
            """ + (year != null ? " AND YEAR(o.order_date) = ? " : "") + """
            GROUP BY month
            ORDER BY month
        """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (year != null) ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("month"), rs.getBigDecimal("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 畅销书排行（前N名，按销量降序）
     * @param limit 返回前几名
     * @return List<Map> 包含 book_id, title, total_quantity, total_sales
     */
    public List<Map<String, Object>> getTopSellingBooks(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
            SELECT b.book_id, b.title, SUM(od.quantity) AS total_qty, 
                   SUM(od.subtotal) AS total_sales
            FROM OrderDetails od
            JOIN Books b ON od.book_id = b.book_id
            JOIN Orders o ON od.order_id = o.order_id
            WHERE o.status IN ('已完成', '已签收')
            GROUP BY b.book_id, b.title
            ORDER BY total_qty DESC
            LIMIT ?
        """;

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("book_id", rs.getLong("book_id"));
                    row.put("title", rs.getString("title"));
                    row.put("total_quantity", rs.getInt("total_qty"));
                    row.put("total_sales", rs.getBigDecimal("total_sales"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 整体退货率（已取消订单占比）
     * @return 百分比，如 12.5
     */
    public double getCancelRate() {
        String sqlTotal = "SELECT COUNT(*) FROM Orders";
        String sqlCancel = "SELECT COUNT(*) FROM Orders WHERE status = '已取消'";
        int total = 0, cancel = 0;
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sqlTotal)) {
                if (rs.next()) total = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery(sqlCancel)) {
                if (rs.next()) cancel = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total == 0 ? 0.0 : (double) cancel / total * 100;
    }

    /**
     * 按分类统计销售额（饼图常用）
     */
    public List<Map<String, Object>> getSalesByCategory() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = """
            SELECT bc.category_name, SUM(od.subtotal) AS total_sales
            FROM OrderDetails od
            JOIN Books b ON od.book_id = b.book_id
            JOIN BookCategories bc ON b.category_id = bc.category_id
            JOIN Orders o ON od.order_id = o.order_id
            WHERE o.status IN ('已完成', '已签收')
            GROUP BY bc.category_id, bc.category_name
            ORDER BY total_sales DESC
        """;
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category_name"));
                row.put("sales", rs.getBigDecimal("total_sales"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<OrderDetail> getOrderDetails(long orderId) {
        return orderDao.getOrderDetails(orderId);
    }

    /**
     * 获取所有订单（用于订单管理面板显示，先全量加载 + 内存分页，课设简单版）
     * @return 所有订单列表
     */
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT order_id, customer_id, user_id, order_date, total_amount, status, payment_method, notes " +
                "FROM Orders ORDER BY order_date DESC";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Order(
                        rs.getLong("order_id"),
                        rs.getLong("customer_id"),
                        rs.getLong("user_id"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("status"),
                        rs.getString("payment_method"),
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> searchOrders(String keyword, String status, int page, int pageSize) {
        return orderDao.searchOrders(keyword, status, page, pageSize);
    }

    public int getOrderCount(String keyword, String status) {
        return orderDao.countOrders(keyword, status);
    }
}