package com.bookstore.service;

import com.bookstore.dao.OrderDao;
import com.bookstore.dao.ReviewDao;
import com.bookstore.entity.Order;
import com.bookstore.entity.Review;

import java.util.List;

public class ReviewService {

    private final ReviewDao reviewDao = new ReviewDao();
    private final OrderDao orderDao = new OrderDao();  // 用于校验订单状态

    public boolean addReview(Review review) {
        // 业务校验：订单必须是“已签收”或“已完成”
        Order order = orderDao.getOrderById(review.orderId());
        if (order == null || (!"已签收".equals(order.status()) && !"已完成".equals(order.status()))) {
            System.out.println("只有已签收或已完成的订单才能评价");
            return false;
        }

        // 还可以加：同一个客户对同一订单的同一本书只能评一次（课设可简化不做）

        return reviewDao.addReview(review);
    }

    public List<Review> getReviewsByOrder(long orderId) {
        return reviewDao.getReviewsByOrderId(orderId);
    }

    public List<Review> getReviewsByBook(long bookId) {
        return reviewDao.getReviewsByBookId(bookId);
    }
}