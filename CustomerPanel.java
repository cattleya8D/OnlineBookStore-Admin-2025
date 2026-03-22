package com.bookstore.gui;

import com.bookstore.entity.Customer;
import com.bookstore.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerService customerService = new CustomerService();
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton prevBtn, nextBtn;
    private JLabel pageLabel;
    private int currentPage = 1;
    private final int pageSize = 10;
    private String currentKeyword = null;

    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolBar.add(new JLabel("搜索（姓名/电话/地址）："));
        searchField = new JTextField(20);
        toolBar.add(searchField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> searchCustomers());
        toolBar.add(searchBtn);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> loadCustomers());
        toolBar.add(refreshBtn);

        JButton addBtn = new JButton("新增客户");
        addBtn.addActionListener(e -> showAddDialog());
        toolBar.add(addBtn);

        JButton editBtn = new JButton("修改选中");
        editBtn.addActionListener(e -> showEditDialog());
        toolBar.add(editBtn);

        JButton deleteBtn = new JButton("删除选中");
        deleteBtn.addActionListener(e -> deleteSelected());
        toolBar.add(deleteBtn);

        add(toolBar, BorderLayout.NORTH);

        // 表格
        String[] columns = {"ID", "姓名", "电话", "地址", "邮箱", "注册时间"};
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
                loadCustomers();
            }
        });
        nextBtn = new JButton("下一页");
        nextBtn.addActionListener(e -> {
            currentPage++;
            loadCustomers();
        });
        pageLabel = new JLabel("第 1 页 / 共 ? 页");
        pageBar.add(prevBtn);
        pageBar.add(pageLabel);
        pageBar.add(nextBtn);
        add(pageBar, BorderLayout.SOUTH);

        // 首次加载
        loadCustomers();
    }

    private void loadCustomers() {
        model.setRowCount(0);
        List<Customer> customers = customerService.searchCustomers(currentKeyword, currentPage, pageSize);
        for (Customer c : customers) {
            model.addRow(new Object[]{
                    c.customerId(),
                    c.name(),
                    c.phone(),
                    c.address(),
                    c.email(),
                    c.registerTime()
            });
        }

        // 更新分页（从 service 获取总页数）
        int totalPages = customerService.getTotalPages(currentKeyword, pageSize);
        pageLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");
        prevBtn.setEnabled(currentPage > 1);
        nextBtn.setEnabled(currentPage < totalPages);
    }

    private void searchCustomers() {
        currentKeyword = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
        currentPage = 1;
        loadCustomers();
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField emailField = new JTextField(20);

        Object[] message = {
                "姓名:", nameField,
                "电话:", phoneField,
                "地址:", addressField,
                "邮箱:", emailField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "新增客户", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Customer customer = new Customer(
                    null,
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    addressField.getText().trim(),
                    emailField.getText().trim(),
                    LocalDateTime.now()
            );
            if (customerService.addCustomer(customer)) {
                JOptionPane.showMessageDialog(this, "新增成功！");
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, "新增失败！");
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行客户！");
            return;
        }

        long customerId = (Long) model.getValueAt(row, 0);
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "客户不存在！");
            return;
        }

        JTextField nameField = new JTextField(customer.name(), 20);
        JTextField phoneField = new JTextField(customer.phone(), 20);
        JTextField addressField = new JTextField(customer.address(), 20);
        JTextField emailField = new JTextField(customer.email(), 20);

        Object[] message = {
                "姓名:", nameField,
                "电话:", phoneField,
                "地址:", addressField,
                "邮箱:", emailField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "修改客户", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Customer updated = new Customer(
                    customer.customerId(),
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    addressField.getText().trim(),
                    emailField.getText().trim(),
                    customer.registerTime()
            );
            if (customerService.updateCustomer(updated)) {
                JOptionPane.showMessageDialog(this, "修改成功！");
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, "修改失败！");
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一行客户！");
            return;
        }

        long customerId = (Long) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除客户 ID=" + customerId + "？\n删除后不可恢复！", "删除确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (customerService.deleteCustomer(customerId)) {
                JOptionPane.showMessageDialog(this, "删除成功！");
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！（可能有外键约束，如存在订单）");
            }
        }
    }
}