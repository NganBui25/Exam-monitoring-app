
package src.Client.view;

// Import logic DAO của bạn
import src.Server.dao.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Giao diện Đăng ký (Đã thiết kế lại)
 * Tương thích với giao diện Login mới và tích hợp UserDAO
 */
public class Register extends javax.swing.JFrame {

    // Khai báo các biến UI
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;
    private JButton btnBack; // Đổi tên từ loginLink

    public Register() {
        Components();
    }

    private void Components() {

        // --- 1. Cài đặt Cửa sổ (Frame) ---
        setTitle("Đăng ký Tài khoản");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(450, 550)); // Kích thước
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

        // --- 2. Tiêu đề ---
        JLabel titleLabel = new JLabel("Tạo tài khoản Giám thị");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(titleLabel, gbc);

        // --- 3. Nhãn "Tài khoản" ---
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0); // Reset padding
        mainPanel.add(createLabel("Username:"), gbc);

        // --- 4. Ô "Tài khoản" ---
        txtUsername = new JTextField();
        styleField(txtUsername);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0); // Khoảng cách dưới
        mainPanel.add(txtUsername, gbc);

        // --- 5. Nhãn "Mật khẩu" ---
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(createLabel("Password:"), gbc);

        // --- 6. Ô "Mật khẩu" ---
        txtPassword = new JPasswordField();
        styleField(txtPassword);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(txtPassword, gbc);

        // --- 7. Nhãn "Nhập lại Mật khẩu" ---
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(createLabel("Confirm Password:"), gbc);

        // --- 8. Ô "Nhập lại Mật khẩu" ---
        txtConfirmPassword = new JPasswordField();
        styleField(txtConfirmPassword);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(txtConfirmPassword, gbc);

        // --- 9. Nút "Đăng ký" ---
        btnRegister = new JButton("Đăng ký");
        stylePrimaryButton(btnRegister);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(btnRegister, gbc);

        // --- 10. Link "Quay lại Đăng nhập" ---
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        switchPanel.setBackground(Color.WHITE);
        
        JLabel switchLabel = new JLabel("Đã có tài khoản?");
        switchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        switchLabel.setForeground(Color.GRAY);
        
        btnBack = createLinkButton("Đăng nhập ngay"); // Nút "Quay lại"
        
        switchPanel.add(switchLabel);
        switchPanel.add(btnBack);
        
        gbc.gridy++;
        mainPanel.add(switchPanel, gbc);
        
        // Đẩy lên trên
        gbc.gridy++;
        gbc.weighty = 1.0;
        mainPanel.add(Box.createVerticalGlue(), gbc);

        // --- 11. Gán sự kiện ---
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        pack(); // Tối ưu kích thước
    }

    // --- CÁC HÀM TIỆN ÍCH (HELPER) ĐỂ TÙY CHỈNH UI ---

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }
    
    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(200, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(new Color(0, 120, 215)); // Màu xanh dương
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(null);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(0, 120, 215)); // Màu link
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

    /**
     * Hàm này được lấy trực tiếp từ code của bạn,
     * chỉ đổi tên biến JPasswordField.
     */
    private void btnRegisterActionPerformed(ActionEvent evt) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        // 1. Kiểm tra thông tin trống
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Kiểm tra mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không khớp. Vui lòng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 3. Gọi hàm register từ UserDAO
        Integer newUserId = UserDAO.register(username, password);
        
        if (newUserId != null && newUserId > 0) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập lại.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            // Quay lại màn hình đăng nhập
            new Login().setVisible(true);
            this.dispose();
        } else if (newUserId != null && newUserId == -1) {
            JOptionPane.showMessageDialog(this, "Username '" + username + "' đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Đã có lỗi xảy ra trong quá trình đăng ký.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {
        new Login().setVisible(true);
        this.dispose();
    }
    
    // --- Hàm Main (Để chạy test nếu cần) ---
    public static void main(String args[]) {
        // Set Look and Feel (Nimbus)
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Register.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        java.awt.EventQueue.invokeLater(() -> new Register().setVisible(true));
    }
}