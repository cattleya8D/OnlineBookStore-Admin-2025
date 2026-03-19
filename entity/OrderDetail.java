// src/com/bookstore/entity/OrderDetail.java
package com.bookstore.entity;

import java.math.BigDecimal;

public record OrderDetail(
        Long detailId,
        Long orderId,
        Long bookId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}