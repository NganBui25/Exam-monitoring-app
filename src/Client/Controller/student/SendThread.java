package Client.Controller.student;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.DatagramPacket;
import javax.imageio.ImageIO;
import Client.Constant;

public class SendThread extends Thread {

	private StudentController par;
	private boolean isScreen;
	private byte[] tmp = new byte[Constant.PACKET_SIZE]; 
	private ByteArrayOutputStream baos;
	// Dùng để chuyển đổi short (vị trí x,y) thành 2 bytes
	private ByteBuffer byteBuffer = ByteBuffer.allocate(2);
	private static final int SEND_FPS = 10;
	private static final long FRAME_PERIOD = 1000 / SEND_FPS;
	private static final int DELTA_THRESHOLD = 0;
	
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
//		while (par.running) {
//			try {
//				byte[] image = compressScreen();
//				long start = System.nanoTime();
//				
//				//Tính toán số lượng gói tin cần thiết
//				// cần bao nhiêu gói tin để gửi hết image(mỗi gói tin chỉ chứa tối đa Constant.IMAGE_SEGMENT byte dữ liệu ảnh
//				int size = (image.length + Constant.IMAGE_SEGMENT - 1) / Constant.IMAGE_SEGMENT;
//				int j = header, packetNum = 0;
//				tmp[1] = (byte) size; //Tổng số package
//				tmp[2] = (byte) (currImg * Constant.MAX_CAMS + (isScreen ? 1 : 0)); 
//				for (int i = 1; i <= image.length; i++) {
//					tmp[j++] = image[i - 1];
//					if (i % Constant.IMAGE_SEGMENT == 0 || i == image.length) {
//						tmp[3] = (byte) packetNum++;
//						par.udpSocket.send(new DatagramPacket(tmp, j, Constant.serverAddress, Constant.udpPort));
//						j = header; //đặt j về đúng vị trí bắt đầu của vùng chứa ảnh trong tmp
//					}
//				}
//				currImg = (currImg + 1) % (isScreen ? Constant.MAX_SCREENS : Constant.MAX_CAMS);
//				System.out.println(System.nanoTime() - start);
//				Thread.sleep(20);
//			} catch (Exception ex) {
//			}
//		}
		while(par.running) {
			long start = System.currentTimeMillis();
			
			try {
				BufferedImage currentImage = getCurrentImageFromModel();
				if(currentImage == null) continue;
				
				boolean mustSendKeyFrame = (System.currentTimeMillis() - par.lastKeyframeTime > Constant.KEYFRAME_INTERVAL
											|| (par.previousImage == null));
				if(mustSendKeyFrame) {
					sendKeyframe(currentImage, currImg);
					par.lastKeyframeTime = System.currentTimeMillis();
					par.previousImage = currentImage;
				}else {
					List<Rectangle> changedTiles = findChangedTiles(par.previousImage, currentImage);
					if(changedTiles.size() > DELTA_THRESHOLD) {
						sendKeyframe(currentImage, currImg);
						par.lastKeyframeTime = System.currentTimeMillis();
					}else {
						sendDeltaTiles(currentImage, currImg, changedTiles);
					}
					sendDeltaFrames(par.previousImage, currentImage, currImg);
					par.previousImage = currentImage;
				}
				
				currImg = (currImg + 1) % 256;
				
				long eslapsedTime = System.currentTimeMillis() - start;
				long sleepTime = FRAME_PERIOD - eslapsedTime;
				if(sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			}catch(Exception e) {
				
			}
		}
	}

	private BufferedImage getCurrentImageFromModel() {
		par.imgModelLock.lock();
		try {
			if(par.imgModel == null || par.imgModel.img == null) return null;
			
			BufferedImage copy = new BufferedImage(
					par.imgModel.img.getWidth(),
					par.imgModel.img.getHeight(),
					par.imgModel.img.getType()
					);
			Graphics g = copy.getGraphics();
			g.drawImage(par.imgModel.img, 0, 0, null);
			g.dispose();
			return copy;
		}finally {
			par.imgModelLock.unlock();
		}
	}
	
