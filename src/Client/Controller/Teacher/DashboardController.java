package Client.Controller.Teacher;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import Client.Constant;
import commom.model.Participant;
import commom.model.Test;
import commom.model.User;
import Client.commom.Util.Service;
import Client.View.Home;
import Client.View.TeacherDashboard;
import Client.View.KeyLog.KeyLog;

public class DashboardController {
	public User user;
	private DatagramSocket udpStreamSocket;
	public TeacherDashboard view;

	public DashboardController(User user) {
		this.user = user;
		try {
			this.udpStreamSocket = new DatagramSocket();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.view = new TeacherDashboard(this);
	}

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public List<Test> getListLSCT() {
		List<Test> listData = new ArrayList<Test>();
		String sendMsg = "E," + user.getId();
		try (Socket soc = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataInputStream dis = new DataInputStream(soc.getInputStream());
				DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {
			dos.writeUTF(sendMsg);
			String receiveMsg = dis.readUTF();
			if (!"0".equals(receiveMsg)) {
				String[] splitMsg = receiveMsg.split("\\|");
				for (String s : splitMsg) {
					String[] data = s.split(",");
					Test t = new Test(Integer.parseInt(data[0]), user.getId(), data[1], dateFormat.parse(data[2]));
					listData.add(t);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return listData;
	}

	public void batdau(String tencuocthi) {
		try (Socket tcpStreamSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataOutputStream dos = new DataOutputStream(tcpStreamSocket.getOutputStream());
				DataInputStream dis = new DataInputStream(tcpStreamSocket.getInputStream())) {
			String msg = "C" + udpStreamSocket.getLocalPort() + " " + user.getId() + " " + tencuocthi, roomId = null;
			dos.writeUTF(msg);
			roomId = dis.readUTF();
			view.setVisible(false);
			new Client.Controller.Teacher.TeacherController(this, tencuocthi, roomId, udpStreamSocket);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void capnhatmatkhau(String matkhau) {
		if (matkhau.equals("")) {
			if (Service.confirmAlert("Bạn chắc không", "Thông báo") == 0) {
				String msg = "U," + user.getId() + "," + matkhau;
				try (Socket tcpStreamSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
						DataOutputStream dos = new DataOutputStream(tcpStreamSocket.getOutputStream());
						DataInputStream dis = new DataInputStream(tcpStreamSocket.getInputStream())) {
					dos.writeUTF(msg);
					Service.showAlert("Bạn đã đổi mật khẩu thành công", "Thông báo");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			Service.showAlert("Bạn chưa nhập mật khẩu mới", "Thông báo lỗi");
		}
	}

	public void logout() {
		view.dispose();
		new Home().setVisible(true);
	}

	public List<Participant> getListParticipant(int test_id) {
		List<Participant> listData = new ArrayList<>();
		String sendMsg = "P," + test_id;
		try (Socket soc = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataInputStream dis = new DataInputStream(soc.getInputStream());
				DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {
			dos.writeUTF(sendMsg);
			String receiveMsg = dis.readUTF();
			if (!"0".equals(receiveMsg)) {
				String[] splitMsg = receiveMsg.split("\\|");
				for (String s : splitMsg) {
					String[] data = s.split(",");
					Participant t = new Participant(Integer.parseInt(data[0]), test_id, data[1]);
					listData.add(t);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return listData;
	}

	public void showKeys(Integer participant_id) {
		if (participant_id == null)
			return;
		String msg = "K" + participant_id;
		try (Socket soc = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataInputStream dis = new DataInputStream(soc.getInputStream());
				DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {
			dos.writeUTF(msg);
			String receiveMsg = dis.readUTF();
			new KeyLog(receiveMsg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void showVideo(Integer participant_id) {
		if(participant_id == null) {
			return;
		}
		String msg = "V" + participant_id;
		try (Socket soc = new Socket(Constant.serverAddress, Constant.tcpPort);
				DataInputStream dis = new DataInputStream(soc.getInputStream());
				DataOutputStream dos = new DataOutputStream(soc.getOutputStream())) {
			dos.writeUTF(msg);
			String receiveMsg = dis.readUTF();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
//	public void downloadStudentSubmission(int participantId) {
//	    new Thread(() -> {
//	        try (java.net.Socket tcpSocket = new java.net.Socket(Client.Constant.serverAddress, Client.Constant.tcpPort);
//	             java.io.DataOutputStream dos = new java.io.DataOutputStream(tcpSocket.getOutputStream());
//	             java.io.DataInputStream dis = new java.io.DataInputStream(tcpSocket.getInputStream())) {
//	            
//	            // Gửi lệnh A (Answer) + ID sinh viên
//	            dos.writeUTF("I" + participantId);
//	            
//	            String status = dis.readUTF();
//	            if ("OK".equals(status)) {
//	                String fileName = dis.readUTF();
//	                long fileSize = dis.readLong();
//	                
//	                // Chọn nơi lưu
//	                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
//	                fileChooser.setSelectedFile(new java.io.File(fileName));
//	                
//	                if (fileChooser.showSaveDialog(view) == javax.swing.JFileChooser.APPROVE_OPTION) {
//	                    java.io.File saveFile = fileChooser.getSelectedFile();
//	                    
//	                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile)) {
//	                        byte[] buffer = new byte[4096];
//	                        long totalRead = 0;
//	                        int read;
//	                        while (totalRead < fileSize && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
//	                            fos.write(buffer, 0, read);
//	                            totalRead += read;
//	                        }
//	                    }
//	                    javax.swing.JOptionPane.showMessageDialog(view, "Tải bài làm thành công!");
//	                    java.awt.Desktop.getDesktop().open(saveFile);
//	                }
//	            } else {
//	                javax.swing.JOptionPane.showMessageDialog(view, "Sinh viên này chưa nộp bài!", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
//	            }
//	            
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	            javax.swing.JOptionPane.showMessageDialog(view, "Lỗi tải bài: " + e.getMessage());
//	        }
//	    }).start();
//	}
	
	public void previewStudentSubmission(int participantId) {
		new Thread(() -> {
            try (Socket tcpSocket = new Socket(Constant.serverAddress, Constant.tcpPort);
                 DataOutputStream dos = new DataOutputStream(tcpSocket.getOutputStream());
                 DataInputStream dis = new DataInputStream(tcpSocket.getInputStream())) {
            	dos.writeUTF("I" + participantId);
            	String res = dis.readUTF();
            	if("OK".equals(res)) {
            		String fileName = dis.readUTF();
            		long fileSize = dis.readLong();
            		
            		ByteArrayOutputStream baos = new ByteArrayOutputStream();
            		byte[] buffer = new byte[4096];
            		long totalRead = 0;
                    int read;
                    while (totalRead < fileSize && (read = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                        baos.write(buffer, 0, read);
                        totalRead += read;
                    }
                    
                    byte[] fileData = baos.toByteArray();
                    
                    SwingUtilities.invokeLater(() -> {
                    	view.showSubmissionDialog(fileName, fileData);
                    });
            	} else {
            		SwingUtilities.invokeLater(() -> {
                        Service.showAlert("Sinh viên này chưa nộp bài!", "Thông báo");
                   });
            	}
            } catch(Exception e) {
            	e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    Service.showAlert("Lỗi tải bài: " + e.getMessage(), "Lỗi");
                });
            }
		}).start();
	}
}
