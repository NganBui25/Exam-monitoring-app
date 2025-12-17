package Client.View;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	public StudentlnContest(StudentController controller) {
		this.controller = controller;
		setLayout(new BorderLayout());

		cameraScreen = new JLabel();
		cameraScreen.setText("Đang tải camera...");
        cameraScreen.setHorizontalAlignment(JLabel.CENTER);

        JPanel topPn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Nút tải đề
        JButton btnDownload = new JButton("Tải đề thi");
        btnDownload.setFocusable(false);
        btnDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.downloadExamFile();
            }
        });
        
        JButton btnSubmit = new JButton("Nộp bài");
        btnSubmit.addActionListener(e -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                int confirm = javax.swing.JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc muốn nộp file: " + file.getName() + "?", "Xác nhận", javax.swing.JOptionPane.YES_NO_OPTION);
                if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                    controller.submitExam(file);
                }
            }
        });
        btnDownload.setVisible(false);
        btnSubmit.setVisible(false);
        // Thêm btnSubmit vào thanh công cụ (topPn)
        topPn.add(btnSubmit);
        

        topPn.add(javax.swing.Box.createHorizontalStrut(20)); // Khoảng cách
        topPn.add(btnDownload);
		JButton backBtn = new JButton("\u2190");
		topPn.add(backBtn, BorderLayout.WEST);
		backBtn.addActionListener(e -> controller.back());
		JPanel topCenterPn = new JPanel(new FlowLayout());
		JLabel lbName = new JLabel("Name:");
		JTextField nameTf = new JTextField(10);
		JLabel lbRoomId = new JLabel("Room ID:");
		JTextField roomIdTf = new JTextField(10);
		JButton button = new JButton("Join");
		JLabel lbWarning = new JLabel("Khong ton tai phong");
		JLabel teacher = new JLabel();
		lbWarning.setVisible(false);
		topCenterPn.add(teacher);
		topCenterPn.add(lbName);
		topCenterPn.add(nameTf);
		topCenterPn.add(lbRoomId);
		topCenterPn.add(roomIdTf);
		topCenterPn.add(button);
		topCenterPn.add(lbWarning);

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				roomIdTf.setEditable(false);
				nameTf.setEditable(false);
				button.setEnabled(false);
				String msg = controller.joinRoom(nameTf.getText(), roomIdTf.getText());
				if (msg != null) {
					btnDownload.setVisible(true);
			        btnSubmit.setVisible(true);
					teacher.setText("Contest name: " + msg);
					lbName.setVisible(false);
					lbRoomId.setVisible(false);
					lbWarning.setVisible(false);
					backBtn.setVisible(false);
					startCapture();
				} else {
					roomIdTf.setEditable(true);
					nameTf.setEditable(true);
					button.setEnabled(true);
					lbWarning.setVisible(true);
				}
			}
		});

		topPn.add(topCenterPn, BorderLayout.CENTER);
		chatPn = new ChatPanel(controller);

		add(cameraScreen, BorderLayout.CENTER);
		add(topPn, BorderLayout.NORTH);
		add(chatPn, BorderLayout.EAST);

		setSize(1000, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
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

	public void showWarning(String message) {
		javax.swing.JOptionPane.showMessageDialog(this, 
		        "Bạn đã bị giám thị cảnh báo", 
		        "CẢNH BÁO TỪ GIÁM THỊ", 
		        javax.swing.JOptionPane.WARNING_MESSAGE);
	}
}