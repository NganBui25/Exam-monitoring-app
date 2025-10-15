package Client.Controller.student;

import java.awt.AWTException;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

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
}