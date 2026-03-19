// src/com/bookstore/entity/Book.java
package com.bookstore.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Book(
        Long bookId,
        String isbn,
        String title,
        String author,
        String publisher,
        Integer publishYear,
        BigDecimal price,
        Integer stock,
        Long categoryId,
        String description,
        String coverUrl,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {}