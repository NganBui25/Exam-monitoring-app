package Client.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// Import các thư viện Map
import java.util.HashMap; 
import java.util.Map; 

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import Client.Controller.Teacher.TeacherController;
import Client.View.Utils.ChatPanel;
import Client.View.Utils.LogPanel;
import Client.View.StudentDisplayPanel; // Import Panel "thông minh"

public class TeacherInContest extends JFrame {

    public TeacherController controller;
    
    // === SỬA LỖI: Dùng Map thay vì ArrayList ===
    // Key: finalSlotNum (0, 1, 2, 3...), Value: Panel hiển thị
    public Map<Integer, StudentDisplayPanel> cameraScreens = new HashMap<>();
    
    // Lưu tên sinh viên (Key: studentNum gốc 0, 1, 2...)
    public Map<Integer, String> studentNames = new HashMap<>();


    public JPanel cameras; // Panel chứa tất cả các StudentDisplayPanel
    public ChatPanel chatPn;
    public LogPanel keyPn;

    public TeacherInContest(TeacherController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // --- Phần setup giao diện (giữ nguyên) ---
        JPanel topPn = new JPanel(new FlowLayout());
		topPn.add(new JLabel("Contest Name:" + controller.name));
		topPn.add(new JLabel("Room ID:" + controller.roomId));

		JScrollPane mainPn = new JScrollPane();
		cameras = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
		cameras.setPreferredSize(new Dimension(500, 10000));
		mainPn.setViewportView(cameras);
		mainPn.setBorder(null);

		JPanel rightPn = new JPanel(new BorderLayout());
		rightPn.setPreferredSize(new Dimension(350, 1000));
		keyPn = new LogPanel("Keyboard");
		rightPn.add(keyPn, BorderLayout.CENTER);
		chatPn = new ChatPanel(controller);
		rightPn.add(chatPn, BorderLayout.SOUTH);
		
		JButton ketthuc = new JButton("Kết thúc");
		JPanel bottomCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomCenterPanel.add(ketthuc);
		ketthuc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Sửa lại: Nên gọi hàm endStream của controller
				 controller.endStream();
			}
		});

		add(bottomCenterPanel, BorderLayout.SOUTH);
		add(mainPn, BorderLayout.CENTER);
		add(topPn, BorderLayout.NORTH);
		add(rightPn, BorderLayout.EAST);
        // --- Kết thúc setup giao diện ---

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Sửa lại: Dùng DISPOSE_ON_CLOSE để không tắt cả chương trình
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setVisible(true);
    }

    /**
     * Hàm helper: Tạo và thêm một Panel mới vào giao diện.
     * Trả về Panel vừa tạo.
     */
    private StudentDisplayPanel createAndAddCameraScreen(int finalSlotNum) {
        StudentDisplayPanel cameraScreen = new StudentDisplayPanel();
        cameraScreen.setBorder(new LineBorder(Color.BLACK, 1));

        // Lưu panel vào Map
        cameraScreens.put(finalSlotNum, cameraScreen);
        // Thêm panel vào JPanel chính
        cameras.add(cameraScreen);

        // Thêm listener để focus
        cameraScreen.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Gửi finalSlotNum (0, 1, 2...) khi focus
                controller.focus(finalSlotNum); 
            }
        });

        // Cập nhật giao diện (quan trọng)
        cameras.revalidate();
        cameras.repaint();
        return cameraScreen;
    }


    /**
     * === HÀM ĐÃ SỬA (DÙNG MAP) ===
     * Hàm này được gọi bởi ViewThread khi có KEYFRAME.
     */
    public void updateKeyframeForStudent(int finalSlotNum, byte[] keyframeData) {
        // computeIfAbsent: Lấy panel, nếu chưa có (lần đầu tiên) 
        // thì GỌI HÀM 'createAndAddCameraScreen' để TẠO MỚI.
        // Đây là hàm "an toàn" (thread-safe)
        StudentDisplayPanel panel = cameraScreens.computeIfAbsent(finalSlotNum, k -> createAndAddCameraScreen(k));
        
        // Cập nhật ảnh
        panel.updateKeyframe(keyframeData);
    }

    /**
     * === HÀM ĐÃ SỬA (DÙNG MAP) ===
     * Hàm này được gọi bởi ViewThread khi có DELTA-FRAME (ô).
     */
    public void drawDeltaTileForStudent(int finalSlotNum, byte[] tileData, int x, int y) {
        // Lấy panel từ Map
        StudentDisplayPanel panel = cameraScreens.get(finalSlotNum);
        
        // Chỉ vẽ nếu panel đã tồn tại (đã nhận Keyframe đầu tiên)
        if (panel != null) {
            panel.drawDeltaTile(tileData, x, y);
        }
    }

    /**
     * === HÀM ĐÃ SỬA: Tạo cả 2 slot và lưu tên ===
     * Hàm này được gọi khi có sinh viên mới (TCP).
     * studentNum ở đây là ID gốc (0, 1, 2...).
     */
    public void addStudent(int studentNum, String name) {
        // Tính toán 2 slot cho sinh viên này
        int screenSlot = studentNum * 2; // Ví dụ: 0
        int camSlot = studentNum * 2 + 1;    // Ví dụ: 1

        // "Khởi động" 2 slot này (để tạo panel rỗng trước)
        cameraScreens.computeIfAbsent(screenSlot, k -> createAndAddCameraScreen(k));
        cameraScreens.computeIfAbsent(camSlot, k -> createAndAddCameraScreen(k));

        // Lưu tên sinh viên để hiển thị keylog
        studentNames.put(studentNum, name);

        // Cập nhật lại giao diện một lần cuối
        cameras.revalidate();
        cameras.repaint();
    }

    /**
     * === HÀM ĐÃ SỬA: Xóa cả 2 slot ===
     * studentNum ở đây là ID gốc (0, 1, 2...).
     */
    public void deleteStudent(int studentNum) {
        int screenSlot = studentNum * 2;
        int camSlot = studentNum * 2 + 1;

        // Lấy panel từ Map
        StudentDisplayPanel screenPanel = cameraScreens.get(screenSlot);
        StudentDisplayPanel camPanel = cameraScreens.get(camSlot);

        // Xóa khỏi JPanel chính
        if (screenPanel != null) {
            cameras.remove(screenPanel);
            cameraScreens.remove(screenSlot); // Xóa khỏi Map
        }
        if (camPanel != null) {
            cameras.remove(camPanel);
            cameraScreens.remove(camSlot); // Xóa khỏi Map
        }

        // Xóa tên sinh viên
        studentNames.remove(studentNum);

        // Cập nhật giao diện
        if (screenPanel != null || camPanel != null) {
            cameras.revalidate();
            cameras.repaint();
        }
    }

    // Hàm thêm chat (Giữ nguyên)
    public void addText(String txt) {
        chatPn.addText(txt);
    }

    /**
     * === HÀM ĐÃ SỬA: Lấy tên từ Map ===
     */
    public void addKeyLog(int studentNum, String duration, String keys) {
        // Lấy tên sinh viên từ Map (dùng ID gốc)
        String name = studentNames.getOrDefault(studentNum, "(Student)");
        String msg = duration + ": " + studentNum + ". " + name + " has typed: " + keys;
        keyPn.addText(msg);
    }
}