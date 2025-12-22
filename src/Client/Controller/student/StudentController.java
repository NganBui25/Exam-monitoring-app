package Client.Controller.student;

import java.awt.image.BufferedImage;
import Server.controller.Server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
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
import commom.dto.Room;
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
	
	private final String[] BLACKLIST = {"chrome", "browser", "coccoc", "firefox", "zalo", "discord", "teamviewer", "anydesk", "ultraviewer", "edge"};
	private long lastWarningTime = 0;
	
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
			} else if(res.equals("LOCKED")) {
				return "LOCKED";
			}
			else {
				res = null;
			}
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
		startBlacklistScanner();
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
		this.running = false;
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
	            
	            dos.writeUTF("G" + roomId);

	            String status = dis.readUTF();
	            
	            if ("OK".equals(status)) {
	                String fileName = dis.readUTF();
	                long fileSize = dis.readLong();
	                
	                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
	                fileChooser.setSelectedFile(new java.io.File(fileName)); // Gợi ý tên file gốc
	                fileChooser.setDialogTitle("Chọn nơi lưu đề thi");
	                
	                int userSelection = fileChooser.showSaveDialog(view);
	                
	                if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
	                    java.io.File saveFile = fileChooser.getSelectedFile();
	                    
	                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile)) {
	                        byte[] buffer = new byte[4096];
	                        long totalRead = 0;
	                        int read;
	                        while (totalRead < fileSize && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
	                            fos.write(buffer, 0, read);
	                            totalRead += read;
	                        }
	                    }
	                    
	                    javax.swing.JOptionPane.showMessageDialog(view, 
	                        "Tải đề thi thành công!\nLưu tại: " + saveFile.getAbsolutePath(), 
	                        "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
	                        
	                    try {
	                        java.awt.Desktop.getDesktop().open(saveFile);
	                    } catch (Exception ex) { }
	                }
	            } else {
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
	            
	            dos.writeUTF("N" + id);
	            
	            dos.writeUTF(file.getName());
	            dos.writeLong(file.length());
	            
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
	public void sendRaiseHand() {
		if(this.id != null) {
			try (Socket s = new Socket(Client.Constant.serverAddress, Client.Constant.tcpPort);
			         DataOutputStream dos = new DataOutputStream(s.getOutputStream())) {
			        dos.writeUTF("#" + roomId + " " + this.id);
			    } catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public void startBlacklistScanner() {
		Thread t = new Thread(() -> {
			System.out.println("Bắt đầu quét process...");
			while (running) {
				try {
					// Gọi hàm quét và diệt
					scanAndKill();
					
					// Nghỉ 5 giây rồi quét tiếp
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	// Hàm riêng thực hiện logic quét và diệt
	private void scanAndKill() {
		try {
			// Sử dụng /fo csv /nh để lấy định dạng CSV, dễ tách tên hơn
			Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\tasklist.exe /fo csv /nh");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			
			Set<String> detectedApps = new HashSet<>();

			while ((line = reader.readLine()) != null) {
				if(line.trim().isEmpty()) continue;
				
				// Line dạng: "chrome.exe","1234","Console",...
				// Tách lấy phần tử đầu tiên là tên
				String[] parts = line.split(",");
				String processName = parts[0].replace("\"", "").trim(); // Bỏ dấu ngoặc kép -> chrome.exe
				String processNameLower = processName.toLowerCase();

				for (String keyword : BLACKLIST) {
					// Bỏ qua Edge WebView (Tiến trình hệ thống của Window)
					if (processNameLower.contains("edge") && line.toLowerCase().contains("webview")) {
						continue;
					}
					if (processNameLower.contains("crashhandler") || processNameLower.contains("service")) {
				        continue; 
				    }

					// Nếu tên process chứa từ khóa cấm (ví dụ "chrome.exe" chứa "chrome")
					if (processNameLower.contains(keyword)) {
						// 1. DIỆT NGAY LẬP TỨC
						killProcess(processName);
						
						// 2. Thêm vào danh sách đã diệt để tí nữa báo cáo
						detectedApps.add(processName);
					}
				}
			}
			reader.close();

			// Nếu có diệt được thằng nào -> Gửi cảnh báo 1 lần
			if (!detectedApps.isEmpty()) {
				// Chỉ gửi cảnh báo nếu cách lần trước > 5 giây để tránh spam server
				if(System.currentTimeMillis() - lastWarningTime > 5000) {
					String msgList = String.join(", ", detectedApps);
					System.out.println("Auto-Kill: " + msgList);
					sendWarning(msgList); // Gửi về server
					lastWarningTime = System.currentTimeMillis();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Hàm thực thi lệnh kill
	public void killProcess(String processName) {
		try {
			String cmd = "taskkill /F /T /IM \"" + processName + "\"";
	        
	        Process p = Runtime.getRuntime().exec(cmd);
	        BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        String line;
	        while ((line = errorReader.readLine()) != null) {
	            // Chỉ in ra nếu có lỗi thực sự
	            if(!line.trim().isEmpty()) {
	                System.err.println("Kill Error (" + processName + "): " + line);
	            }
	        }
	        
	        // Đợi lệnh chạy xong để chắc chắn
	        p.waitFor();
		} catch (Exception e) {
			// Không in stacktrace để tránh rác console nếu không kill được
		}
	}
	private void sendWarning(String appName) {
		if(this.id != null) {
			String msg = "-" + roomId + " " + this.id + " " + appName;
			try (Socket s = new Socket(Client.Constant.serverAddress, Client.Constant.tcpPort);
			         DataOutputStream dos = new DataOutputStream(s.getOutputStream())) {
			        dos.writeUTF(msg);
			    } catch (Exception e) { e.printStackTrace(); }
		}
	}
}