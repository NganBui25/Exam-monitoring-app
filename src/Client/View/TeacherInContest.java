package Client.View;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import Client.Controller.Teacher.TeacherController;
import Client.View.Utils.ChatPanel;
import Client.View.Utils.LogPanel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.border.LineBorder; // Để dùng LineBorder
import javax.swing.border.Border;

public class TeacherInContest extends JFrame {

    public TeacherController controller;
    public ArrayList<CameraPanel> cameraPanels = new ArrayList<>(); //List lưu các ô camera
    
    public JPanel camerasGridPanel; 
    public ChatPanel chatPn;
    public LogPanel keyPn;
    
    private File uploadedExamFile =null;
    private JButton btnViewExam;
    private JButton btnLockRoom;
    private boolean isRoomLocked = false;
    
    private DefaultListModel<String> submitListModel;
    private JList<String> submitList;
    

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
        
        //Nút khóa phòng
        btnLockRoom = new JButton("Khóa phòng thi");
        styleSecondaryButton(btnLockRoom);
        btnLockRoom.addActionListener(e -> toggleRoomLock());
        
        //Nút xem đề thi
        btnViewExam = new JButton("Xem đề");
        styleSecondaryButton(btnViewExam);
        btnViewExam.setEnabled(false); // Chưa có đề 
        btnViewExam.setBackground(Color.GRAY); // Màu xám lúc đầu
        btnViewExam.addActionListener(e -> openExamFile());
        
        // Nút Upload đề thi 
        JButton btnUpload = new JButton("Upload Đề thi");
        styleSecondaryButton(btnUpload);
        btnUpload.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(); //Mở cửa sổ chọn file
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
            	uploadedExamFile = fileChooser.getSelectedFile();
                controller.uploadExamFile(fileChooser.getSelectedFile());
                
