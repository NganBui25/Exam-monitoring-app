package Client.View;
import Server.DAO.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Register extends javax.swing.JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public Register() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng ký tài khoản");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(450, 350));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font textFont = new Font("Arial", Font.PLAIN, 14);

        mainPanel.add(new JLabel("Username:")).setFont(labelFont);
        txtUsername = new JTextField();
        txtUsername.setFont(textFont);
        mainPanel.add(txtUsername);

        mainPanel.add(new JLabel("Password:")).setFont(labelFont);
        txtPassword = new JPasswordField();
        txtPassword.setFont(textFont);
        mainPanel.add(txtPassword);

        mainPanel.add(new JLabel("Confirm Password:")).setFont(labelFont);
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(textFont);
        mainPanel.add(txtConfirmPassword);
        
        JButton btnBack = new JButton("Quay lại");
        btnBack.setFont(labelFont);
        mainPanel.add(btnBack);

        JButton btnRegister = new JButton("Đăng ký");
        btnRegister.setFont(labelFont);
        mainPanel.add(btnRegister);

        // --- Xử lý sự kiện ---
        btnRegister.addActionListener(e -> btnRegisterActionPerformed(e));
        
        btnBack.addActionListener(e -> {
            new Login().setVisible(true);
            this.dispose();
        });
        
        this.add(mainPanel);
    }
    
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

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Register().setVisible(true));
    }
}