package Client.View;

import Server.DAO.UserDAO;
import Server.Entity.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login extends javax.swing.JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public Login() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập ");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(400, 300)); // Tăng chiều cao một chút
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 15)); // Tăng số hàng lên 4
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

        // --- Ô trống để căn chỉnh ---
        mainPanel.add(new JLabel());
        
        JButton btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(labelFont);
        mainPanel.add(btnLogin);

        // --- Ô trống ---
        mainPanel.add(new JLabel());
        
        // --- Nút Đăng ký mới ---
        JButton btnRegister = new JButton("Chưa có tài khoản? Đăng ký");
        btnRegister.setFont(new Font("Arial", Font.ITALIC, 12));
        // Loại bỏ đường viền để trông giống một liên kết
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mainPanel.add(btnRegister);
        
        // --- Xử lý sự kiện cho các nút ---
        btnLogin.addActionListener(e -> btnLoginActionPerformed(e));
        
        btnRegister.addActionListener(e -> {
            new Register().setVisible(true);
            this.dispose();
        });

        this.add(mainPanel);
    }
    
    private void btnLoginActionPerformed(ActionEvent evt) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ username và password.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = UserDAO.login(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!\nChào mừng giám thị " + user.getUsername() + ".", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username hoặc password không chính xác.", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}