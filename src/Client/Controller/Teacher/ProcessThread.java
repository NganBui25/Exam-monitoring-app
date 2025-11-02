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
			try {
				Packet packet = par.packets.poll();
				if (packet == null) {
					Thread.sleep(0, 1000);
					continue;
				}
				byte[] receiveData = packet.getData();
				
				int originalStudentNum = receiveData[0] & 0xFF;
				byte packetType = receiveData[Constant.OFFSET_PACKET_TYPE];
				int packedImageNum = receiveData[Constant.OFFET_IMG_NUM] & 0xFF;
				int imageNum = packedImageNum / 2;   // Lấy số thứ tự khung hình (0-127)
				int streamType = packedImageNum % 2; // Lấy loại stream (0=Screen, 1=Cam)
				int finalSlotNum = originalStudentNum * 2 + streamType;
				
				if(packetType == Constant.PACKET_TYPE_KEYFRAME) {
	                int total = receiveData[Constant.OFFSET_KEY_TOTAL] & 0xFF;
	                int packetNum = receiveData[Constant.OFFSET_KEY_NUM] & 0xFF;
	                
	                ArrayList<ImageModel> studentImages = par.images.get(finalSlotNum);
	                if (studentImages == null) {
	                    studentImages = new ArrayList<>();
	                    while (studentImages.size() < 128) 
	                    	studentImages.add(null);
	                    
	                    ImageModel image = new ImageModel(total, System.currentTimeMillis());
	                    image.getData().put(packetNum, packet);
	                    studentImages.set(imageNum, image);
	                    par.images.put(finalSlotNum, studentImages);
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
					receiveData[0] = (byte) finalSlotNum;
					par.deltaQueue.add(packet);
				}
				if(isFirst) {
					isFirst = false;
					new CheckThread(par).start();
					for(int i = 0; i < Constant.VIEW_THREADS; i++) {
						new ViewThread(par).start();
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}