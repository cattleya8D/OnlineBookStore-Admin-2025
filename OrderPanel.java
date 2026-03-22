package com.bookstore.gui;

import com.bookstore.entity.Order;
import com.bookstore.entity.OrderDetail;
import com.bookstore.service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderPanel extends JPanel {

    private final OrderService orderService = new OrderService();
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> statusCombo;
    private JButton prevBtn, nextBtn;
    private JLabel pageLabel;
    private int currentPage = 1;
    private final int pageSize = 10;
    private String currentKeyword = null;
    private String currentStatus = null;

    public OrderPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolBar.add(new JLabel("搜索（订单号/客户姓名）："));
        searchField = new JTextField(20);
        toolBar.add(searchField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> searchOrders());
        toolBar.add(searchBtn);

        toolBar.add(new JLabel("状态筛选："));
        statusCombo = new JComboBox<>(new String[]{"全部", "待付款", "已付款", "已发货", "已完成", "已取消"});
        statusCombo.addActionListener(e -> searchOrders());
        toolBar.add(statusCombo);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadOrders());
        toolBar.add(refreshBtn);

        JButton detailBtn = new JButton("查看明细");
        detailBtn.addActionListener(e -> showOrderDetails());
        toolBar.add(detailBtn);

        // 状态变更按钮（根据选中订单状态动态启用）
        JButton payBtn = new JButton("付款");
        payBtn.addActionListener(e -> changeStatus("已付款"));
        JButton shipBtn = new JButton("发货");
        shipBtn.addActionListener(e -> showShipDialog());
        JButton completeBtn = new JButton("完成");
        completeBtn.addActionListener(e -> changeStatus("已完成"));
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> cancelOrder());

        toolBar.add(payBtn);
        toolBar.add(shipBtn);
        toolBar.add(completeBtn);
        toolBar.add(cancelBtn);

        add(toolBar, BorderLayout.NORTH);

        // 表格
        String[] columns = {"订单ID", "客户ID", "操作员ID", "下单时间", "总金额", "状态", "支付方式"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates(payBtn, shipBtn, completeBtn, cancelBtn));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 分页栏
        JPanel pageBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        prevBtn = new JButton("上一页");
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadOrders();
            }
        });
        nextBtn = new JButton("下一页");
        nextBtn.addActionListener(e -> {
            currentPage++;
            loadOrders();
        });
        pageLabel = new JLabel("第 1 页 / 共 ? 页");
        pageBar.add(prevBtn);
        pageBar.add(pageLabel);
        pageBar.add(nextBtn);
        add(pageBar, BorderLayout.SOUTH);

        // 首次加载
        loadOrders();
    }

    private void loadOrders() {
        model.setRowCount(0);
        List<Order> orders = orderService.searchOrders(currentKeyword, currentStatus, currentPage, pageSize);
        for (Order o : orders) {
            model.addRow(new Object[]{
                    o.orderId(),
                    o.customerId(),
                    o.userId(),
                    o.orderDate(),
                    o.totalAmount(),
                    o.status(),
                    o.paymentMethod()
            });
        }

        int total = orderService.getOrderCount(currentKeyword, currentStatus);
        int totalPages = (total + pageSize - 1) / pageSize;
        pageLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");
        prevBtn.setEnabled(currentPage > 1);
        nextBtn.setEnabled(currentPage < totalPages);
    }

    private void searchOrders() {
        currentKeyword = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
        String selected = (String) statusCombo.getSelectedItem();
        currentStatus = selected.equals("全部") ? null : selected;
        currentPage = 1;
        loadOrders();
    }

    private void updateButtonStates(JButton payBtn, JButton shipBtn, JButton completeBtn, JButton cancelBtn) {
        int row = table.getSelectedRow();
        if (row == -1) {
            payBtn.setEnabled(false);
            shipBtn.setEnabled(false);
            completeBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            return;
        }

        String status = (String) model.getValueAt(row, 5);
        payBtn.setEnabled("待付款".equals(status));
        shipBtn.setEnabled("已付款".equals(status));
        completeBtn.setEnabled("已发货".equals(status));
        cancelBtn.setEnabled("待付款".equals(status) || "已付款".equals(status));
    }

    private void changeStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行订单！");
            return;
        }

        long orderId = (Long) model.getValueAt(row, 0);
        if (orderService.changeOrderStatus(orderId, newStatus)) {
            JOptionPane.showMessageDialog(this, "状态更新成功！");
            loadOrders();
        } else {
            JOptionPane.showMessageDialog(this, "状态更新失败！");
        }
    }

    private void showShipDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行订单！");
            return;
        }

        long orderId = (Long) model.getValueAt(row, 0);
        String trackingNumber = JOptionPane.showInputDialog(this, "输入物流单号：", "发货操作", JOptionPane.QUESTION_MESSAGE);
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) return;

        String company = JOptionPane.showInputDialog(this, "输入物流公司：", "发货操作", JOptionPane.QUESTION_MESSAGE);
        if (company == null || company.trim().isEmpty()) return;

        if (orderService.shipOrder(orderId, trackingNumber.trim(), company.trim())) {
            JOptionPane.showMessageDialog(this, "发货成功！");
            loadOrders();
        } else {
            JOptionPane.showMessageDialog(this, "发货失败！");
        }
    }

    private void cancelOrder() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行订单！");
            return;
        }

        long orderId = (Long) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确认取消订单 ID=" + orderId + "？库存将恢复！", "取消确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (orderService.cancelOrder(orderId)) {
                JOptionPane.showMessageDialog(this, "取消成功！库存已恢复");
                loadOrders();
            } else {
                JOptionPane.showMessageDialog(this, "取消失败！");
            }
        }
    }

    private void showOrderDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行订单！");
            return;
        }

        long orderId = (Long) model.getValueAt(row, 0);
        List<OrderDetail> details = orderService.getOrderDetails(orderId); // 假设 OrderService 有这个方法

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "订单明细 - ID=" + orderId, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"明细ID", "图书ID", "数量", "单价", "小计"};
        DefaultTableModel detailModel = new DefaultTableModel(columns, 0);
        JTable detailTable = new JTable(detailModel);
        for (OrderDetail d : details) {
            detailModel.addRow(new Object[]{
                    d.detailId(),
                    d.bookId(),
                    d.quantity(),
                    d.unitPrice(),
                    d.subtotal()
            });
        }

        JScrollPane scrollPane = new JScrollPane(detailTable);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
}