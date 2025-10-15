package Client.Controller.Teacher;
import java.util.Map;

import Client.Constant;
import Client.commom.DTO.InContest.Teacher.ImageModel;
import Client.commom.DTO.InContest.Teacher.Packet;

public class ViewThread extends Thread {
	public static TeacherController par;

	public ViewThread(TeacherController par) {
		ViewThread.par = par;
	}

	public void run() {
		while (par.running) {
			if(par.viewList.size() == 0) {
				try {
					Thread.sleep(0, 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				//Duyệt qua danh sách các ảnh chờ hiển thị
				for(int i = 0; i < par.viewList.size(); i++) {
					if(par.viewList.get(i) != null) {
						long start = System.nanoTime();
						ImageModel tmp = par.viewList.get(i);
						par.viewList.set(i, null);
						//Lắp rắp ảnh
						byte[] completeData = assemblePackets(tmp.getData());
						par.setImage(completeData, i);
						System.out.println(System.nanoTime() - start);
					}
				}
				try {
				Thread.sleep(1);
				} catch (InterruptedException e) {
					//TODO Auto-generated catch block
					e.printStackTrace();
			}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] assemblePackets(Map<Integer, Packet> packets) {
		int totalLength = 0, header = Constant.PACKET_SIZE - Constant.IMAGE_SEGMENT;
		for (Packet packet : packets.values()) {
			if (packet != null)
				// Tổng kích thước dữ liệu ảnh trong 1 gói tin, dựa vào vòng lặp for -> kích thước của cả ảnh
				totalLength += packet.getLength() - header;
		}
		byte[] completeData = new byte[totalLength];
		int currentPos = 0;
		for (int i = 0; i < packets.size(); i++) {
			Packet packet = packets.get(i);
			if (packet != null) {
				// System.arraycopy(Nguồn, Bắt đầu từ đâu ở Nguồn, Đích, Dán vào đâu ở Đích, Sao chép bao nhiêu);
				System.arraycopy(packet.getData(), header, completeData, currentPos, packet.getLength() - header);
				currentPos += packet.getLength() - header;
			}
		}
		return completeData;
	}
}