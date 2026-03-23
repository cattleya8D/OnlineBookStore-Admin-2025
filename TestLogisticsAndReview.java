package com.bookstore.test;

import com.bookstore.service.OrderService;
import com.bookstore.service.ReviewService;
import com.bookstore.entity.Review;

import java.time.LocalDateTime;

public class TestLogisticsAndReview {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        ReviewService reviewService = new ReviewService();

        long testOrderId = 54L;  // 改成你数据库里已发货的订单ID（53或54都可以）

        System.out.println("=== 测试物流状态更新（使用英文code） ===");
        boolean updated1 = orderService.updateShipping(testOrderId, "PICKED");
        System.out.println("更新为运输中: " + (updated1 ? "成功" : "失败"));

        boolean updated2 = orderService.updateShipping(testOrderId, "DELIVERED");
        System.out.println("更新为已签收: " + (updated2 ? "成功" : "失败"));

        System.out.println("\n=== 测试评价（签收后才能评） ===");
        Review review = new Review(
                0L,                                 // reviewId让数据库自增
                testOrderId,
                1L,                                 // 改成订单里实际的图书ID
                10L,                                // 改成实际客户ID
                5,
                "这本书质量很好，物流也很快，强烈推荐！",
                LocalDateTime.now()
        );
        boolean reviewed = reviewService.addReview(review);
        System.out.println("添加评价: " + (reviewed ? "成功" : "失败"));

        // 查看该订单的所有评价
        System.out.println("\n该订单的评价：");
        reviewService.getReviewsByOrderId(testOrderId).forEach(r ->
                System.out.println("评分: " + r.rating() + " | 评论: " + r.comment())
        );
    }
}