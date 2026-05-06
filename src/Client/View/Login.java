package Client.View;

import Client.Controller.AuthenticationController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Box;

public class Login extends javax.swing.JFrame {

    private JPasswordField password;
    private JTextField username;
    private JButton login;
    private JButton register; 

    public Login() {
        Components();
    }

    private void Components() {

        setTitle("Đăng nhập Giám thị");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Đăng nhập Giám thị");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.insets = new Insets(0, 0, 30, 0); // Khoảng cách dưới
        mainPanel.add(titleLabel, gbc);

        // Nhãn "Tài khoản" 
        gbc.gridy++; // Tăng y
        gbc.insets = new Insets(0, 0, 5, 0); // Reset padding
        mainPanel.add(createLabel("Tài khoản:"), gbc);

        // Ô "Tài khoản" 
        username = new JTextField("teacher123"); // Giữ giá trị mặc định của bạn
        styleField(username);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0); // Khoảng cách dưới
        mainPanel.add(username, gbc);

        // Nhãn "Mật khẩu" 
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(createLabel("Mật khẩu:"), gbc);

        // Ô "Mật khẩu"
        password = new JPasswordField("teacher123"); // Giữ giá trị mặc định của bạn
        styleField(password);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0); // Khoảng cách dưới
        mainPanel.add(password, gbc);

        // Nút "Đăng nhập" 
        login = new JButton("Đăng nhập");
        stylePrimaryButton(login);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(login, gbc);

        // Link "Chuyển sang Đăng ký"
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        switchPanel.setBackground(Color.WHITE);
        
        JLabel switchLabel = new JLabel("Chưa có tài khoản?");
        switchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        switchLabel.setForeground(Color.GRAY);
        
        register = createLinkButton("Đăng ký ngay"); 
        
        switchPanel.add(switchLabel);
        switchPanel.add(register);
        
        gbc.gridy++;
        mainPanel.add(switchPanel, gbc);
        
        // Thêm một panel rỗng để đẩy mọi thứ lên trên
        gbc.gridy++;
        gbc.weighty = 1.0; // Đây là mấu chốt
        mainPanel.add(Box.createVerticalGlue(), gbc);

        login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginActionPerformed(evt);
            }
        });
        
        register.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerActionPerformed(evt);
            }
        });

        pack(); // Tối ưu kích thước
    }

    // Tạo nhãn (Label)
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }
    
    // Tùy chỉnh ô nhập liệu
    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(200, 45));
        // Thêm padding bên trong
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    // Tùy chỉnh nút chính (Primary Button)
    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(new Color(0, 120, 215)); // Màu xanh dương
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // Tùy chỉnh nút dạng link (Link Button)
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(0, 120, 215)); // Màu link
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Xóa nền và viền
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }
    
    private void loginActionPerformed(java.awt.event.ActionEvent evt) {
        if (AuthenticationController.handleLoginAction(username.getText(), new String(password.getPassword())))
            this.dispose();
    }

    private void registerActionPerformed(java.awt.event.ActionEvent evt) {
        new Register().setVisible(true); // Sẽ mở Register.java mới
        this.dispose();
    }

}