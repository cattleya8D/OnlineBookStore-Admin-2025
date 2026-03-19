// src/com/bookstore/entity/Order.java
package com.bookstore.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Order(
        Long orderId,
        Long customerId,
        Long userId,           // 操作员
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        String status,         // "待付款","已付款","已发货","已完成","已取消"
        String paymentMethod,
        String notes
) {}