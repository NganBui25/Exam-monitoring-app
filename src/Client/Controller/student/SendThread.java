package Client.Controller.student;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import Client.Constant;

public class SendThread extends Thread {

	private StudentController par;
	private boolean isScreen;
	private byte[] tmp = new byte[Constant.PACKET_SIZE]; 
	private ByteArrayOutputStream baos;

	private Mat resizedFrame = new Mat();
	private MatOfByte mob = new MatOfByte();
	
	public SendThread(StudentController par, boolean isScreen) {
		this.par = par;
		this.isScreen = isScreen;
		tmp[0] = Byte.valueOf(par.roomId); //Byte đầu tiên mỗi gói tin là roomId
		if(isScreen) baos = new ByteArrayOutputStream();
	}
	public void run() {
		//Cấu trúc của 1 gói tin UDP
		// tmp[0]: roomID, tmp[1]: size - tổng số gói tin cần thiết để ghép thành 1 ảnh
		// tmp[2]: ID của khung hình
		// tmp[3]: packetNum - Số thứ tự của gói tin
		// Còn lại là dữ liệu của ảnh
		int currImg = 0;
		// kích thước của 1 gói tin - kích thước tối đa của phần dữ liệu ảnh trong 1 gói 
		int header = Constant.PACKET_SIZE - Constant.IMAGE_SEGMENT;
		while (par.running) {
			try {
				byte[] image = compress();
				long start = System.nanoTime();
				
				//Tính toán số lượng gói tin cần thiết
				// cần bao nhiêu gói tin để gửi hết image(mỗi gói tin chỉ chứa tối đa Constant.IMAGE_SEGMENT byte dữ liệu ảnh
				int size = (image.length + Constant.IMAGE_SEGMENT - 1) / Constant.IMAGE_SEGMENT;
				int j = header, packetNum = 0;
				tmp[1] = (byte) size; //Tổng số package
				tmp[2] = (byte) (currImg * Constant.MAX_CAMS + (isScreen ? 1 : 0)); 
				for (int i = 1; i <= image.length; i++) {
					tmp[j++] = image[i - 1];
					if (i % Constant.IMAGE_SEGMENT == 0 || i == image.length) {
						tmp[3] = (byte) packetNum++;
						par.udpSocket.send(new DatagramPacket(tmp, j, Constant.serverAddress, Constant.udpPort));
						j = header; //đặt j về đúng vị trí bắt đầu của vùng chứa ảnh trong tmp
					}
				}
				currImg = (currImg + 1) % (isScreen ? Constant.MAX_SCREENS : Constant.MAX_CAMS);
				System.out.println(System.nanoTime() - start);
				if(isScreen) {
	                System.out.println("Gửi SCREEN: " + (System.nanoTime() - start));
	            } else {
	                System.out.println("Gửi CAMERA: " + (System.nanoTime() - start));
	            }
				Thread.sleep(20);
			} catch (Exception ex) {
			}
		}
	}

	private byte[] compress() throws IOException {
		if(isScreen) {
			baos.reset(); //xóa sạch dữ liệu của lần nén trước
			//Lấy ảnh từ par.imgModel.img, nén ảnh theo định dạng jpg
			ImageIO.write(par.imgModel.img, "jpg", baos);
			return baos.toByteArray();
		}
		else {
			Imgproc.resize(par.frame, par.camImg, par.camDim);
			Mat matTemp = par.camImg;
			MatOfByte buffer = new MatOfByte();
			Imgcodecs.imencode(".jpg", matTemp, mob);
			return buffer.toArray();
		}
	}
}