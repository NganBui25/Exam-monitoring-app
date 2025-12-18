package Client.View;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.LineBorder;
import Client.Controller.Teacher.TeacherController;
import Client.View.Utils.ChatPanel;
import Client.View.Utils.LogPanel;

public class TeacherInContest extends JFrame {

    public TeacherController controller;
    public ArrayList<CameraPanel> cameraPanels = new ArrayList<>();
    
    public JPanel camerasGridPanel; 
    public ChatPanel chatPn;
    public LogPanel keyPn;

    public TeacherInContest(TeacherController controller) {
        this.controller = controller;
        initModernUI(); 
    }

    private void initModernUI() {
        setTitle("Hệ thống Giám sát Phòng thi");
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 242, 245));

        // --- 1. HEADER (NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(new Color(45, 55, 75)); // Màu xanh Charcoal hiện đại
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Thông tin phòng thi
        JLabel titleLabel = new JLabel(
            "<html><font color='#bdc3c7'>Kỳ thi:</font> " + controller.name + 
            " &nbsp;&nbsp; <font color='#bdc3c7'>Mã phòng:</font> <font color='#f1c40f'>" + controller.roomId + "</font></html>"
        );
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Nhóm nút chức năng bên phải Header
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);

        // Nút Upload đề thi (Tính năng mới từ code 2)
        JButton btnUpload = new JButton("Upload Đề thi");
        styleSecondaryButton(btnUpload);
        btnUpload.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                controller.uploadExamFile(fileChooser.getSelectedFile());
            }
        });

        // Nút Kết thúc
        JButton endButton = new JButton("Kết thúc Giám sát");
        styleDangerButton(endButton);
        endButton.addActionListener(e -> controller.endStream());

        actionPanel.add(btnUpload);
        actionPanel.add(endButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. LOGS & CHAT (EAST) ---
        JTabbedPane logsTabbedPane = new JTabbedPane();
        logsTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logsTabbedPane.setPreferredSize(new Dimension(350, 1000));

        keyPn = new LogPanel("Keyboard Log");
        logsTabbedPane.addTab("Keylog", keyPn);

        chatPn = new ChatPanel(controller);
        logsTabbedPane.addTab("Chat Giám thị", chatPn);

        add(logsTabbedPane, BorderLayout.EAST);

        // --- 3. CAMERA GRID (CENTER) ---
        camerasGridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        camerasGridPanel.setBackground(new Color(230, 234, 237));

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(camerasGridPanel, BorderLayout.NORTH);
        wrapperPanel.setBackground(new Color(230, 234, 237));

        JScrollPane mainScrollPane = new JScrollPane(wrapperPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(mainScrollPane, BorderLayout.CENTER);

        // --- Cài đặt Frame ---
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void addCameraScreen() {
        CameraPanel newCamPanel = new CameraPanel(cameraPanels.size());
        
        // Tạo Menu chuột phải (Tính năng từ code 2)
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem warnItem = new JMenuItem("Gửi cảnh báo (Warning)");
        JMenuItem kickItem = new JMenuItem("Đuổi sinh viên (Kick)");
        kickItem.setForeground(Color.RED);

        warnItem.addActionListener(e -> {
            int studentID = newCamPanel.getPanelIndex() / 2;
            int confirm = JOptionPane.showConfirmDialog(this, "Gửi cảnh báo đến sinh viên " + studentID + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.sendWarning(studentID, "Cảnh báo: Yêu cầu bạn nghiêm túc làm bài!");
            }
        });

        kickItem.addActionListener(e -> {
            int studentID = newCamPanel.getPanelIndex() / 2;
            int confirm = JOptionPane.showConfirmDialog(this, "Đuổi sinh viên " + studentID + " khỏi phòng?", "Cảnh báo nguy hiểm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.kickStudent(studentID);
            }
        });

        popupMenu.add(warnItem);
        popupMenu.addSeparator();
        popupMenu.add(kickItem);
        newCamPanel.setComponentPopupMenu(popupMenu);

        // Sự kiện click trái
        newCamPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.focus(newCamPanel.getPanelIndex());
            }
        });

        cameraPanels.add(newCamPanel);
        camerasGridPanel.add(newCamPanel);
        camerasGridPanel.revalidate();
        camerasGridPanel.repaint();
    }

    public void removeCameraScreen(int studentNum) {
        if (studentNum < cameraPanels.size() && cameraPanels.get(studentNum) != null) {
            camerasGridPanel.remove(cameraPanels.get(studentNum));
            cameraPanels.set(studentNum, null);
            camerasGridPanel.revalidate();
            camerasGridPanel.repaint();
        }
    }

    public void setImage(byte[] img, int cameraNum) {
        while (cameraNum >= cameraPanels.size()) addCameraScreen();
        CameraPanel panel = cameraPanels.get(cameraNum);
        if (panel != null) {
            panel.setImageIcon(new ImageIcon(img));
        }
    }

    public void addStudent(int studentNum, String name) {
        while (studentNum * 2 + 1 >= cameraPanels.size()) addCameraScreen();
        cameraPanels.get(studentNum * 2).setNameText(studentNum + ". " + name + " [Màn hình]");
        cameraPanels.get(studentNum * 2 + 1).setNameText(studentNum + ". " + name + " [Facecam]");
    }

    public void deleteStudent(int studentNum) {
        removeCameraScreen(studentNum * 2);
        removeCameraScreen(studentNum * 2 + 1);
    }

    public void addText(String txt) { chatPn.addText(txt); }

    public void addKeyLog(int studentNum, String duration, String keys) {
        if (studentNum * 2 < cameraPanels.size() && cameraPanels.get(studentNum * 2) != null) {
            String nameText = cameraPanels.get(studentNum * 2).getNameText();
            String msg = duration + ": " + nameText + " typed: " + keys;
            keyPn.addText(msg);
        }
    }

    // --- UTILS: STYLE BUTTONS ---
    private void styleDangerButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(231, 76, 60)); // Red
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(52, 152, 219)); // Blue
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    // --- INNER CLASS CAMERA PANEL ---
    private class CameraPanel extends JPanel {
        private JLabel imageLabel;
        private JLabel nameLabel;
        private int panelIndex;

        public CameraPanel(int index) {
            this.panelIndex = index;
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(320, 240));
            setBackground(Color.WHITE);
            setBorder(new LineBorder(new Color(189, 195, 199), 1));

            imageLabel = new JLabel("Đang kết nối...", SwingConstants.CENTER);
            imageLabel.setOpaque(true);
            imageLabel.setBackground(new Color(33, 33, 33));
            imageLabel.setForeground(Color.LIGHT_GRAY);

            nameLabel = new JLabel("Chờ sinh viên...");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setPreferredSize(new Dimension(320, 30));
            nameLabel.setBackground(new Color(245, 245, 245));
            nameLabel.setOpaque(true);

            add(imageLabel, BorderLayout.CENTER);
            add(nameLabel, BorderLayout.SOUTH);
        }

        public void setImageIcon(ImageIcon icon) {
            Image img = icon.getImage();
            int w = (imageLabel.getWidth() > 0) ? imageLabel.getWidth() : 320;
            int h = (imageLabel.getHeight() > 0) ? imageLabel.getHeight() : 210;
            imageLabel.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            imageLabel.setText("");
        }

        public void setNameText(String text) { nameLabel.setText(text); }
        public String getNameText() { return nameLabel.getText(); }
        public int getPanelIndex() { return panelIndex; }
    }
}