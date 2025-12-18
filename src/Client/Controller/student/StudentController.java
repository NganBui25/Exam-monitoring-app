package Client.Controller.student;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import Client.Constant;
import Client.Controller.InContestBaseController;
import commom.dto.ImageModel;
import commom.dto.ScreenImageDTO;
import Client.View.Home;
import Client.View.StudentlnContest;

public class StudentController extends InContestBaseController {
	static {
	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
}
	

	public StudentlnContest view;
	public ScreenImageDTO imgModel = new ScreenImageDTO(Constant.NORMAL_WIDTH, Constant.NORMAL_HEIGHT);
	public Queue<BufferedImage> screenQueue = new ConcurrentLinkedQueue<BufferedImage>();
	public Size camDim = new Size(Constant.NORMAL_WIDTH, Constant.NORMAL_HEIGHT);
	public Mat camImg = new Mat();
	public Mat frame = new Mat();
	public Queue<Mat> camQueue = new ConcurrentLinkedQueue<Mat>();
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
			dos.writeUTF(msg);
			res = dis.readUTF();
			if (res.startsWith("Y")) {
				int i = res.indexOf(" ");
				id = res.substring(1, i);
				res = res.substring(i + 1);
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
		
		new CameraStudentThread().start();
	}

	public void handleFocus(int width, int height) {
		ScreenImageDTO img = new ScreenImageDTO(width, height);
		ScreenImageDTO curr = imgModel;
		imgModel = img;
		curr.g2d.dispose();
	}

	public void addText(String txt) {
		view.addText(txt);
	}

	public void endStream() {
		view.dispose();
		new Home().setVisible(true);
	}

	public void back() {
		view.dispose();
		new Home().setVisible(true);
	}
	private BufferedImage MatToBufferedImage(Mat matrix) {
		if(matrix == null || matrix.empty()) return null;
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		byte[] byteArray = mob.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return bufImage;
	}
	
	private class CameraStudentThread extends Thread{
		public void run() {
			while(running) {
				try {
					if(frame != null && !frame.empty()) {
						BufferedImage image = MatToBufferedImage(frame);
						
						// 2. Cập nhật lên giao diện (Phải dùng SwingUtilities.invokeLater để an toàn luồng UI)
	                    if (image != null && view != null && view.cameraScreen != null) {
	                        SwingUtilities.invokeLater(() -> {
	                            // Kiểm tra lại lần nữa để tránh lỗi khi cửa sổ bị đóng đột ngột
	                            if (view.isVisible()) {
	                                // Kỹ thuật resize ảnh cho vừa với khung chứa (tùy chọn, nếu muốn ảnh không bị méo)
	                                // Image scaledImg = image.getScaledInstance(view.cameraScreen.getWidth(), view.cameraScreen.getHeight(), Image.SCALE_SMOOTH);
	                                // view.cameraScreen.setIcon(new ImageIcon(scaledImg));
	                                
	                                // Cách đơn giản nhất: Set thẳng ảnh gốc vào
	                                view.cameraScreen.setIcon(new ImageIcon(image));
	                                view.cameraScreen.setText(""); 
	                            }
	                        });
	                    }
	                }
	                Thread.sleep(33);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void downloadExamFile() {
	    new Thread(() -> {
	        try (java.net.Socket tcpSocket = new java.net.Socket(Client.Constant.serverAddress, Client.Constant.tcpPort);
	             java.io.DataOutputStream dos = new java.io.DataOutputStream(tcpSocket.getOutputStream());
	             java.io.DataInputStream dis = new java.io.DataInputStream(tcpSocket.getInputStream())) {
	            
	            // 1. Gửi lệnh G + RoomID (roomId là biến có sẵn trong controller)
	            dos.writeUTF("G" + roomId);
	            
	            // 2. Nhận phản hồi từ Server
	            String status = dis.readUTF();
	            
	            if ("OK".equals(status)) {
	                // 3. Đọc thông tin file
	                String fileName = dis.readUTF();
	                long fileSize = dis.readLong();
	                
	                // 4. Mở hộp thoại để sinh viên chọn nơi lưu
	                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
	                fileChooser.setSelectedFile(new java.io.File(fileName)); // Gợi ý tên file gốc
	                fileChooser.setDialogTitle("Chọn nơi lưu đề thi");
	                
	                int userSelection = fileChooser.showSaveDialog(view);
	                
	                if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
	                    java.io.File saveFile = fileChooser.getSelectedFile();
	                    
	                    // 5. Ghi file xuống ổ cứng
	                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile)) {
	                        byte[] buffer = new byte[4096];
	                        long totalRead = 0;
	                        int read;
	                        // Đọc byte từ mạng ghi vào file
	                        while (totalRead < fileSize && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
	                            fos.write(buffer, 0, read);
	                            totalRead += read;
	                        }
	                    }
	                    
	                    javax.swing.JOptionPane.showMessageDialog(view, 
	                        "Tải đề thi thành công!\nLưu tại: " + saveFile.getAbsolutePath(), 
	                        "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
	                        
	                    // Tùy chọn: Tự động mở file sau khi tải
	                    try {
	                        java.awt.Desktop.getDesktop().open(saveFile);
	                    } catch (Exception ex) { /* Không mở được thì thôi */ }
	                }
	            } else {
	                // Server trả về NO_FILE hoặc lỗi khác
	                javax.swing.JOptionPane.showMessageDialog(view, 
	                    "Giáo viên chưa upload đề thi nào cho phòng này!", 
	                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
	            }
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            javax.swing.JOptionPane.showMessageDialog(view, "Lỗi kết nối: " + ex.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
	        }
	    }).start();
	}
	public void submitExam(File file) {
	    new Thread(() -> {
	        try (java.net.Socket tcpSocket = new java.net.Socket(Client.Constant.serverAddress, Client.Constant.tcpPort);
	             java.io.DataOutputStream dos = new java.io.DataOutputStream(tcpSocket.getOutputStream())) {
	            
	            // Gửi lệnh N + ID (Giả sử biến 'id' trong controller là participant_id)
	            dos.writeUTF("N" + id);
	            
	            // Gửi thông tin file
	            dos.writeUTF(file.getName());
	            dos.writeLong(file.length());
	            
	            // Gửi nội dung
	            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
	                byte[] buffer = new byte[4096];
	                int read;
	                while ((read = fis.read(buffer)) != -1) {
	                    dos.write(buffer, 0, read);
	                }
	            }
	            dos.flush();
	            javax.swing.JOptionPane.showMessageDialog(view, "Nộp bài thành công!");
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            javax.swing.JOptionPane.showMessageDialog(view, "Lỗi nộp bài: " + e.getMessage());
	        }
	    }).start();
	}
}