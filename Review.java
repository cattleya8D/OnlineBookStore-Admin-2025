package com.bookstore.entity;

import java.time.LocalDateTime;

/**
 * 评价实体（必须包含 comment 字段，否则 .comment() 会报错）
 */
public record Review(
        long reviewId,
        long orderId,
        long bookId,
        long customerId,
        int rating,
        String comment,           // ← 关键字段！文字评价
        LocalDateTime reviewDate
) {}