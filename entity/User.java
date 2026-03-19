// src/com/bookstore/entity/User.java
package com.bookstore.entity;

import java.time.LocalDateTime;

public record User(
        Long userId,
        String username,
        String passwordHash,
        String role,           // "admin","operator","warehouse","support"
        String realName,
        String phone,
        String email,
        LocalDateTime createTime
) {}