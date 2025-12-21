package Client.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import Client.Controller.student.StudentController;
import Client.View.Utils.ChatPanel;

public class StudentlnContest extends JFrame implements NativeKeyListener {

    public StudentController controller;
    public ChatPanel chatPn;
    public JLabel cameraScreen;
    
    // Khai báo biến toàn cục để dễ ẩn/hiện
    private JButton btnDownload;
    private JButton btnSubmit;
    private JButton btnLeave; // [MỚI] Nút rời phòng
    private JTextField nameTf;
    private JTextField roomIdTf;
    private JButton btnJoin;
    private JLabel lbName, lbRoomId, teacher;
    private JButton btnHand;

    public StudentlnContest(StudentController controller) {
        this.controller = controller;
        // Tắt log của JNativeHook cho đỡ rác console
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        setLayout(new BorderLayout());
        setTitle("Hệ thống Thi Trực tuyến - Sinh viên");

        cameraScreen = new JLabel();
        cameraScreen.setHorizontalAlignment(JLabel.CENTER);
        cameraScreen.setBackground(Color.WHITE);
        cameraScreen.setOpaque(true); // Để hiện nền đen khi chưa có cam

        JPanel topPn = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnDownload = new JButton("Tải đề thi");
        btnDownload.setFocusable(false);
        btnDownload.addActionListener(e -> controller.downloadExamFile());
        
        btnSubmit = new JButton("Nộp bài");
        btnSubmit.setBackground(new Color(46, 204, 113)); // Xanh lá
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.addActionListener(e -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc muốn nộp file: " + file.getName() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.submitExam(file);
                }
            }
        });

        btnLeave = new JButton("Rời phòng");
        btnLeave.setBackground(new Color(231, 76, 60)); // Màu đỏ
        btnLeave.setForeground(Color.WHITE);
        btnLeave.addActionListener(e -> handleLeaveRoom());
        
        btnHand = new JButton("Giơ tay");
        btnHand.setBackground(Color.yellow);
        btnHand.setForeground(Color.BLACK);
        btnHand.addActionListener(e -> {
        	controller.sendRaiseHand();
        	btnHand.setEnabled(false);
        	btnHand.setText("Đã giơ tay");
        	//Chặn spam sau 10s
        	new javax.swing.Timer(10000, evt -> {
                btnHand.setEnabled(true);
                btnHand.setText("✋ Giơ tay");
                ((javax.swing.Timer)evt.getSource()).stop();
            }).start();
        });
        btnDownload.setVisible(false);
        btnSubmit.setVisible(false);
        btnLeave.setVisible(false);
        btnHand.setVisible(false);

        topPn.add(btnSubmit);
        topPn.add(javax.swing.Box.createHorizontalStrut(10));
        topPn.add(btnDownload);
        topPn.add(javax.swing.Box.createHorizontalStrut(10));
        topPn.add(btnLeave); // Thêm nút Rời phòng vào giao diện
        topPn.add(btnHand);

        // Nút Back (chỉ hiện lúc chưa join)
        JButton backBtn = new JButton("\u2190");
        // topPn.add(backBtn); // Tùy bạn muốn để đâu, code cũ để BorderLayout.WEST hơi lạ trong FlowLayout
        backBtn.addActionListener(e -> controller.back());

        // --- 4. Khu vực Đăng nhập (Center Panel) ---
        JPanel topCenterPn = new JPanel(new FlowLayout());
        lbName = new JLabel("Name:");
        nameTf = new JTextField(10);
        lbRoomId = new JLabel("Room ID:");
        roomIdTf = new JTextField(10);
        btnJoin = new JButton("Join");
        JLabel lbWarning = new JLabel("Không tồn tại phòng");
        teacher = new JLabel();
        lbWarning.setVisible(false);
        
        // Font style
        teacher.setFont(new Font("Arial", Font.BOLD, 14));
        teacher.setForeground(new Color(41, 128, 185));

        topCenterPn.add(teacher);
        topCenterPn.add(lbName);
        topCenterPn.add(nameTf);
        topCenterPn.add(lbRoomId);
        topCenterPn.add(roomIdTf);
        topCenterPn.add(btnJoin);
        topCenterPn.add(lbWarning);

        // Sự kiện nút Join
        btnJoin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validate input
                if(nameTf.getText().isEmpty() || roomIdTf.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(StudentlnContest.this, "Vui lòng nhập đầy đủ thông tin!");
                    return;
                }

                roomIdTf.setEditable(false);
                nameTf.setEditable(false);
                btnJoin.setEnabled(false);

                // Gọi Controller
                String msg = controller.joinRoom(nameTf.getText(), roomIdTf.getText());
                
                if (msg != null && !msg.equals("LOCKED") && !msg.equals("NETWORK_ERROR")) {
                    // --- JOIN THÀNH CÔNG ---
                    // Hiện các nút chức năng
                    btnDownload.setVisible(true);
                    btnSubmit.setVisible(true);
                    btnLeave.setVisible(true); // Hiện nút rời phòng
                    btnHand.setVisible(true);
                    
                    // Ẩn các ô nhập liệu
                    teacher.setText("Kỳ thi: " + msg);
                    lbName.setVisible(false);
                    nameTf.setVisible(false);
                    lbRoomId.setVisible(false);
                    roomIdTf.setVisible(false);
                    btnJoin.setVisible(false);
                    lbWarning.setVisible(false);
                    backBtn.setVisible(false);
                    
                    // Bắt đầu Keylog & Camera
                    startCapture();
                }
                else if(msg != null && msg.equals("LOCKED")){
                    JOptionPane.showMessageDialog(teacher, "Phòng thi đang bị KHÓA!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    resetLoginUI();
                } else {
                    lbWarning.setVisible(true);
                    resetLoginUI();
                }
            }
        });

        topPn.add(topCenterPn);
        chatPn = new ChatPanel(controller);

        add(cameraScreen, BorderLayout.CENTER);
        add(topPn, BorderLayout.NORTH);
        add(chatPn, BorderLayout.EAST);

        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // --- LOGIC XỬ LÝ UI MỚI ---

    private void handleLeaveRoom() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn rời phòng thi?\nViệc này sẽ ngắt kết nối với giám thị.", 
            "Xác nhận rời phòng", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            stopCapture();
            
            controller.endStream(); 
            
            btnDownload.setVisible(false);
            btnSubmit.setVisible(false);
            btnLeave.setVisible(false);
            teacher.setText("");
            cameraScreen.setIcon(null); 
            
            lbName.setVisible(true);
            nameTf.setVisible(true);
            nameTf.setText(""); 
            lbRoomId.setVisible(true);
            roomIdTf.setVisible(true);
            btnJoin.setVisible(true);
            resetLoginUI();
            
            JOptionPane.showMessageDialog(this, "Đã rời phòng thi.");
        }
    }

    private void resetLoginUI() {
        roomIdTf.setEditable(true);
        nameTf.setEditable(true);
        btnJoin.setEnabled(true);
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
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            GlobalScreen.addNativeKeyListener(this);
            
            controller.startThreads();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void stopCapture() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addText(String txt) {
        chatPn.addText(txt);
    }

    public void showWarning(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Nội dung: " + message, 
            "CẢNH BÁO TỪ GIÁM THỊ", 
            javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}