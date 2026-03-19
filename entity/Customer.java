// src/com/bookstore/entity/Customer.java
package com.bookstore.entity;

import java.time.LocalDateTime;

public record Customer(
        Long customerId,
        String name,
        String phone,
        String address,
        String email,
        LocalDateTime registerTime
) {}