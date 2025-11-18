package src.Client.controller.student;

import java.awt.AWTException;
import org.opencv.core.Core;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import src.Client.Constant;
import src.Client.commom.DTO.InContest.ScreenImageDTO;

public class CaptureThread extends Thread {
	private StudentController par;
	private boolean isScreen; 
	private VideoCapture videoCapture;
	
	public CaptureThread(StudentController par, boolean isScreen) {
		this.par = par;
		this.isScreen = isScreen;
	}

	public void run() {
		if (isScreen)
			captureScreen();
		else
			captureCam();
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
			par.running = false;
			return;
		}
		while (par.running) {
			try { // --- BẮT ĐẦU KHỐI TRY ---
				
				ScreenImageDTO tmp = par.imgModel;
				BufferedImage fullImage = r.createScreenCapture(capture); // 1. Chụp
//				par.screenQueue.add(fullImage);
				tmp.g2d.drawImage(fullImage, 0, 0, tmp.img.getWidth(), tmp.img.getHeight(), null); // 2. Vẽ
				
				fullImage = null; 
				
				Thread.sleep(30);
				
			} catch (InterruptedException e) {
				par.running = false; 
				
			} catch (Throwable t) {
				
				System.err.println("LỖI NGHIÊM TRỌNG TRONG LUỒNG CAPTURE MÀN HÌNH: " + t.getMessage());
				
				if (t instanceof OutOfMemoryError) {
					System.gc();
				}
				
				try { 
					Thread.sleep(1000); 
				} catch (InterruptedException ie) {
					par.running = false;
				}
			}
		}
	}
	
	public void captureCam() {
		videoCapture = new VideoCapture(0 + Videoio.CAP_DSHOW); 
		
		if (!videoCapture.isOpened()) {
			System.err.println("!!! LỖI: Không thể mở camera");
			return; // Dừng luồng
		}
		
		Constant.camWidth = (int) videoCapture.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH);
		Constant.camHeight = (int) videoCapture.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT);
		
		System.out.println("!!! Đã khởi tạo camera (DSHOW index 0). Đang chờ đọc frame...");

		while(par.running) {
			try { 
				Mat frame = new Mat();
				videoCapture.read(frame); 
				
				if (!frame.empty()) {
					par.camQueue.add(frame); 
					System.out.println("CAM: Đọc frame thành công!");
				}
				
				Thread.sleep(30); 
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}