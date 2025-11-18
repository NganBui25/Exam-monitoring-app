package Client.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import Client.Controller.Teacher.TeacherController;
import Client.View.Utils.ChatPanel;
import Client.View.Utils.LogPanel;

public class TeacherInContest extends JFrame {

	public TeacherController controller;
	
	public ArrayList<JLabel> cameraScreens = new ArrayList<JLabel>();
	public JPanel cameras;
	public ChatPanel chatPn;
	public LogPanel keyPn;

	public TeacherInContest(TeacherController controller) {
		this.controller = controller;
		setLayout(new BorderLayout());

		JPanel topPn = new JPanel(new BorderLayout()); // Đổi sang BorderLayout
		topPn.setBackground(new Color(45, 62, 80)); // Màu xanh đậm

		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
		titlePanel.setOpaque(false); // Làm trong suốt để lấy nền của topPn

		JLabel contestLabel = new JLabel("Kỳ thi: " + controller.name);
		contestLabel.setForeground(Color.WHITE); // Chữ trắng
		titlePanel.add(contestLabel);

		JLabel roomLabel = new JLabel("Mã phòng: " + controller.roomId);
		roomLabel.setForeground(Color.WHITE); // Chữ trắng
		titlePanel.add(roomLabel);
		
		topPn.add(titlePanel, BorderLayout.WEST);

		JButton ketthuc = new JButton("Kết thúc giám sát");
		styleDangerButton(ketthuc);
		ketthuc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.endStream();
			}
		});
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		buttonPanel.setOpaque(false);
		buttonPanel.add(ketthuc);
		topPn.add(buttonPanel, BorderLayout.EAST); 
		
		JScrollPane mainPn = new JScrollPane();
		cameras = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
		cameras.setPreferredSize(new Dimension(500, 10000));
		cameras.setBackground(new Color(236, 236, 236)); // Màu xám nhạt như trong ảnh
		mainPn.setViewportView(cameras);
		mainPn.setBorder(null);
		
		JPanel rightPn = new JPanel(new BorderLayout());
		rightPn.setPreferredSize(new Dimension(350, 1000));
		keyPn = new LogPanel("Keyboard");
		rightPn.add(keyPn, BorderLayout.CENTER);
		chatPn = new ChatPanel(controller);
		rightPn.add(chatPn, BorderLayout.SOUTH);

		add(mainPn, BorderLayout.CENTER);
		add(topPn, BorderLayout.NORTH); // Thêm top panel mới
		add(rightPn, BorderLayout.EAST);

		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void addCameraScreen() {
		JLabel cameraScreen = new JLabel();
		cameraScreen.setBorder(new LineBorder(Color.BLACK, 1));
		cameraScreen.setHorizontalTextPosition(JLabel.CENTER);
		cameraScreen.setVerticalTextPosition(JLabel.TOP);

		cameraScreens.add(cameraScreen);
		cameras.add(cameraScreen);
		cameraScreen.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int panelIndex = cameraScreens.indexOf(cameraScreen);
				int studentNum = panelIndex/2;
				if(e.getButton() == MouseEvent.BUTTON1) {
					controller.focus(panelIndex);
				}
				if(e.getButton() == MouseEvent.BUTTON3) {
					showKickMenu(e, studentNum);
				}
			}
		});
	}
	private void showKickMenu(MouseEvent e, int studentNum) {
		JPopupMenu contextMenu = new JPopupMenu();
		String studentName = "Thí sinh" + studentNum;
		try {
			String fullText = cameraScreens.get(studentNum * 2).getText();
			studentName = fullText.substring(fullText.indexOf('.') + 2, fullText.indexOf(" -"));
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		JMenuItem warnItem = new JMenuItem("Cảnh báo " + studentName);
        warnItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        warnItem.setForeground(new Color(230, 126, 34)); // Màu cam
        
        warnItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String message = "Bạn đang vi phạm quy chế thi! Yêu cầu nghiêm túc làm bài";
        		controller.warnStudent(studentNum, message);
        	}
        });
        contextMenu.add(warnItem);
        contextMenu.addSeparator();
        
        JMenuItem kickItem = new JMenuItem("Kick (Đuổi) " + studentName);
        kickItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        kickItem.setForeground(Color.RED);
        
        kickItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent ae) {
                controller.deleteStudent(studentNum);
            }
        });
        contextMenu.add(kickItem);
        contextMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public void removeCameraScreen(int studentNum) {
		cameras.remove(cameraScreens.get(studentNum));
		repaint();
	}
	
	public void setImage(byte[] img, int cameraNum) {
		while(cameraNum >= cameraScreens.size()) addCameraScreen();
		JLabel screen = cameraScreens.get(cameraNum);
		screen.setIcon(new ImageIcon(img));
	}
	
	public void addStudent(int studentNum, String name) {
		while (studentNum * 2 + 1 >= cameraScreens.size())
			addCameraScreen();
		cameraScreens.get(studentNum * 2).setText(studentNum + ". " + name + " - MH");
		cameraScreens.get(studentNum * 2 + 1).setText(studentNum + ". " + name + " - Face cam");
	}
	
	public void deleteStudent(int studentNum) {
		removeCameraScreen(studentNum * 2);
		removeCameraScreen(studentNum * 2 + 1);
	}
	
	public void addText(String txt) {
		chatPn.addText(txt);
	}
	
	public void addKeyLog(int studentNum, String duration, String keys) {
		String cameraText = cameraScreens.get(studentNum * 2).getText();
		int i = cameraText.indexOf(' ');
		int j = cameraText.indexOf(' ', i + 1);
		String msg = duration + ": " + studentNum + ". " + cameraText.substring(i + 1, j) + " has typed: " + keys;
		keyPn.addText(msg);
	}
	private void styleDangerButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(new Color(220, 53, 69)); 
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}