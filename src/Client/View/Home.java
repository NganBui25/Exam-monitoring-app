package Client.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Home extends javax.swing.JFrame{
	public Home() {
		initComponents();
	}
	 private javax.swing.JButton btnGiamThi;
    private javax.swing.JButton btnThiSinh;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel mainPanel;
	@SuppressWarnings("unchecked")
	private void initComponents() {
        // --- Khởi tạo các thành phần ---
        mainPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        btnGiamThi = new javax.swing.JButton();
        btnThiSinh = new javax.swing.JButton();

        // --- Cấu hình cửa sổ chính (JFrame) ---
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Trang chủ");
        setMinimumSize(new java.awt.Dimension(400, 250));
        setLocationRelativeTo(null); // Canh giữa màn hình

        // --- Cấu hình Panel chính ---
        mainPanel.setLayout(new java.awt.GridLayout(2, 1, 10, 20)); // 2 hàng, 1 cột, khoảng cách dọc 20
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 50, 30, 50)); // Thêm đệm

        // --- Cấu hình Tiêu đề ---
        lblTitle.setFont(new java.awt.Font("Arial", 1, 20)); // Font Arial, Bold, size 20
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("VUI LÒNG CHỌN VAI TRÒ");
        mainPanel.add(lblTitle);

        // --- Panel cho các nút ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 hàng, 2 cột, cách ngang 20

        // --- Cấu hình Nút Giám thị ---
        btnGiamThi.setFont(new java.awt.Font("Arial", 0, 16)); // Font Arial, Plain, size 16
        btnGiamThi.setText("Giám thị");
        btnGiamThi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGiamThiActionPerformed(evt);
            }
        });
        buttonPanel.add(btnGiamThi);

        // --- Cấu hình Nút Thí sinh ---
        btnThiSinh.setFont(new java.awt.Font("Arial", 0, 16)); // Font Arial, Plain, size 16
        btnThiSinh.setText("Thí sinh");
        btnThiSinh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThiSinhActionPerformed(evt);
            }
        });
        buttonPanel.add(btnThiSinh);

        mainPanel.add(buttonPanel);
        
        // Thêm panel chính vào cửa sổ
        getContentPane().add(mainPanel);
        pack(); // Tự động điều chỉnh kích thước cửa sổ cho vừa các thành phần
    }

    // --- Xử lý sự kiện khi nhấn nút Giám thị ---
    private void btnGiamThiActionPerformed(java.awt.event.ActionEvent evt) {
        // Mở màn hình Login (giả sử bạn có một lớp Login kế thừa JFrame)
        // Login loginScreen = new Login();
        // loginScreen.setVisible(true);
        
        // Nếu dùng lại code LoginScreen ở trên
        new Login().setVisible(true);

        // Đóng cửa sổ Home hiện tại
        this.dispose();
    }

    // --- Xử lý sự kiện khi nhấn nút Thí sinh ---
    private void btnThiSinhActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Chức năng dành cho Thí sinh đang được phát triển.");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }
}
