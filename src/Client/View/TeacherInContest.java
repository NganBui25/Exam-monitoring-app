
package src.Client.view;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.Font;
import java.awt.GridLayout; // Sẽ dùng GridLayout
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


import javax.swing.*;
import javax.swing.border.LineBorder;

import src.Client.controller.teacher.TeacherController;
import src.Client.view.Utils.ChatPanel;
import src.Client.view.Utils.LogPanel;

public class TeacherInContest extends JFrame {

    public TeacherController controller;
    
    // Sửa lại: Chúng ta sẽ dùng một Panel tùy chỉnh cho mỗi camera
    // để quản lý cả ảnh và tên
    public ArrayList<CameraPanel> cameraPanels = new ArrayList<>();
    
    public JPanel camerasGridPanel; // Panel dùng GridLayout
    public ChatPanel chatPn;
    public LogPanel keyPn;

    public TeacherInContest(TeacherController controller) {
        this.controller = controller;
        Components(); // Xây dựng giao diện mới
    }

    private void Components() {
        setLayout(new BorderLayout());
        
        // --- 1. HEADER (NORTH) ---
        // Thanh header màu tối, hiện đại
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(new Color(45, 55, 75)); // Màu xanh đậm/tối
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Tên phòng thi
        JLabel titleLabel = new JLabel(
            "<html><b>Kỳ thi:</b> " + controller.name + 
            "&nbsp;&nbsp;&nbsp;&nbsp;<b>Mã phòng:</b> " + controller.roomId + "</html>"
        );
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Nút "Kết thúc"
        JButton endButton = new JButton("Kết thúc Giám sát");
        styleDangerButton(endButton); // Style nút màu đỏ
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.endStream();
            }
        });
        headerPanel.add(endButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. KHU VỰC LOGS (EAST) ---
        // Dùng JTabbedPane để chứa Chat và Keylog
        JTabbedPane logsTabbedPane = new JTabbedPane();
        logsTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logsTabbedPane.setPreferredSize(new Dimension(350, 1000));

        // Tạo LogPanel (từ code cũ của bạn)
        keyPn = new LogPanel("Keyboard Log");
        logsTabbedPane.addTab("Keylog", keyPn);

        // Tạo ChatPanel (từ code cũ của bạn)
        chatPn = new ChatPanel(controller);
        logsTabbedPane.addTab("Chat Giám thị", chatPn);

        add(logsTabbedPane, BorderLayout.EAST);

        // --- 3. KHU VỰC CAMERA (CENTER) ---
        // Dùng GridLayout để các camera thẳng hàng
        // (0 hàng, 3 cột, khoảng cách 15px)
        camerasGridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        camerasGridPanel.setBackground(new Color(230, 230, 230)); 

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(230, 230, 230)); // Đặt màu nền cho wrapper
        wrapperPanel.add(camerasGridPanel, BorderLayout.NORTH);

        // Thêm JScrollPane để có thể cuộn
        JScrollPane mainScrollPane = new JScrollPane(wrapperPanel); // Cuộn wrapperPanel
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16); 

        add(mainScrollPane, BorderLayout.CENTER);

        // --- Cài đặt Frame ---
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Hàm này thêm một "CameraPanel" (tùy chỉnh) vào lưới
     */
    public void addCameraScreen() {
        CameraPanel newCamPanel = new CameraPanel(cameraPanels.size());
        
        // Thêm sự kiện click (lấy từ code cũ)
        newCamPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                controller.focus(newCamPanel.getPanelIndex());
            }
        });
        
        cameraPanels.add(newCamPanel);
        camerasGridPanel.add(newCamPanel);
        
        // Cập nhật UI
        camerasGridPanel.revalidate();
        camerasGridPanel.repaint();
    }

    public void removeCameraScreen(int studentNum) {
        if (studentNum < cameraPanels.size() && cameraPanels.get(studentNum) != null) {
            camerasGridPanel.remove(cameraPanels.get(studentNum));
            cameraPanels.set(studentNum, null); 
            
            // Cập nhật UI
            camerasGridPanel.revalidate();
            camerasGridPanel.repaint();
        }
    }
    
    public void setImage(byte[] img, int cameraNum) {
        while (cameraNum >= cameraPanels.size()) {
            addCameraScreen();
        }
        CameraPanel panel = cameraPanels.get(cameraNum);
        if (panel != null) {
            panel.setImageIcon(new ImageIcon(img));
        }
    }
    
    public void addStudent(int studentNum, String name) {
        // Cần 2 panel (Screen và Cam)
        while (studentNum * 2 + 1 >= cameraPanels.size()) {
            addCameraScreen();
        }
        // Gán tên cho 2 panel
        cameraPanels.get(studentNum * 2).setNameText(studentNum + ". " + name + " - Màn hình");
        cameraPanels.get(studentNum * 2 + 1).setNameText(studentNum + ". " + name + " - Facecam");
    }
    
    public void deleteStudent(int studentNum) {
        // Xóa 2 panel
        removeCameraScreen(studentNum * 2);
        removeCameraScreen(studentNum * 2 + 1);
    }
    
    public void addText(String txt) {
        chatPn.addText(txt);
    }
    
    public void addKeyLog(int studentNum, String duration, String keys) {
        // Logic lấy tên được cập nhật để dùng panel mới
        if (studentNum * 2 < cameraPanels.size() && cameraPanels.get(studentNum * 2) != null) {
            String cameraText = cameraPanels.get(studentNum * 2).getNameText(); // Lấy tên từ panel
            
            // Logic parse tên (giữ từ code cũ của bạn)
            int i = cameraText.indexOf(' ');
            int j = cameraText.indexOf(' ', i + 1);
            if (i != -1 && j != -1) {
                String namePart = cameraText.substring(i + 1, j);
                String msg = duration + ": " + studentNum + ". " + namePart + " has typed: " + keys;
                keyPn.addText(msg);
            }
        }
    }
    
    // --- CÁC HÀM TIỆN ÍCH UI MỚI ---
    
    private void styleDangerButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(new Color(220, 53, 69)); // Màu đỏ
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    /**
     * Một lớp nội (inner class) để quản lý
     * một ô camera (gồm ảnh và tên).
     */
    private class CameraPanel extends JPanel {
        private JLabel imageLabel;
        private JLabel nameLabel;
        private int panelIndex;
        
        public CameraPanel(int index) {
            this.panelIndex = index;
            setLayout(new BorderLayout());
            setBorder(new LineBorder(Color.BLACK, 1));
            setBackground(Color.WHITE);
            
            setPreferredSize(new Dimension(300,300));
            // 1. Khu vực hiển thị ảnh
            imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.DARK_GRAY);
            
            // 2. Khu vực hiển thị tên
            nameLabel = new JLabel("Đang chờ kết nối...");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            nameLabel.setOpaque(true);
            nameLabel.setBackground(Color.LIGHT_GRAY);

            add(imageLabel, BorderLayout.CENTER);
            add(nameLabel, BorderLayout.SOUTH);
        }
        
        public int getPanelIndex() {
            return panelIndex;
        }
        
        public void setImageIcon(ImageIcon icon) {
			Image img = icon.getImage();
            
            // Lấy kích thước của imageLabel (phần màu xám)
            int width = imageLabel.getWidth();
            int height = imageLabel.getHeight();
            
            // Nếu panel chưa được vẽ, width/height sẽ là 0.
            // Chúng ta dùng kích thước mong muốn trừ đi tên
            if (width == 0 || height == 0) {
                width = 320; 
                height = 280 - 40; // Giả sử nameLabel cao 40px
            }

            // Resize ảnh
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            
            imageLabel.setIcon(new ImageIcon(scaledImg));
            imageLabel.setText(null); // Xóa text chờ
        }
        
        public void setNameText(String text) {
            nameLabel.setText(text);
        }
        
        public String getNameText() {
            return nameLabel.getText();
        }
    }
}
