package com.bookstore.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("网上书店订单管理系统 - 后台管理端");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // 管理菜单
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

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "网上书店订单管理系统 v1.0\nJavaSE + Swing + JDBC + MySQL\n第二周完成核心业务，第三周实现桌面GUI"));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // 主内容区 - CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 占位面板（后面每天填充一个）
        mainPanel.add(new JLabel("图书管理面板（待实现）", SwingConstants.CENTER), "bookPanel");
        mainPanel.add(new JLabel("客户管理面板（待实现）", SwingConstants.CENTER), "customerPanel");
        mainPanel.add(new JLabel("订单管理面板（待实现）", SwingConstants.CENTER), "orderPanel");
        mainPanel.add(new JLabel("物流管理面板（待实现）", SwingConstants.CENTER), "shippingPanel");
        mainPanel.add(new JLabel("评价管理面板（待实现）", SwingConstants.CENTER), "reviewPanel");
        mainPanel.add(new JLabel("统计报表面板（待实现）", SwingConstants.CENTER), "reportPanel");

        add(mainPanel, BorderLayout.CENTER);

        // 启动时显示登录对话框
        showLoginDialog();
    }

    // 简单登录对话框（模态窗口）
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "登录 - 网上书店后台", true);
        loginDialog.setSize(400, 300);
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
            String role = (String) roleCombo.getSelectedItem();

            // 简单验证（真实项目应查 Users 表）
            if (username.equals("admin1") && password.equals("123456")) {
                JOptionPane.showMessageDialog(this, "登录成功！角色: " + role);
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