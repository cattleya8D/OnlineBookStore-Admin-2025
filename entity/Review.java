// src/com/bookstore/entity/Review.java
package com.bookstore.entity;

import java.time.LocalDateTime;

public record Review(
        Long reviewId,
        Long orderId,
        Long bookId,
        Long customerId,
        Integer rating,        // 1-5
        String commentText,
        LocalDateTime reviewDate
) {}