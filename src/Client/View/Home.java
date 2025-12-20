package Client.View;

import Client.Controller.student.StudentController;
import org.opencv.core.Core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Home extends javax.swing.JFrame {

    public Home() {
        Components();
    }

    private void Components() {

        setTitle("Hệ thống Giám sát Thi cử");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650)); 
        setLocationRelativeTo(null); 
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(20, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false); 
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0)); // Padding

        JLabel titleLabel = new JLabel("Chọn Vai Trò Của Bạn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(new Color(50, 50, 50)); 
        titlePanel.add(titleLabel);

        add(titlePanel, BorderLayout.NORTH);

        // --- 3. Khu vực chọn (Trung tâm) ---
        // Dùng GridLayout để tạo 2 cột bằng nhau, có khoảng cách
        JPanel cardPanel = new JPanel(new GridLayout(1, 2, 40, 40));
        cardPanel.setOpaque(false);
        // Thêm padding xung quanh 2 thẻ
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); 

        // --- 4. Tạo Thẻ "Giám thị" ---
        // (Chúng ta dùng một hàm trợ giúp bên dưới)
        JPanel teacherCard = createRoleCard(
                "Giám thị",
                "Đăng nhập để quản lý và giám sát phòng thi.",
                new Color(230, 240, 255), // Màu nền xanh nhạt
                "giaovien" // Tên hành động
        );
        // 
        cardPanel.add(teacherCard);

        // --- 5. Tạo Thẻ "Thí sinh" ---
        JPanel studentCard = createRoleCard(
                "Thí sinh",
                "Vào phòng thi để bắt đầu làm bài kiểm tra.",
                new Color(230, 255, 240), // Màu nền xanh lá nhạt
                "hocsinh" // Tên hành động
        );
        // 
        cardPanel.add(studentCard);

        add(cardPanel, BorderLayout.CENTER);
        
        // --- 6. Chân trang (Phía Nam) ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel footerLabel = new JLabel("Ứng dụng Livestream Giám sát Cuộc Thi Online");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
        
        pack(); // Tối ưu kích thước
    }

    /**
     * Hàm trợ giúp để tạo một thẻ (Card) vai trò có thể nhấp chuột
     */
    private JPanel createRoleCard(String title, String description, Color bgColor, String actionCommand) {
        
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(bgColor);
        
        // Tạo viền bo tròn (hơi giả lập) và hiệu ứng nổi
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1), // Viền ngoài
                BorderFactory.createEmptyBorder(40, 40, 40, 40) // Padding bên trong
        ));
        
        // Tiêu đề (Tên vai trò)
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);

        // Mô tả
        // Dùng HTML để tự động xuống dòng và căn giữa
        String htmlDescription = "<html><div style='text-align: center; width: 250px;'>" + description + "</div></html>";
        JLabel descLabel = new JLabel(htmlDescription);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        descLabel.setForeground(new Color(80, 80, 80));
        descLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(descLabel, BorderLayout.CENTER);
        
        // Biến panel thành một nút bấm
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm sự kiện nhấp chuột và hiệu ứng rê chuột
        card.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (actionCommand.equals("giaovien")) {
                    giaovienActionPerformed();
                } else {
                    hocsinhActionPerformed();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Làm thẻ sáng lên khi rê chuột vào
                card.setBackground(bgColor.brighter());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 120, 215), 2), // Viền xanh dương
                        BorderFactory.createEmptyBorder(40, 40, 40, 40)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Trở lại bình thường khi rê chuột ra
                card.setBackground(bgColor);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(bgColor.darker(), 1),
                        BorderFactory.createEmptyBorder(40, 40, 40, 40)
                ));
            }
        });
        
        return card;
    }

    private void giaovienActionPerformed() {
        new Login().setVisible(true);
        this.dispose();
    }

    private void hocsinhActionPerformed() {
        new StudentController();
        this.dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }


}