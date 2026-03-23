package com.bookstore.service;

import com.bookstore.dao.OrderDao;
import com.bookstore.dao.ReviewDao;
import com.bookstore.entity.Order;
import com.bookstore.entity.Review;

import java.util.List;

public class ReviewService {

    private final ReviewDao reviewDao = new ReviewDao();
    private final OrderDao orderDao = new OrderDao();

    public boolean addReview(Review review) {
        Order order = orderDao.getOrderById(review.orderId());
        if (order == null || (!"已签收".equals(order.status()) && !"已完成".equals(order.status()))) {
            System.out.println("只有已签收或已完成的订单才能评价");
            return false;
        }
        return reviewDao.addReview(review);
    }

    public List<Review> getReviewsByOrder(long orderId) {
        return reviewDao.getReviewsByOrderId(orderId);
    }

    public List<Review> getReviewsByBook(long bookId) {
        return reviewDao.getReviewsByBookId(bookId);
    }

    /**
     * 分页 + 关键字搜索评价
     */
    public List<Review> getReviewsByKeyword(String keyword, int page, int pageSize) {
        return reviewDao.getReviewsByKeyword(keyword, page, pageSize);
    }

    public int getReviewCount(String keyword) {
        return reviewDao.getReviewCount(keyword);
    }

    /**
     * 【新增】根据评价ID获取完整评价（含文字评论）
     */
    public Review getReviewById(long reviewId) {
        return reviewDao.getReviewById(reviewId);
    }

    /**
     * 兼容测试类调用的方法名（getReviewsByOrderId）
     */
    public List<Review> getReviewsByOrderId(long orderId) {
        return getReviewsByOrder(orderId);
    }
}