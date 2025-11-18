package Client.View;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import Client.Controller.student.StudentController;
import Client.View.Utils.ChatPanel; // Giữ lại import của bạn

public class StudentlnContest extends JFrame implements NativeKeyListener {

    public StudentController controller;
    public ChatPanel chatPn;

    // Các biến UI cho thiết kế mới
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JTextField nameTf;
    private JTextField roomIdTf;
    private JLabel lbWarning;
    private JLabel teacherNameLabel; // Hiển thị tên kỳ thi
    private JLabel statusLabel; // Hiển thị trạng thái giám sát

    public StudentlnContest(StudentController controller) {
        this.controller = controller;

        // --- 1. Cài đặt Cửa sổ (Frame) ---
        setTitle("Thí sinh - Phòng thi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Giữ kích thước cũ của bạn
        setLocationRelativeTo(null);

        // --- 2. CardLayout (Để lật qua lại) ---
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        JPanel panelJoin = createJoinPanel(); // Mặt 1: Form tham gia
        JPanel panelContest = createContestPanel(); // Mặt 2: Phòng thi

        // --- 4. Thêm 2 mặt vào CardLayout ---
        mainContainer.add(panelJoin, "JOIN_PANEL");
        mainContainer.add(panelContest, "CONTEST_PANEL");

        // --- 5. Thêm Container chính vào Frame ---
        add(mainContainer, BorderLayout.CENTER);

        // Hiển thị mặt "Join" đầu tiên
        cardLayout.show(mainContainer, "JOIN_PANEL");
        
        setVisible(true); // Hiển thị frame
    }

    /**
     * Tạo Panel "Tham gia" (Giống Login)
     */
    private JPanel createJoinPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // Tiêu đề
        JLabel titleLabel = new JLabel("Tham gia Kỳ thi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(titleLabel, gbc);

        // Nhãn "Họ tên"
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createLabel("Họ tên:"), gbc);

        // Ô "Họ tên"
        nameTf = new JTextField("Student Name"); // Thay đổi giá trị mặc định nếu muốn
        styleField(nameTf);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(nameTf, gbc);

        // Nhãn "Mã phòng"
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createLabel("Mã phòng (Room ID):"), gbc);

        // Ô "Mã phòng"
        roomIdTf = new JTextField(""); // Thay đổi giá trị mặc định nếu muốn
        styleField(roomIdTf);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(roomIdTf, gbc);

        // Nhãn cảnh báo (Ẩn)
        lbWarning = new JLabel("Không tìm thấy phòng hoặc tên đã tồn tại.");
        lbWarning.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbWarning.setForeground(Color.RED);
        lbWarning.setHorizontalAlignment(JLabel.CENTER);
        lbWarning.setVisible(false);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lbWarning, gbc);

        // Nút "Tham gia"
        JButton joinButton = new JButton("Tham gia");
        stylePrimaryButton(joinButton);
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(joinButton, gbc);
        
        // Đẩy mọi thứ lên trên
        gbc.gridy++;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        // Xử lý sự kiện nút Join
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinButton.setEnabled(false); // Vô hiệu hóa nút
                joinButton.setText("Đang tham gia...");
                lbWarning.setVisible(false);

                String name = nameTf.getText();
                String roomId = roomIdTf.getText();

                // Gọi controller (giống code cũ)
                String msg = controller.joinRoom(name, roomId);

                if (msg != null) {
                    startCapture(); // Đăng ký key hook và bắt đầu threads
                    teacherNameLabel.setText("Kỳ thi: " + msg); // Cập nhật tên kỳ thi
                    cardLayout.show(mainContainer, "CONTEST_PANEL"); // Lật
                } else {
                    lbWarning.setVisible(true);
                    joinButton.setEnabled(true);
                    joinButton.setText("Tham gia");
                }
            }
        });

        return panel;
    }

    private JPanel createContestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245)); 

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(60, 90, 150)); // Màu xanh đậm
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        teacherNameLabel = new JLabel("Kỳ thi: [Đang chờ...]");
        teacherNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        teacherNameLabel.setForeground(Color.WHITE);
        topPanel.add(teacherNameLabel, BorderLayout.WEST);
        
        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        eastPanel.setOpaque(false); 
        
        statusLabel = new JLabel("TRẠNG THÁI: ĐANG GIÁM SÁT (SCREEN + CAM)");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(150, 255, 150)); // Màu xanh lá
        eastPanel.add(statusLabel); 

        JButton leaveButton = new JButton("Rời phòng giám sát");
        styleDangerButton(leaveButton); 
        
        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.running = false; 
            }
        });
        
        eastPanel.add(leaveButton); 
        
        topPanel.add(eastPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        chatPn = new ChatPanel(controller);
        chatPn.setPreferredSize(new Dimension(300, 0)); 
        panel.add(chatPn, BorderLayout.EAST);

        // --- Trung tâm (Thông báo) ---
        JLabel centerMessage = new JLabel("Bạn đang trong phòng thi. Vui lòng tập trung làm bài.");
        centerMessage.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        centerMessage.setForeground(Color.GRAY);
        centerMessage.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(centerMessage, BorderLayout.CENTER);
        
        return panel;
    }

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

    public void nativeKeyPressed(NativeKeyEvent e) {
        char c = NativeKeyEvent.getKeyText(e.getKeyCode()).charAt(0);
        if (e.getKeyCode() == 0xe36)
            c = '⇧';
        controller.currKeys += System.currentTimeMillis() + " " + c + " ";
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        char c = NativeKeyEvent.getKeyText(e.getKeyCode()).charAt(0);
        if (e.getKeyCode() == 0xe36)
            c = '⇧';
        if (c == '⇧' || c == '⌘' || c == '⌥' || c == '⌃')
            controller.currKeys += System.currentTimeMillis() + " " + c + " ";
    }

    private void startCapture() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        GlobalScreen.addNativeKeyListener(this);
        controller.startThreads();
    }
    
    public void addText(String txt) {
		chatPn.addText(txt);
	}
    private void styleDangerButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(new Color(220, 53, 69)); // Màu đỏ
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}