package com.bookstore.service;

import com.bookstore.dao.BookDao;
import com.bookstore.dao.OrderDao;
import com.bookstore.entity.Book;
import com.bookstore.util.DbUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
}