	private void sendKeyframe(BufferedImage image, int currImg) throws IOException {
		baos.reset();
		ImageIO.write(image, "jpg", baos);
		byte[] imageBytes = baos.toByteArray();
		
		int size = (imageBytes.length + Constant.IMAGE_SEGMENT - 1) / Constant.IMAGE_SEGMENT;
		int packetNum = 0;
		
		for(int i = 0; i < imageBytes.length; i += Constant.IMAGE_SEGMENT) {
			int length = Math.min(Constant.IMAGE_SEGMENT, imageBytes.length - i);
			
			tmp[Constant.OFFET_IMG_NUM] = (byte) currImg;
			tmp[Constant.OFFSET_PACKET_TYPE] = Constant.PACKET_TYPE_KEYFRAME;
			tmp[Constant.OFFSET_KEY_TOTAL] = (byte) size;
			tmp[Constant.OFFSET_KEY_NUM] = (byte) packetNum++;
			
			System.arraycopy(imageBytes, i, tmp, Constant.HEADER_SIZE, length);
			
			par.udpSocket.send(new DatagramPacket(tmp, length + Constant.HEADER_SIZE, Constant.serverAddress, Constant.udpPort));
		}
	}
	/**
     * Helper 3: So sánh, nén và gửi DELTA-FRAMES (Các ô thay đổi)
     * --- ĐÃ SỬA LỖI XỬ LÝ MÉP MÀN HÌNH ---
     */
    private void sendDeltaFrames(BufferedImage prevImage, BufferedImage currImage, int currImg) throws IOException {
        int tileWidth = Constant.TITLE_WIDTH;
        int tileHeight = Constant.TITLE_HEIGHT;
        
        // Duyệt qua toàn bộ ảnh theo từng ô
        for (int y = 0; y < currImage.getHeight(); y += tileHeight) {
            for (int x = 0; x < currImage.getWidth(); x += tileWidth) {
                
                // === PHẦN SỬA LỖI (BƯỚC 7.5) ===
                // Tính toán kích thước thực tế của ô (để xử lý các mép)
                int w = Math.min(tileWidth, currImage.getWidth() - x);
                int h = Math.min(tileHeight, currImage.getHeight() - y);
                
                // Cắt 2 ô (cũ và mới) với kích thước (w, h) chính xác
                BufferedImage prevTile = prevImage.getSubimage(x, y, w, h);
                BufferedImage currTile = currImage.getSubimage(x, y, w, h);
                // ===================================

                // Nếu 2 ô không giống nhau
                if (!areTilesSame(prevTile, currTile)) {
                    // Nén Ô MỚI này thành JPEG
                    baos.reset();
                    ImageIO.write(currTile, "jpg", baos);
                    byte[] tileBytes = baos.toByteArray();

                    // --- ĐIỀN HEADER 9-BYTE (Loại Delta) ---
                    tmp[Constant.OFFET_IMG_NUM] = (byte) currImg; //Số thứ tự khung hình để biết ô thay đổi là của khung hình nào
                    tmp[Constant.OFFSET_PACKET_TYPE] = Constant.PACKET_TYPE_DELTA; // Gói tin này là 1 ô thay đổi
                    
                    // Ghi vị trí X, Y (dùng ByteBuffer)
                    byteBuffer.clear();
                    byteBuffer.putShort((short) x); // Chuyển số X (vd: 200) thành 2 bytes
                    System.arraycopy(byteBuffer.array(), 0, tmp, Constant.OFFSET_DELTA_X, 2);
                    
                    byteBuffer.clear();
                    byteBuffer.putShort((short) y); // Chuyển số Y (vd: 100) thành 2 bytes
                    System.arraycopy(byteBuffer.array(), 0, tmp, Constant.OFFSET_DELTA_Y, 2);
                    // ------------------------------------------

                    // Copy dữ liệu ảnh (của ô) vào bộ đệm
                    System.arraycopy(tileBytes, 0, tmp, Constant.HEADER_SIZE, tileBytes.length);

                    // Gửi gói tin
                    par.udpSocket.send(new DatagramPacket(tmp, tileBytes.length + Constant.HEADER_SIZE, Constant.serverAddress, Constant.udpPort));
                }
            }
        }
    }
	
