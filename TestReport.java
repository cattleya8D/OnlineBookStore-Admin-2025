package com.bookstore.test;

import com.bookstore.service.OrderService;

public class TestReport {
    public static void main(String[] args) {
        OrderService s = new OrderService();

        System.out.println("=== 统计报表测试 ===");
        System.out.println("月销售额记录数: " + s.getMonthlySales().size() + " 条");
        System.out.println("畅销书Top10: " + s.getTopSellingBooks(10).size() + " 本");
        System.out.println("退货率统计图书数: " + s.getReturnRateByBook().size() + " 本");
        System.out.println("✅ 所有报表方法调用成功！");
    }
}