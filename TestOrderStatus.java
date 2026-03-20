package com.bookstore.test;

import com.bookstore.service.OrderService;

public class TestOrderStatus {
    public static void main(String[] args) {
        OrderService service = new OrderService();

        // 先创建一个订单（用昨天的方法，或手动在数据库插入一条待付款订单）
        // 假设你已经有一个 orderId = 1 的待付款订单
        long orderId = 22L;  // 改成你昨天创建成功的订单ID

        System.out.println("测试1: 付款（待付款 → 已付款）");
        boolean paid = service.changeOrderStatus(orderId, "已付款");
        System.out.println("付款操作: " + (paid ? "成功" : "失败"));

        System.out.println("测试2: 发货（已付款 → 已发货）");
        boolean shipped = service.shipOrder(orderId, "SF1234567890", "顺丰速运");
        System.out.println("发货操作: " + (shipped ? "成功" : "失败"));

        System.out.println("测试3: 完成订单（已发货 → 已完成）");
        boolean completed = service.changeOrderStatus(orderId, "已完成");
        System.out.println("完成操作: " + (completed ? "成功" : "失败"));

        // 测试取消（新建一个待付款订单再取消）
        System.out.println("\n测试取消订单（新建一个待付款订单）");
        // 这里你可以先调用昨天的 createOrder 创建一个新订单，然后用新ID测试取消
        // 为简单，假设你手动在数据库插入一条待付款订单，ID为新ID
        long cancelOrderId = 23L; // 改成你要测试的订单ID
        boolean canceled = service.cancelOrder(cancelOrderId);
        System.out.println("取消订单: " + (canceled ? "成功" : "失败"));
        System.out.println("请检查：Books表库存是否恢复 + Orders表状态是否为'已取消'");
    }
}