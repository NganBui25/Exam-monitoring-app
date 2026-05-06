package Server.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import Server.Constant;
import commom.dao.ParticipantDAO;
import commom.dao.TestDAO;
import commom.dao.UserDAO;
import commom.dto.ClientModel;
import commom.dto.Room;
import commom.model.Participant;
import commom.model.Test;
import commom.model.User;
import Server.Utils.Service;

//xử lý tất cả các yêu cầu được gửi từ 1 client duy nhất thông qua giao thức TCP
public class TCPHandler implements Runnable {
	private Socket socket; //đại diện cho kết nối của 1 client
	private InetAddress address; //địa chỉ IP tương ứng 

	public TCPHandler(Socket socket) {
		this.socket = socket;
		this.address = socket.getInetAddress();
	}

	@Override
	public void run() {
		try (DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
			String message = input.readUTF();
			char msgCode = message.charAt(0);
			switch (msgCode) {
			case 'C': // C<udpPort> <id> <tencuocthi>
				int id = createRoom(message);
				output.writeUTF(id + "");
				break;

			case 'J': // J<roomId> <udpPort> <studentName>
				joinRoom(message, output);
				break;

			case 'H': // H<roomId> <studentNum>
				String[] msges = message.split(" ");
				focus(msges);
				break;

			case 'M': // M<roomId> <text>
				if (!texting(message))
					output.writeUTF("Q");
				break;

			case 'S':
				checkStudent(message, output);
				break;

			case 'T':
				checkTeacher(message, output);
				break;

			case '!': // !<roomId> <...>
				takeKeys(message);
				message = input.readUTF();
				checkStudent(message, output);
				break;

			case 'E':
				handleListTestRequest(message, output);
				break;

			case 'P':
				handleListParticipant(message, output);
				break;

			case 'L': // login : "L,username,password"
				handleLogin(message, output);
				break;

			case 'R': // register : "R,username,password"
				handleRegister(message, output);
				break;

			case 'U': // user update
				handleUpdateUserRequest(message);
				break;

			case 'K': // Lay file ban phim cua participant ve cho teacher xem
				getKeys(message.substring(1), output);
				break;

			case 'Q': // Teacher bam nut ket thuc
				endStream(message);
				break;
			case 'W':
				handlerWarning(message);
				break;
			
			case 'X': // Lệnh Kick (Trục xuất)
                handleKick(message);
                break;
			case 'A':
				receiveExamFile(message.substring(1), input);
				break;
			case 'G':
				sendExamFile(message.substring(1), output);
			    break;
			case 'N':
				receiveSubmission(message.substring(1), input);
			    break;
			case 'I':
				sendStudentSubmission(message.substring(1), output);
			    break;
			case 'Z': //Khóa phòng
				handleLockRoom(message, true);
				break;
			case 'O': //Mở phòng
				handleLockRoom(message, false);
			case '#':
				handleRaiseHand(message);
				break;
			case '-':
				handleWarningGianLan(message);
				break;
			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleWarningGianLan(String message) {
		try {
			int firstSpace = message.indexOf(" ");
			int roomId = Integer.parseInt(message.substring(1, firstSpace));
			
			String remaining = message.substring(firstSpace + 1);
			int secondSpace = remaining.indexOf(" ");
			int studentDbId = Integer.parseInt(remaining.substring(0, secondSpace));
			String appName = remaining.substring(secondSpace + 1);
			
			Room room = Server.rooms.get(roomId);
			if(room != null) {
				ClientModel student = room.getStudents().get(studentDbId);
				if(student != null) {
					int studentNum = student.getStudentNum();
					
					String warnMsg = "WARN " + studentNum + " " + appName;
					room.getWarnings().add(warnMsg);
					System.out.println("Cảnh báo: SV " + studentDbId + " dùng " + appName);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void handleRaiseHand(String msg) {
	    try {
	        String[] parts = msg.split(" ");
	        int roomId = Integer.parseInt(parts[0].substring(1)); 
	        int studentDbId = Integer.parseInt(parts[1]);

	        Room room = Server.rooms.get(roomId);
	        if (room != null) {
	            ClientModel student = room.getStudents().get(studentDbId);
	            if (student != null) {
	                int studentNum = student.getStudentNum();
	                room.getRaisedHands().add(studentNum);
	                System.out.println("Sinh viên " + studentDbId + " (Cam số " + studentNum + ") giơ tay.");
	            }
	        }
	    } catch (Exception e) { e.printStackTrace(); }
	}

	private void handleLockRoom(String message, boolean b) {
		try {
			int roomId = Integer.parseInt(message.substring(1).trim());
			Room room = Server.rooms.get(roomId);
			if(room != null) {
				room.setLocked(b);
				System.out.println("Phòng " + roomId + " trạng thái khóa: " + b);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	// Nhận bài làm từ sinh viên
	private void receiveSubmission(String participantId, DataInputStream input) {
	    try {
	        String fileName = input.readUTF();
	        long fileSize = input.readLong();
	        
	        String folderPath = Constant.FILE_LOCATION + File.separator + "Submissions" + File.separator + participantId + File.separator;
	        File folder = new File(folderPath);
	        if (!folder.exists()) folder.mkdirs();
	        
	        File[] oldFiles = folder.listFiles();
	        if(oldFiles != null) for(File f : oldFiles) f.delete();

	        File file = new File(folderPath + fileName);
	        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
	            byte[] buffer = new byte[4096];
	            long totalRead = 0;
	            int read;
	            while (totalRead < fileSize && (read = input.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
	                fos.write(buffer, 0, read);
	                totalRead += read;
	            }
	        }
	        int pId = Integer.parseInt(participantId);
	        for(Room r : Server.rooms.values()) {
	        	if(r.getStudents().containsKey(pId)) {
	        		r.getNewSubmissions().add(pId);
	        		System.out.println("Đã thêm sinh viên " + pId + " vào danh sách nộp bài");
	        		break;
	        	}
	        }
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	//Gửi bài làm cho giáo viên
	private void sendStudentSubmission(String participantId, DataOutputStream output) {
	    try {
	        String folderPath = Constant.FILE_LOCATION + File.separator + "Submissions" + File.separator + participantId + File.separator;
	        File folder = new File(folderPath);
	        File[] files = folder.listFiles();
	        
	        if (files != null && files.length > 0) {
	            File file = files[0]; 
	            
	            output.writeUTF("OK");
	            output.writeUTF(file.getName());
	            output.writeLong(file.length());
	            
	            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
	                byte[] buffer = new byte[4096];
	                int read;
	                while ((read = fis.read(buffer)) != -1) {
	                    output.write(buffer, 0, read);
	                }
	            }
	            output.flush();
	        } else {
	            output.writeUTF("NO_FILE"); 
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	

	private void receiveExamFile(String substring, DataInputStream input) {
		try {
			String fileName = input.readUTF(); 
			long fileSize = input.readLong();
			String folderPath = Constant.FILE_LOCATION + File.separator + "Test" + File.separator + substring + File.separator;
	        File folder = new File(folderPath);
	        if (!folder.exists()) folder.mkdirs();
	        
	        // 3. Tạo file trên ổ cứng Server
	        File file = new File(folderPath + fileName);
	        try (FileOutputStream fos = new FileOutputStream(file)) {
	            byte[] buffer = new byte[4096];
	            long totalRead = 0; //Đã nhận được bao nhiêu byte rồi
	            int read; //Đọc được bao nhiêu byte
	            // Vòng lặp đọc byte từ mạng và ghi xuống file
	            //min(...): tức là nếu như gói dữ liệu cuối còn 4000 thì nó sẽ đọc 4000, nó sẽ không cố đọc 4096 làm hỏng file
	            while (totalRead < fileSize && (read = input.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
	                fos.write(buffer, 0, read); //ghi dữ liệu vào ổ cứng
	                totalRead += read;
	            }
	        }
	        System.out.println("Đã nhận đề thi");
		}catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
	
	// Hàm 2: Gửi file từ Server xuống cho Student
	private void sendExamFile(String testId, DataOutputStream output) {
	    try {
	        // Tìm file trong thư mục phòng thi
	        String folderPath = Constant.FILE_LOCATION + File.separator + "Test" + File.separator + testId + File.separator;
	        File folder = new File(folderPath);
	        
	        File[] files = folder.listFiles(); // Lấy file đầu tiên tìm thấy
	        if (files != null && files.length > 0) {
	            File file = files[0]; // Giả sử chỉ có 1 đề thi
	            
	            output.writeUTF("OK"); // Báo là có file
	            output.writeUTF(file.getName()); // Gửi tên file
	            output.writeLong(file.length()); // Gửi kích thước
	            
	            // Đọc file từ ổ cứng và bắn qua mạng
	            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
	                byte[] buffer = new byte[4096];
	                int read;
	                while ((read = fis.read(buffer)) != -1) {
	                    output.write(buffer, 0, read);
	                }
	            }
	            output.flush();
	        } else {
	            output.writeUTF("NO_FILE"); // Báo không có đề
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	private void handleKick(String msg) {
		try {
			int i = msg.indexOf(" ");
			int roomId = Integer.valueOf(msg.substring(1, i));
			int studentNum = Integer.valueOf(msg.substring(i + 1));
			
			Room room = Server.rooms.get(roomId);
			if (room != null) {
                ClientModel student = room.getStudentByNum(studentNum);
				if (student != null) {
                    student.addWarning("KICK");
                }
                
                room.getQuittedStudents().add(studentNum);
			}
		} catch (Exception e) { e.printStackTrace(); }
		
	}

	private void handlerWarning(String msg) {
		try {
			int i = msg.indexOf(" ");
			int j = msg.indexOf(" ", i + 1);
	        int roomId = Integer.valueOf(msg.substring(1, i));
	        int studentNum = Integer.valueOf(msg.substring(i + 1, j));
	        String text = msg.substring(j + 1);
	        Room room = Server.rooms.get(roomId);
	        if (room != null) {
	            room.getStudentByNum(studentNum).addWarning(text); 
	        }
	    } catch (Exception e) { e.printStackTrace(); }
	}

	private int createRoom(String msg) {
		//msg có dạng: "C"<udpPort> <user_id> <tencuocthi>
		int i = msg.indexOf(" "); 
		int udpPort = Integer.valueOf(msg.substring(1, i));
		int j = msg.indexOf(" ", i + 1);
		String user_id = msg.substring(i + 1, j);
		String tencuocthi = msg.substring(j + 1);
		int roomId = TestDAO.addTest(user_id, tencuocthi);
		Room room = new Room(address, udpPort, tencuocthi);
		Server.rooms.put(roomId, room);
		return roomId;
	}

	private void joinRoom(String msg, DataOutputStream output) throws Exception {
		int i = msg.indexOf(" ");
		int j = msg.indexOf(" ", i + 1);
		int roomId = Integer.valueOf(msg.substring(1, i));
		Room room = Server.rooms.get(roomId);
		if (room != null) {
			if(room.getIsLocked() == true) {
				output.writeUTF("LOCKED");
			} else {
				String name = msg.substring(j + 1);
				int studentId = ParticipantDAO.addParticipant(roomId, name);
				int studentNum = room.getNewStudentId().getAndIncrement();
				room.getStudentNums().put(address.toString() + msg.substring(i + 1, j), studentNum);
				room.getStudents().put(studentId, new ClientModel(studentNum));
				room.getForFocus().put(studentNum * 2, studentId * 2);
				room.getForFocus().put(studentNum * 2 + 1, studentId * 2 + 1);
				room.getNames().add(Map.entry(studentNum, msg.substring(j + 1)));
				output.writeUTF("Y" + studentId + " " + room.getTeachername());
			}
		} else
			output.writeUTF("N");
	}

	private void focus(String[] msges) {
		int studentNum = Integer.parseInt(msges[1]);
		Room room = Server.rooms.get(Integer.parseInt(msges[0].substring(1)));
		int id = room.getForFocus().get(studentNum);
		if (id == room.getFocusAddress())
			room.setFocusAddress(-1);
		else
			room.setFocusAddress(id);
	}

	private boolean texting(String msg) throws UnsupportedEncodingException {
		int i = msg.indexOf(" ");
		int roomId = Integer.valueOf(msg.substring(1, i));
		Room room = Server.rooms.get(roomId);
		if (room != null) {
			room.getChatHistory().add(msg.substring(i + 1));
			return true;
		}
		return false;
	}

	private void checkStudent(String message, DataOutputStream dos) throws Exception {
		int i = message.indexOf(" ");
		int j = message.indexOf(" ", i + 1);
		int roomId = Integer.valueOf(message.substring(1, i));
		Room room = Server.rooms.get(roomId);
		if (room != null) {
			int studentId = Integer.parseInt(message.substring(i + 1, j));
			int msgNum = Integer.parseInt(message.substring(j + 1));
			ClientModel student = room.getStudents().get(studentId);
		    student.setTime(System.currentTimeMillis());
		    //Cập nhật thời gian student liên lạc
		    
			if (studentId * 2 == room.getFocusAddress()) {
				dos.writeUTF("H1");
			} else if (studentId * 2 + 1 == room.getFocusAddress()) {
				dos.writeUTF("H2");
			} else
				dos.writeUTF("~H");
			ArrayList<String> chatHistory = room.getChatHistory();
			while (msgNum < chatHistory.size()) {
				dos.writeUTF("M" + chatHistory.get(msgNum));
				msgNum++;
			}
			Queue<String> warnings = student.getWarnings(); 
		    while (!warnings.isEmpty()) {
		        dos.writeUTF("ALERT:" + warnings.poll()); 
		    }
			dos.writeUTF("E");
		} else
			dos.writeUTF("Q");
	}

	private void checkTeacher(String msg, DataOutputStream dos) throws Exception {
		int i = msg.indexOf(" ");
		int roomId = Integer.valueOf(msg.substring(1, i));
		Room room = Server.rooms.get(roomId);
		if (room != null) {
			int msgNum = Integer.parseInt(msg.substring(i + 1));
			room.getTeacher().setTime(System.currentTimeMillis());

			while (!room.getNames().isEmpty()) {
				Map.Entry<Integer, String> entry = room.getNames().poll();
				dos.writeUTF("N" + entry.getKey() + " " + entry.getValue());
			}

			while (!room.getQuittedStudents().isEmpty()) {
				Integer quit = room.getQuittedStudents().poll();
				dos.writeUTF("D" + quit);
			}
			while(!room.getNewSubmissions().isEmpty()) {
				Integer subId = room.getNewSubmissions().poll();
				ClientModel student = room.getStudents().get(subId);
				if(student != null) {
					dos.writeUTF("F" + " " + subId + " " + student.getStudentNum());
				}
			}
			while(!room.getRaisedHands().isEmpty()) {
				Integer Num = room.getRaisedHands().poll();
				dos.writeUTF("HAND " + Num);
			}
			while(!room.getWarnings().isEmpty()) {
				String w = room.getWarnings().poll();
				dos.writeUTF(w);
				System.out.println("Ở cảnh báo giáo viên");
			}
			ArrayList<String> chatHistory = room.getChatHistory();
			while (msgNum < chatHistory.size()) {
				dos.writeUTF("M" + chatHistory.get(msgNum));
				msgNum++;
			}
			while (!room.getKeys().isEmpty()) {
				dos.writeUTF("K" + room.getKeys().poll());
			}
		}
		dos.writeUTF("E");
	}

	private void takeKeys(String msg) {
		int i = msg.indexOf(" ");
		int j = msg.indexOf(" ", i + 1);
		int roomId = Integer.valueOf(msg.substring(1, i));
		Room room = Server.rooms.get(roomId);
		if (room != null) {
			int id = Integer.valueOf(msg.substring(i + 1, j));
			int studentNum = room.getStudents().get(id).getStudentNum();
			String txt = msg.substring(j + 1);
			room.getKeys().add(studentNum + " " + txt);
			Service.saveKeyLog(id, txt);
		}
	}

	private void handleUpdateUserRequest(String msg) {
		String splitMsg[] = msg.split(",");
		UserDAO.updatePassword(splitMsg[1], splitMsg[2]);
	}

	private void handleLogin(String msg, DataOutputStream dos) throws IOException {
		String splitMsg[] = msg.split(",");
		String username = splitMsg[1];
		String password = splitMsg[2];
		User user = UserDAO.login(username, password);
		if (user != null) {
			dos.writeUTF("Y," + user.getId());
		} else {
			dos.writeUTF("N");
		}
	}

	private void handleRegister(String msg, DataOutputStream dos) throws IOException {
		String splitMsg[] = msg.split(",");
		String username = splitMsg[1];
		String password = splitMsg[2];
		int user_id = UserDAO.register(username, password);
		if (user_id != -1) {
			dos.writeUTF("Y," + user_id);
		} else {
			dos.writeUTF("N");
		}
	}

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private void handleListTestRequest(String msg, DataOutputStream dos) throws IOException {
		String[] splitMsg = msg.split(",");
		String user_id = splitMsg[1];
		List<Test> listData = TestDAO.listTests(user_id);
		StringBuilder sendMsg = new StringBuilder();
		/*StringBuilder: là 1 class kế thừa từ AbstractStringBuilder
		AbstractStringBuilder: có 1 thuộc tính là value (khong final)
		StringBuilder có phương thức append (str)=> cộng dồn str vào value ở class cha
		*/
		if (!listData.isEmpty()) {
			for (int i = 0; i < listData.size(); i++) {
				Test t = listData.get(i);
				sendMsg.append(t.getId()).append(",").append(t.getName()).append(",")
						.append(dateFormat.format(t.getCreate())).append(",");

				if (i != listData.size() - 1)
					sendMsg.append("|");
			}
			dos.writeUTF(sendMsg.toString());
		} else {
			dos.writeUTF("0");
		}
	}

	private void handleListParticipant(String msg, DataOutputStream dos) throws IOException {
		String[] splitMsg = msg.split(",");
		String test_id = splitMsg[1];
		List<Participant> listData = ParticipantDAO.listParticipants(test_id);
		StringBuilder sendMsg = new StringBuilder();
		if (!listData.isEmpty()) {
			for (int i = 0; i < listData.size(); i++) {
				Participant t = listData.get(i);
				sendMsg.append(t.getId()).append(",").append(t.getName()).append(",");
				if (i != listData.size() - 1)
					sendMsg.append("|");
			}
			dos.writeUTF(sendMsg.toString());
		} else {
			dos.writeUTF("0");
		}
	}

	private void getKeys(String participant_id, DataOutputStream dos) throws IOException {
		String filePath = Constant.FILE_LOCATION + File.separator + "Keyboard" + File.separator + participant_id
				+ ".txt";
		String s = Files.readString(Paths.get(filePath));
		dos.writeUTF(s);
	}

	private void endStream(String message) {
		Server.rooms.remove(Integer.valueOf(message.substring(1)));
	}
}