package Client.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import Client.Controller.Teacher.TeacherController;
import Client.View.Utils.ChatPanel;
import Client.View.Utils.LogPanel;

// Import Panel "thông minh" mà chúng ta đã tạo
import Client.View.StudentDisplayPanel; 

public class TeacherInContest extends JFrame {

    public TeacherController controller;
    
    // Đã đổi sang Panel "thông minh"
    public ArrayList<StudentDisplayPanel> cameraScreens = new ArrayList<>();
    
    public JPanel cameras;
    public ChatPanel chatPn;
    public LogPanel keyPn;

    public TeacherInContest(TeacherController controller) {
        // ... (Toàn bộ code constructor của bạn giữ nguyên) ...
		this.controller = controller;
		setLayout(new BorderLayout());

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
				//controller.endStream();
			}
		});

		add(bottomCenterPanel, BorderLayout.SOUTH);
		add(mainPn, BorderLayout.CENTER);
		add(topPn, BorderLayout.NORTH);
		add(rightPn, BorderLayout.EAST);

		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
    }

    /**
     * === HÀM ĐÃ SỬA (THÊM revalidate) ===
     * Hàm này được gọi khi cần thêm 1 slot camera mới
     */
    public void addCameraScreen() {
        StudentDisplayPanel cameraScreen = new StudentDisplayPanel(); 
        cameraScreen.setBorder(new LineBorder(Color.BLACK, 1));

        cameraScreens.add(cameraScreen);
        cameras.add(cameraScreen);
        cameraScreen.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                controller.focus(cameraScreens.indexOf(cameraScreen));
            }
        });
        
        // === THÊM 2 DÒNG NÀY ===
        // Báo cho layout manager tính toán lại kích thước
        cameras.revalidate();
        // Yêu cầu Swing vẽ lại panel 'cameras'
        cameras.repaint();
    }

    public void removeCameraScreen(int studentNum) {
        cameras.remove(cameraScreens.get(studentNum));
        // Cũng cần revalidate khi xóa
        cameras.revalidate();
        cameras.repaint();
    }
    
    // 2 hàm mới (cho Keyframe và Delta) - Giữ nguyên
    public void updateKeyframeForStudent(int studentNum, byte[] keyframeData) {
        while(studentNum >= cameraScreens.size()) addCameraScreen();
        cameraScreens.get(studentNum).updateKeyframe(keyframeData);
    }
    
    public void drawDeltaTileForStudent(int studentNum, byte[] tileData, int x, int y) {
        if(studentNum < cameraScreens.size()) {
            cameraScreens.get(studentNum).drawDeltaTile(tileData, x, y);
        }
    }
    
    /**
     * === HÀM ĐÃ SỬA (Sửa lỗi logic *2 + 1) ===
     * Giờ đây 1 sinh viên (TCP) chỉ add 1 panel (vì chúng ta chỉ có 1 stream UDP)
     */
    public void addStudent(int studentNum, String name) {
        // Đảm bảo slot cho sinh viên này tồn tại
        while (studentNum >= cameraScreens.size())
            addCameraScreen();
        
        // (Tạm thời chúng ta không hiển thị tên, vì Panel không có setText)
        // (Bạn có thể tạo 1 class "StudentSlot" chứa cả Panel và JLabel tên sau)
    }
    
    /**
     * === HÀM ĐÃ SỬA (Sửa lỗi logic *2) ===
     * Xóa 1 sinh viên (TCP) là xóa 1 panel
     */
    public void deleteStudent(int studentNum) {
        if (studentNum < cameraScreens.size()) {
            removeCameraScreen(studentNum);
        }
    }
    
    public void addText(String txt) {
        chatPn.addText(txt);
    }
    
    public void addKeyLog(int studentNum, String duration, String keys) {
        // Sửa lại logic lấy tên (vì không còn .getText())
        String msg = duration + ": " + studentNum + ". " + " (Student) " + " has typed: " + keys;
        keyPn.addText(msg);
    }
}