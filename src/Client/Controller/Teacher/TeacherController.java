package Client.Controller.Teacher;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import Client.Constant;
import Client.Controller.InContestBaseController;
import commom.dto.ImageModel;
import commom.model.Packet;
import Client.View.TeacherInContest;

public class TeacherController extends InContestBaseController{
	
	public Queue<Packet> packets = new ConcurrentLinkedQueue<>();
	public Map<Integer, ArrayList<ImageModel>> images = new ConcurrentHashMap<>();
	public ImageModel currImage;
	public ArrayList<ImageModel> viewList = new ArrayList<>();
	public TeacherInContest view;
	public DashboardController dashboardController;
		
	public TeacherController(DashboardController dashboardController, String name, String roomId, DatagramSocket udpSocket) {
		super();
		this.dashboardController = dashboardController;
		this.name = name;
		this.roomId = roomId;
		this.udpSocket = udpSocket;
		view = new TeacherInContest(this);
		new ReceiveThread(this).start();
		new LiveThread(this).start();
	}
	
	public void endStream() {
		running = false;
		try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream())) {
			dos.writeUTF("Q" + roomId);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		view.dispose();
		dashboardController.view.setVisible(true);
	}
	
	public void focus(int studentNum) {
		try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream())) {
			dos.writeUTF("H" + roomId + " " + studentNum);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void setImage(byte[] img, int cameraNum) {
		view.setImage(img, cameraNum);
	}
	
	public void addStudent(int studentNum, String name) {
		view.addStudent(studentNum, name);
	}
	
	public void deleteStudent(int studentNum) {
		view.deleteStudent(studentNum);
	}
	
	public void addText(String txt) {
		view.addText(txt);
	}
	
	public void addKeyLog(String txt) {
		String[] splits = txt.split(" ");
		int studentNum = Integer.parseInt(splits[0]);
		String start = splits[1];
		String end = splits[2];
		String res = "";
		for(int k = 4; k < splits.length; k += 2) res += splits[k];
		view.addKeyLog(studentNum, start + "-" + end, res + "\n");
	}

	public void kickStudent(int studentID) {
		try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream())) {
			dos.writeUTF("X" + roomId + " " + studentID);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	public void sendWarning(int studentID, String message) {
		String output = "W" + roomId + " " + studentID + " " + message;
	    
	    try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
	            DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream())) {
	        dos.writeUTF(output);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void uploadExamFile(File file) {
	    // Chạy trong Thread mới để không làm đơ giao diện khi đang upload file nặng
	    new Thread(() -> {
	        try (java.net.Socket uploadSocket = new java.net.Socket(Client.Constant.serverAddress, Client.Constant.tcpPort);
	             java.io.DataOutputStream dos = new java.io.DataOutputStream(uploadSocket.getOutputStream())) {

	            dos.writeUTF("A" + roomId);

	            // 2. Gửi thông tin file
	            dos.writeUTF(file.getName());
	            dos.writeLong(file.length());

	            // 3. Gửi nội dung file
	            try (FileInputStream fis = new FileInputStream(file)) {
	                byte[] buffer = new byte[4096];
	                int read;
	                //Gửi từng gói qua mạng
	                while ((read = fis.read(buffer)) != -1) {
	                    dos.write(buffer, 0, read);
	                }
	            }
	            dos.flush();
	            javax.swing.JOptionPane.showMessageDialog(view, "Upload đề thi thành công!", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);

	        } catch (Exception e) {
	            e.printStackTrace();
	            javax.swing.JOptionPane.showMessageDialog(view, "Lỗi upload: " + e.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
	        }
	    }).start();
	}

	public void sendLockSignal(boolean isLock) {
		try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
	            DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream())) {
	        if(isLock) {
	        	dos.writeUTF("Z" + " " + roomId);
	        }
	        else {
	        	dos.writeUTF("O" + " " + roomId);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}