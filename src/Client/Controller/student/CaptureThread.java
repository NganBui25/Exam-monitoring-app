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
    
    // --- FPS MỚI CHO LUỒNG CHỤP ---
    // Chụp 15 khung hình/giây
    // Luồng này sẽ chạy NHANH HƠN Luồng Gửi (SendThread = 10 FPS)
    // để đảm bảo hình ảnh luôn "tươi mới" nhất.
    private static final int CAPTURE_FPS = 15;
    private static final long FRAME_PERIOD = 1000 / CAPTURE_FPS;

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
            return; // Thoát nếu không tạo được Robot
        }

        while (par.running) {
            long startTime = System.currentTimeMillis();
            
            try {
                // 1. Chụp ảnh màn hình (Full-res)
                BufferedImage fullImage = r.createScreenCapture(capture);

                // 2. LỖI MEMORY LEAK CŨ -> XÓA BỎ
                // par.screenQueue.add(fullImage); // <<< XÓA DÒNG NÀY

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
}