package src.Client.controller.teacher;
import java.util.ArrayList;

import src.Client.Constant;
import src.Client.commom.DTO.InContest.Teacher.ImageModel;
import src.Client.commom.DTO.InContest.Teacher.Packet;

//Lấy các dữ liệu ảnh từ hàng đợi rồi sắp xếp nó lại thành ảnh hoàn chỉnh
public class ProcessThread extends Thread {
	public static TeacherController par;
	private boolean isFirst;

	public ProcessThread(TeacherController par, boolean isFirst) {
		ProcessThread.par = par;
		this.isFirst = isFirst;
	}

	public void run() {
		while (par.running) {
			Packet packet = par.packets.poll();
			if (packet == null) {
				try {
					Thread.sleep(0, 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			//byte[]: <studentid> <total: số mảnh> <imagenum(số thứ tự khung hình)-isScreen(loại luồng): chứa thông tin kép> <Số thứ tự mảnh>
			//Mã hóa: (imageNum*Max_cams) + isScreen
			//MAX_CAMS: số ô tối đa
			byte[] receiveData = packet.getData();
			int isScreen = receiveData[2] % Constant.MAX_CAMS;
			int studentNum = isScreen == 1 ? receiveData[0] * 2 : receiveData[0] * 2 + 1;
			int total = receiveData[1];
			int imageNum = receiveData[2] / Constant.MAX_CAMS; //Lấy ra số thứ tự khung hình
			int packetNum = receiveData[3];
			ArrayList<ImageModel> studentImages = par.images.get(studentNum); //Lấy dữ liệu ảnh theo id
			if (studentImages == null) {
				studentImages = new ArrayList<>();
				int max_size = isScreen == 1 ? Constant.MAX_SCREENS
						: Constant.MAX_CAMS;
				//Thêm các dữ liệu rỗng vào
				while (studentImages.size() < max_size)
					studentImages.add(null);
				ImageModel image = new ImageModel(total, System.currentTimeMillis());
				image.getData().put(packetNum, packet);
				studentImages.set(imageNum, image);
				par.images.put(studentNum, studentImages);
			} else {
				ImageModel image = studentImages.get(imageNum);
				if (image == null) {
					image = new ImageModel();
					image.setTotal(total);
					studentImages.set(imageNum, image);
				}
				image.setTime(System.currentTimeMillis());
				image.getData().put(packetNum, packet);
			}
			if (isFirst) {
				isFirst = false;
				new CheckThread(par).start();
				for (int i = 0; i < Constant.VIEW_THREADS; i++) {
					new ViewThread(par).start();
				}
			}
		}
	}
}