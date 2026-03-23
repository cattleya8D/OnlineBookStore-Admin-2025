package com.bookstore.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("网上书店订单管理系统 - 后台管理端");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // CardLayout 管理所有面板
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 添加所有面板（必须和菜单一致）
        mainPanel.add(new BookPanel(), "bookPanel");
        mainPanel.add(new CustomerPanel(), "customerPanel");
        mainPanel.add(new OrderPanel(), "orderPanel");
        mainPanel.add(new ShippingPanel(), "shippingPanel");
        mainPanel.add(new ReviewPanel(), "reviewPanel");
        mainPanel.add(new ReportPanel(), "reportPanel");   // ← 统计报表

        add(mainPanel, BorderLayout.CENTER);

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu manageMenu = new JMenu("管理");
        JMenuItem bookItem = new JMenuItem("图书管理");
        JMenuItem customerItem = new JMenuItem("客户管理");
        JMenuItem orderItem = new JMenuItem("订单管理");
        JMenuItem shippingItem = new JMenuItem("物流管理");
        JMenuItem reviewItem = new JMenuItem("评价管理");
        JMenuItem reportItem = new JMenuItem("统计报表");

        bookItem.addActionListener(e -> cardLayout.show(mainPanel, "bookPanel"));
        customerItem.addActionListener(e -> cardLayout.show(mainPanel, "customerPanel"));
        orderItem.addActionListener(e -> cardLayout.show(mainPanel, "orderPanel"));
        shippingItem.addActionListener(e -> cardLayout.show(mainPanel, "shippingPanel"));
        reviewItem.addActionListener(e -> cardLayout.show(mainPanel, "reviewPanel"));
        reportItem.addActionListener(e -> cardLayout.show(mainPanel, "reportPanel"));

        manageMenu.add(bookItem);
        manageMenu.add(customerItem);
        manageMenu.add(orderItem);
        manageMenu.add(shippingItem);
        manageMenu.add(reviewItem);
        manageMenu.add(reportItem);
        menuBar.add(manageMenu);

        setJMenuBar(menuBar);

        // 显示登录对话框
        showLoginDialog();
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "用户登录", true);
        loginDialog.setSize(350, 250);
        loginDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("用户名:"));
        JTextField usernameField = new JTextField("admin1");
        panel.add(usernameField);

        panel.add(new JLabel("密码:"));
        JPasswordField passwordField = new JPasswordField("123456");
        panel.add(passwordField);

        panel.add(new JLabel("角色:"));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "operator", "warehouse", "support"});
        panel.add(roleCombo);

        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.equals("admin1") && password.equals("123456")) {
                JOptionPane.showMessageDialog(this, "登录成功！欢迎使用系统");
                loginDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        panel.add(buttonPanel);

        loginDialog.add(panel);
        loginDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}