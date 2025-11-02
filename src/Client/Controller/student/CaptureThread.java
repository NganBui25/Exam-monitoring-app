package Client.Controller.student;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import Client.Constant;
import Client.commom.DTO.InContest.ScreenImageDTO;

public class CaptureThread extends Thread {
    private StudentController par;
    private boolean isScreen;
    
    private VideoCapture camera;
	private Mat frame;
	private static final int CAM_FPS = 10; 
	private static final long CAM_FRAME_PERIOD = 1000 / CAM_FPS;
    private static final int CAPTURE_FPS = 15;
    private static final long FRAME_PERIOD = 1000 / CAPTURE_FPS;

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
            return; // Thoát nếu không tạo được Robot
        }

        while (par.running) {
            long startTime = System.currentTimeMillis();
            
            try {
                // 1. Chụp ảnh màn hình (Full-res)
                BufferedImage fullImage = r.createScreenCapture(capture);

                par.screenQueue.add(fullImage); // <<< XÓA DÒNG NÀY

                // 3. VẼ VÀO imgModel (DÙNG LOCK)
                par.imgModelLock.lock(); // <-- LẤY KHÓA
                try {
                    ScreenImageDTO tmp = par.imgModel;
                    if (tmp != null && tmp.g2d != null) {
                        // Vẽ ảnh full-res, scale vào imgModel (preview)
                        tmp.g2d.drawImage(fullImage, 0, 0, tmp.img.getWidth(), tmp.img.getHeight(), null);
                    }
                } finally {
                    par.imgModelLock.unlock(); // <-- LUÔN NHẢ KHÓA
                }

                // 4. KIỂM SOÁT FPS (SỬA LỖI 100% CPU)
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = FRAME_PERIOD - elapsedTime;

                if (sleepTime > 0) {
                    Thread.sleep(sleepTime); // Ngủ để giữ FPS ổn định
                }

            } catch (InterruptedException e) {
                par.running = false; // Dừng luồng nếu bị ngắt
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void captureCam() {
		// Mở webcam
		camera = new VideoCapture(0); // 0 là webcam mặc định
		if (!camera.isOpened()) {
			System.err.println("Không thể mở webcam (OpenCV). Hãy chắc chắn driver đã được cài.");
			return; // Thoát nếu không mở được cam
		}
		
		frame = new Mat(); // 'Mat' là định dạng ảnh của OpenCV

		while (par.running) {
			long startTime = System.currentTimeMillis();
			try {
				// 1. Đọc 1 khung hình (Mat) từ webcam
				camera.read(frame);
				
				if (!frame.empty()) {
					// 2. "Cây cầu": Chuyển đổi Mat -> BufferedImage
					BufferedImage image = matToBufferedImage(frame);
					
					// 3. Thả ảnh vào "băng chuyền" Webcam
					par.camQueue.add(image);
				}
				
				// 4. Kiểm soát FPS (tránh 100% CPU)
				long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = CAM_FRAME_PERIOD - elapsedTime;
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		camera.release(); // 5. Giải phóng webcam khi luồng dừng
	}
	
	/**
	 * Hàm "Cây cầu" (BRIDGE): Chuyển đổi Mat (OpenCV) sang BufferedImage (Java AWT)
	 * Hàm này lấy dữ liệu byte thô từ 'Mat' và sao chép nó vào một 'BufferedImage' mới
	 */
	private BufferedImage matToBufferedImage(Mat mat) {
		// Xác định loại ảnh (thường là 3-byte BGR)
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (mat.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		
		// Lấy dữ liệu byte thô từ Mat
		int bufferSize = mat.channels() * mat.cols() * mat.rows();
		byte[] buffer = new byte[bufferSize];
		mat.get(0, 0, buffer); 
		
		// Tạo một BufferedImage với cùng kích thước và loại
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
		
		// Sao chép dữ liệu byte thô vào bộ đệm của BufferedImage
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
		
		return image;
	}
}