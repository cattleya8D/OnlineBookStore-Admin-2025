// src/com/bookstore/entity/BookCategory.java
package com.bookstore.entity;

import java.time.LocalDateTime;

public record BookCategory(
        Long categoryId,
        String categoryName,
        Long parentId,
        String description,
        LocalDateTime createTime
) {}