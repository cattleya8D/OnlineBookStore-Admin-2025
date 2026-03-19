// src/com/bookstore/entity/ShippingRecord.java
package com.bookstore.entity;

import java.time.LocalDateTime;

public record ShippingRecord(
        Long shippingId,
        Long orderId,
        String trackingNumber,
        LocalDateTime shipDate,
        String status,         // "待发货","已揽件","运输中","已签收"
        String company,
        String notes
) {}