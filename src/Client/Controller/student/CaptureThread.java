package Client.Controller.student;

import java.awt.AWTException;
import org.opencv.core.Core;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import Client.Constant;
import Client.commom.DTO.InContest.ScreenImageDTO;

public class CaptureThread extends Thread {
	private StudentController par;
	private boolean isScreen; 
	
	public CaptureThread(StudentController par, boolean isScreen) {
		this.par = par;
		this.isScreen = isScreen;
	}

	public void run() {
		if (isScreen)
			captureScreen();
	}

	private void captureScreen() {
		Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		Constant.screenWidth = capture.width;
		Constant.screenHeight = capture.height;
		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		while (par.running) {
			ScreenImageDTO tmp = par.imgModel;
			BufferedImage fullImage = r.createScreenCapture(capture);
			par.screenQueue.add(fullImage);
			tmp.g2d.drawImage(fullImage, 0, 0, tmp.img.getWidth(), tmp.img.getHeight(), null);
		}
	}
	
	public void captureCam() {
		VideoCapture camera = new VideoCapture(0);
		Constant.camWidth = (int) camera.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH);
		Constant.camHeight = (int) camera.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT);
		par.camImg = new Mat();
		while(par.running) {
			Mat frame = new Mat();
			camera.read(frame);
			if(!frame.empty()) {
				par.camQueue.add(frame);
				par.frame = frame;
				System.out.println("CAM: Đọc frame thành công!");
			}
		}
		System.out.println("!!! Đã giải phóng camera."); // <-- THÊM DÒNG NÀY
		camera.release();
	}
}