package com.bookstore.test;

import com.bookstore.service.OrderService;
import com.bookstore.service.ReviewService;
import com.bookstore.entity.Review;

public class TestLogisticsAndReview {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        ReviewService reviewService = new ReviewService();

        long testOrderId = 28L;  // 改成你已发货的订单ID

        System.out.println("=== 测试物流状态更新 ===");
        boolean updated1 = orderService.updateShipping(testOrderId, "运输中");
        System.out.println("更新为运输中: " + (updated1 ? "成功" : "失败"));

        boolean updated2 = orderService.updateShipping(testOrderId, "已签收");
        System.out.println("更新为已签收: " + (updated2 ? "成功" : "失败"));

        System.out.println("\n=== 测试评价（签收后） ===");
        Review review = new Review(
                null,
                testOrderId,
                125L,           // 改成订单里的一本书ID
                46L,           // 改成客户ID
                5,
                "这本书质量很好，物流也很快，强烈推荐！",
                null
        );
        boolean reviewed = reviewService.addReview(review);
        System.out.println("添加评价: " + (reviewed ? "成功" : "失败"));

        // 查看评价
        System.out.println("\n该订单的评价：");
        reviewService.getReviewsByOrder(testOrderId).forEach(r ->
                System.out.println("评分: " + r.rating() + " | 评论: " + r.commentText()));
    }
}