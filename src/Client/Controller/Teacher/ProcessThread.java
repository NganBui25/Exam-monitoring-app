package Client.Controller.Teacher;
import java.util.ArrayList;

import Client.Constant;
import Client.commom.DTO.InContest.Teacher.ImageModel;
import Client.commom.DTO.InContest.Teacher.Packet;

//Lấy các dữ liệu ảnh từ hàng đợi rồi sắp xếp nó lại thành ảnh hoàn chỉnh
public class ProcessThread extends Thread {
	public TeacherController par;
	private boolean isFirst;

	public ProcessThread(TeacherController par, boolean isFirst) {
		this.par = par;
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
			byte[] receiveData = packet.getData();
			
			int studentNum = receiveData[0] & 0xFF;
			byte packetType = receiveData[Constant.OFFSET_PACKET_TYPE];
			
			if(packetType == Constant.PACKET_TYPE_KEYFRAME) {
				int imageNum = receiveData[Constant.OFFET_IMG_NUM] & 0xFF;
                int total = receiveData[Constant.OFFSET_KEY_TOTAL] & 0xFF;
                int packetNum = receiveData[Constant.OFFSET_KEY_NUM] & 0xFF;
                
                ArrayList<ImageModel> studentImages = par.images.get(studentNum);
                if (studentImages == null) {
                    studentImages = new ArrayList<>();
                    // Giả sử logic MAX_SCREENS / MAX_CAMS cũ của bạn
                    int max_size = (studentNum % 2 == 0) ? Constant.MAX_SCREENS : Constant.MAX_CAMS;
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
                    image.setTime(System.currentTimeMillis()); // Cập nhật thời gian
                    image.getData().put(packetNum, packet);
                }
			}
			else if(packetType == Constant.PACKET_TYPE_DELTA) {
				par.deltaQueue.add(packet);
			}
			if(isFirst) {
				isFirst = false;
				new CheckThread(par).start();
				for(int i = 0; i < Constant.VIEW_THREADS; i++) {
					new ViewThread(par).start();
				}
			}
		}
	}
}