                btnViewExam.setEnabled(true);
                btnViewExam.setBackground(new Color(52,152,219));
                JOptionPane.showMessageDialog(this, "Upload đề thi thành công!");
            }
        });

        // Nút Kết thúc
        JButton endButton = new JButton("Kết thúc");
        styleDangerButton(endButton);
        endButton.addActionListener(e -> controller.endStream());

        actionPanel.add(btnLockRoom);
        actionPanel.add(btnUpload);
        actionPanel.add(btnViewExam);
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
        
        submitListModel = new DefaultListModel<>();
        submitList = new JList<>(submitListModel);
        submitList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        submitList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setForeground(new Color(39, 174, 96)); // Màu xanh lá cây
                return c;
            }
        });
        JScrollPane submitScroll = new JScrollPane(submitList);
        logsTabbedPane.addTab("Đã nộp bài", submitScroll);

        add(logsTabbedPane, BorderLayout.EAST);

     //Panel chứa các ô camera
     camerasGridPanel = new JPanel(); 

     //FlowwLayout +.LEFT để các ô tự xếp từ trái sang phải, tự động xuống dòng khi hết chỗ
     camerasGridPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

     camerasGridPanel.setBackground(new Color(230, 234, 237));

     	//WrapPanel để giữ FlowLayout hoạt động đúng ScrollPanel, tức là thanh cuộn dọc
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(camerasGridPanel, BorderLayout.NORTH);
        wrapperPanel.setBackground(new Color(230, 234, 237));

        //Tạo thanh cuộn
        JScrollPane mainScrollPane = new JScrollPane(wrapperPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(20); //Tăng tốc độ cuộn chuột
        add(mainScrollPane, BorderLayout.CENTER);

        // --- Cài đặt Frame ---
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void addCameraScreen() {
        CameraPanel newCamPanel = new CameraPanel(cameraPanels.size());
        
        // Tạo Menu chuột phải 
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem warnItem = new JMenuItem("Gửi cảnh báo");
        JMenuItem kickItem = new JMenuItem("Kick sinh viên");
        kickItem.setForeground(Color.RED);

        warnItem.addActionListener(e -> {
            int studentID = newCamPanel.getPanelIndex() / 2;
            //Hộp thoại xác nhận Yes/No
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
            	newCamPanel.setBorder(new LineBorder(new Color(189, 195, 199), 1));
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
            camerasGridPanel.revalidate(); 
            camerasGridPanel.repaint();
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
            nameLabel.setPreferredSize(new Dimension(0, 30));
            nameLabel.setBackground(new Color(245, 245, 245));
            nameLabel.setOpaque(true);

            add(imageLabel, BorderLayout.CENTER);
            add(nameLabel, BorderLayout.SOUTH);
        }

        public void setImageIcon(ImageIcon icon) {
            imageLabel.setIcon(icon);
            int newWidth = icon.getIconWidth();
            int newHeight = icon.getIconHeight() + 30;
            imageLabel.setText("");
            if (this.getPreferredSize().width != newWidth || this.getPreferredSize().height != newHeight) {
                this.setPreferredSize(new Dimension(newWidth, newHeight));
                
                // Cập nhật lại bản thân panel
                this.revalidate();
                this.repaint();
            }
        }

        public void setNameText(String text) { nameLabel.setText(text); }
        public String getNameText() { return nameLabel.getText(); }
        public int getPanelIndex() { return panelIndex; }
    }
    private void toggleRoomLock() {
    	if(!isRoomLocked) {
    		isRoomLocked = true;
    		btnLockRoom.setText("Mở phòng");
    		btnLockRoom.setBackground(new Color(230,126,34));
    		
    		controller.sendLockSignal(true);
    		JOptionPane.showMessageDialog(this, "Đã khóa phòng thi. Sinh viên không thể vào phòng.");
    	}
    	else {
    		isRoomLocked = false;
    		btnLockRoom.setText("Khóa phòng");
    		btnLockRoom.setBackground(new Color(52,152,219));
    		
    		controller.sendLockSignal(false);
    		JOptionPane.showMessageDialog(this, "Đã mở phòng thi!");
    	}
    }
    
    private void openExamFile() {
    	if(uploadedExamFile != null && uploadedExamFile.exists()) {
    		try {
    			Desktop.getDesktop().open(uploadedExamFile);
    		} catch(IOException e) {
    			JOptionPane.showMessageDialog(this, "Không thể mở file: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "File không tồn tại hoặc đã bị xóa!");
    	}
    }
    
    public void addSubmittedStudent(int studentNum, String name) {
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String entry = "[" + time + "] Sinh viên " + studentNum + " - " + name + " đã nộp bài";
        
        if (!submitListModel.contains(entry)) {
            submitListModel.addElement(entry);
            JTabbedPane parentTab = (JTabbedPane) submitList.getParent().getParent().getParent(); // Tùy cấu trúc
        }
    }
    public String getStudentNameByNum(int studentNum) {
        int panelIndex = studentNum * 2;
        if (panelIndex < cameraPanels.size() && cameraPanels.get(panelIndex) != null) {
            String fullName = cameraPanels.get(panelIndex).getNameText();
            try {
                int dotIndex = fullName.indexOf(".");
                int bracketIndex = fullName.lastIndexOf("[");
                if (dotIndex != -1 && bracketIndex != -1) {
                	System.out.println("Tên sinh viên: " + fullName);
                    return fullName.substring(dotIndex+1, bracketIndex);
                }
            } catch (Exception e) { return "Sinh viên " + studentNum; }
        }
        return "Sinh viên " + studentNum;
    }
    
    public void highlightHandRaised(int studentNum) {
    	int panelIdx = studentNum * 2 + 1;
    	if(panelIdx < cameraPanels.size()) {
    		CameraPanel panel = cameraPanels.get(panelIdx);
    		
    		panel.setBorder(new LineBorder(Color.YELLOW, 5));
    		
    		java.awt.Toolkit.getDefaultToolkit().beep();
    		addText("Hệ thống: Sinh viên " + panel.getNameText() + " đang giơ tay!" + "\n");
    	}
    }
    
    public void showStudentWarning(int studentNum, String appName) {
    	String studentName = getStudentNameByNum(studentNum);
    	
    	addText("⚠️ CẢNH BÁO: " + studentName + " đang mở " + appName.toUpperCase() + "!" + "\n");
    	
    	int panelIdx = studentNum*2 + 1;
    	if(panelIdx < cameraPanels.size()) {
    		CameraPanel panel = cameraPanels.get(panelIdx);
    		
    		panel.setBorder(new LineBorder(Color.RED,5));
    		panel.revalidate();
    	}
    	java.awt.Toolkit.getDefaultToolkit().beep();
    }
}