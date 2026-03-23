package com.bookstore.dao;

import com.bookstore.entity.OrderDetail;
import com.bookstore.entity.ShippingRecord;
import com.bookstore.util.DbUtil;
import com.bookstore.entity.Order;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    // 创建订单主表，返回新订单ID（用于插入明细）
    public long createOrder(long customerId, long userId, BigDecimal totalAmount, String status, String paymentMethod, String notes) {
        String sql = "INSERT INTO Orders (customer_id, user_id, order_date, total_amount, status, payment_method, notes) " +
                "VALUES (?, ?, NOW(), ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, customerId);
            ps.setLong(2, userId);
            ps.setBigDecimal(3, totalAmount);
            ps.setString(4, status);
            ps.setString(5, paymentMethod);
            ps.setString(6, notes);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 插入一条订单明细
    public boolean addOrderDetail(long orderId, long bookId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        String sql = "INSERT INTO OrderDetails (order_id, book_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, bookId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.setBigDecimal(5, subtotal);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 扣减库存（单本书）
    public boolean deductStock(long bookId, int quantity) {
        String sql = "UPDATE Books SET stock = stock - ? WHERE book_id = ? AND stock >= ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, bookId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新订单状态
    public boolean updateOrderStatus(long orderId, String newStatus) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 恢复库存（单本书 + 数量）
    public boolean restoreStock(long bookId, int quantity) {
        String sql = "UPDATE Books SET stock = stock + ? WHERE book_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据订单ID查询所有明细（用于取消时恢复库存）
    public List<OrderDetail> getOrderDetails(long orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetails WHERE order_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderDetail(
                            rs.getLong("detail_id"),
                            rs.getLong("order_id"),
                            rs.getLong("book_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price"),
                            rs.getBigDecimal("subtotal")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 创建物流记录（默认待发货）
    public boolean createShipping(long orderId, String trackingNumber, String company) {
        String sql = "INSERT INTO ShippingRecords (order_id, tracking_number, ship_date, status, company) " +
                "VALUES (?, ?, NOW(), 'PENDING', ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, trackingNumber);
            ps.setString(3, company);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 查询物流记录（用于界面显示）
    public ShippingRecord getShippingByOrderId(long orderId) {
        String sql = "SELECT * FROM ShippingRecords WHERE order_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ShippingRecord(
                            rs.getLong("shipping_id"),
                            rs.getLong("order_id"),
                            rs.getString("tracking_number"),
                            rs.getTimestamp("ship_date").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getString("company"),
                            rs.getString("notes")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据订单ID查询订单主表信息
    public Order getOrderById(long orderId) {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Order(
                            rs.getLong("order_id"),
                            rs.getLong("customer_id"),
                            rs.getLong("user_id"),
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("status"),
                            rs.getString("payment_method"),
                            rs.getString("notes")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // 没找到返回 null
    }

    public List<Order> searchOrders(String keyword, String status, int page, int pageSize) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.* FROM Orders o LEFT JOIN Customers c ON o.customer_id = c.customer_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (o.order_id LIKE ? OR c.name LIKE ? OR c.phone LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null && !status.equals("全部")) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY o.order_date DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countOrders(String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Orders o LEFT JOIN Customers c ON o.customer_id = c.customer_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (o.order_id LIKE ? OR c.name LIKE ? OR c.phone LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null && !status.equals("全部")) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 更新物流状态（例如：已揽件 → 运输中 → 已签收）
     * @param orderId 订单ID（物流记录以订单ID唯一）
     * @param newStatus 新状态（必须是 ShippingRecords.status 允许的值）
     * @return 是否更新成功
     */
    public boolean updateShippingStatus(long orderId, String newStatus) {
        String sql = "UPDATE ShippingRecords SET status = ? WHERE order_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, orderId);
            System.out.println("【DAO】执行更新: orderId=" + orderId + ", status=" + newStatus);
            int rows = ps.executeUpdate();
            System.out.println("【DAO】影响行数: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}