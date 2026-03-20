package com.bookstore.dao;

import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.*;
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
}