	private boolean areTilesSame(BufferedImage tile1, BufferedImage tile2) {
		for(int y = 0; y < tile1.getHeight(); y++) {
			for(int x = 0; x < tile1.getWidth(); x++) {
				if(tile1.getRGB(x, y) != tile2.getRGB(x, y)) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * HÀM MỚI: Tìm và trả về danh sách các ô đã thay đổi
	 */
	private List<Rectangle> findChangedTiles(BufferedImage prevImage, BufferedImage currImage) {
	    List<Rectangle> changedTiles = new ArrayList<>();
	    // Dùng tên hằng số của bạn
	    int tileWidth = Constant.TITLE_WIDTH;
	    int tileHeight = Constant.TITLE_HEIGHT;

	    for (int y = 0; y < currImage.getHeight(); y += tileHeight) {
	        for (int x = 0; x < currImage.getWidth(); x += tileWidth) {
	            // Sửa lỗi mép màn hình
	            int w = Math.min(tileWidth, currImage.getWidth() - x);
	            int h = Math.min(tileHeight, currImage.getHeight() - y);

	            BufferedImage prevTile = prevImage.getSubimage(x, y, w, h);
	            BufferedImage currTile = currImage.getSubimage(x, y, w, h);

	            if (!areTilesSame(prevTile, currTile)) {
	                // Nếu ô này khác, thêm "tọa độ" của nó vào danh sách
	                changedTiles.add(new Rectangle(x, y, w, h));
	            }
	        }
	    }
	    return changedTiles; // Trả về danh sách các ô thay đổi
	}
	/**
	 * HÀM MỚI: Chỉ gửi các ô trong danh sách
	 */
	private void sendDeltaTiles(BufferedImage currImage, int currImg, List<Rectangle> tiles) throws IOException {
	    // Duyệt qua danh sách các ô cần gửi
	    for (Rectangle tileRect : tiles) {
	        int x = tileRect.x;
	        int y = tileRect.y;
	        int w = tileRect.width;
	        int h = tileRect.height;
	        
	        // Cắt ô cần gửi từ ảnh MỚI
	        BufferedImage currTile = currImage.getSubimage(x, y, w, h);
	        
	        baos.reset();
	        ImageIO.write(currTile, "jpg", baos);
	        byte[] tileBytes = baos.toByteArray();

	        // Kiểm tra kích thước (Sửa lỗi gói tin quá lớn)
	        if (tileBytes.length > Constant.IMAGE_SEGMENT) {
	            continue; // Bỏ qua ô này nếu nén ra quá lớn
	        }

	        // Gửi gói tin Delta (logic cũ)
	        // Dùng tên hằng số của bạn
	        tmp[Constant.OFFET_IMG_NUM] = (byte) currImg; 
	        tmp[Constant.OFFSET_PACKET_TYPE] = Constant.PACKET_TYPE_DELTA; 
	        
	        byteBuffer.clear();
	        byteBuffer.putShort((short) x); 
	        System.arraycopy(byteBuffer.array(), 0, tmp, Constant.OFFSET_DELTA_X, 2);
	        
	        byteBuffer.clear();
	        byteBuffer.putShort((short) y); 
	        System.arraycopy(byteBuffer.array(), 0, tmp, Constant.OFFSET_DELTA_Y, 2);
	        
	        System.arraycopy(tileBytes, 0, tmp, Constant.HEADER_SIZE, tileBytes.length);
	        par.udpSocket.send(new DatagramPacket(tmp, tileBytes.length + Constant.HEADER_SIZE, Constant.serverAddress, Constant.udpPort));
	    }
	}
}