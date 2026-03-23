package com.bookstore.gui;

import com.bookstore.entity.Review;
import com.bookstore.service.ReviewService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReviewPanel extends JPanel {

    private final ReviewService reviewService = new ReviewService();
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton prevBtn, nextBtn;
    private JLabel pageLabel;
    private int currentPage = 1;
    private final int pageSize = 10;
    private String currentKeyword = null;

    public ReviewPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolBar.add(new JLabel("搜索（图书名/订单号）："));
        searchField = new JTextField(20);
        toolBar.add(searchField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> loadReviews());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadReviews());
        toolBar.add(refreshBtn);

        JButton viewBtn = new JButton("查看详情");
        viewBtn.addActionListener(e -> showReviewDetails());
        toolBar.add(viewBtn);

        add(toolBar, BorderLayout.NORTH);

        // 表格
        String[] columns = {"评价ID", "订单ID", "图书ID", "客户ID", "评分", "评价时间"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 分页栏
        JPanel pageBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        prevBtn = new JButton("上一页");
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadReviews();
            }
        });
        nextBtn = new JButton("下一页");
        nextBtn.addActionListener(e -> {
            currentPage++;
            loadReviews();
        });
        pageLabel = new JLabel("第 1 页 / 共 ? 页");
        pageBar.add(prevBtn);
        pageBar.add(pageLabel);
        pageBar.add(nextBtn);
        add(pageBar, BorderLayout.SOUTH);

        // 首次加载
        loadReviews();
    }

    private void loadReviews() {
        model.setRowCount(0);
        List<Review> reviews = reviewService.getReviewsByKeyword(currentKeyword, currentPage, pageSize); // 假设有这个方法
        for (Review r : reviews) {
            model.addRow(new Object[]{
                    r.reviewId(),
                    r.orderId(),
                    r.bookId(),
                    r.customerId(),
                    r.rating(),
                    r.reviewDate()
            });
        }

        int total = reviewService.getReviewCount(currentKeyword);
        int totalPages = (total + pageSize - 1) / pageSize;
        pageLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");
        prevBtn.setEnabled(currentPage > 1);
        nextBtn.setEnabled(currentPage < totalPages);
    }

    private void showReviewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行评价！");
            return;
        }

        long reviewId = (Long) model.getValueAt(row, 0);
        // 简单弹窗显示详情（可扩展）
        JOptionPane.showMessageDialog(this, "评价详情待完善（reviewId=" + reviewId + "）");
    }
}