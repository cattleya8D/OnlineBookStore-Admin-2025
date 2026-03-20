package com.bookstore.test;

import com.bookstore.service.OrderService;
import com.bookstore.util.DbUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestOrderCreate {
    private static final Random rand = new Random();

    public static void main(String[] args) {
        OrderService service = new OrderService();

        // 动态获取存在的 book_id
        Map<Long, Integer> items = new HashMap<>();
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT book_id, stock FROM Books WHERE stock > 0 LIMIT 5")) {

            while (rs.next()) {
                long bookId = rs.getLong("book_id");
                int stock = rs.getInt("stock");
                int qty = 1 + rand.nextInt(Math.min(3, stock));  // 买1~3本，不超库存
                if (qty > 0) items.put(bookId, qty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (items.isEmpty()) {
            System.out.println("数据库无可用图书（stock>0），无法测试订单创建");
            return;
        }

        System.out.println("测试购物车内容:");
        items.forEach((id, q) -> System.out.println("bookId=" + id + ", 数量=" + q));

        long orderId = service.createOrder(
                55L,  // 改成你数据库里存在的 customer_id
                1L,  // user_id
                items,
                "支付宝",
                "测试事务订单 - 自动选书"
        );

        if (orderId > 0) {
            System.out.println("订单创建成功！订单ID: " + orderId);
            System.out.println("请检查：");
            System.out.println("1. Orders 表新增记录");
            System.out.println("2. OrderDetails 有多条明细");
            System.out.println("3. Books 表对应书的 stock 已减少");
        } else {
            System.out.println("订单创建失败！");
        }
    }
}