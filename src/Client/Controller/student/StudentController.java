package Client.Controller.student;

import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import Client.Constant;
import Client.View.Home;
import Client.View.StudentlnContest;
import Client.Controller.*;
import Client.commom.DTO.InContest.ScreenImageDTO;

public class StudentController extends InContestBaseController {
	 public StudentlnContest view;
	 public ScreenImageDTO imgModel = new ScreenImageDTO(Constant.NORMAL_WIDTH, Constant.NORMAL_HEIGHT);
	 public Queue<BufferedImage> screenQueue = new ConcurrentLinkedQueue<>();
	 public Queue<Mat> camQueue = new ConcurrentLinkedQueue<>();
	 public Size camDim = new Size(Constant.NORMAL_WIDTH, Constant.NORMAL_HEIGHT);
	 public String currKeys = "";
	
	 public StudentController() {
	     try {
	         udpSocket = new DatagramSocket();
	     } catch (IOException e) {
	         System.exit(1);
	     }
	     view = new StudentlnContest(this);
	 }
	
	 public String joinRoom(String name, String roomId) {
		String msg = "J" + roomId + " " + udpSocket.getLocalPort() + " " + name, res = null;
		try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream());
				DataInputStream dis = new DataInputStream(tcpSocket.getInputStream());) {
			dos.writeUTF(msg); //Gửi thông điệp yêu cầu tham gia đến server
			res = dis.readUTF(); //Chờ và đọc thông điệp phản hồi từ server
			if (res.startsWith("Y"))  {
				int i = res.indexOf(" ");
				id = res.substring(1, i);
				res = res.substring(i + 1);
				//id là tên sinh viên, res là tên giáo viên
				this.roomId = roomId;
				this.name = name;
			} else
				res = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return res;
	}
	
	 public void startThreads() {
			new CaptureThread(this, true).start();
			new CaptureThread(this, false).start();
			new SendThread(this, true).start();
			new SendThread(this, false).start();
			new LiveThread(this).start();
		}
	
	 public void addText(String txt) {
	     view.addText(txt);
	 }
	
	 public void endStream() {
	     view.dispose();
	     new Home().setVisible(true);
	 }
	public void handleFocus(int width, int height) {
		ScreenImageDTO img = new ScreenImageDTO(width, height);
		ScreenImageDTO curr = imgModel; // Lưu lại khung ảnh cũ thành curr
		imgModel = img; // Thay thế khung ảnh 
		curr.g2d.dispose(); 
	}
	 public void back() {
	     view.dispose();
	     new Home().setVisible(true);
	 }
}