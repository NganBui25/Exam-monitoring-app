package Client.Controller.student; // (Dùng package của bạn)

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

// Thêm các import này
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import Client.Constant;

public class SendThread extends Thread {

	private StudentController par;
	private boolean isScreen; // Cờ (true=Screen, false=Cam)
	private byte[] tmp = new byte[Constant.PACKET_SIZE]; 
	private ByteArrayOutputStream baos; // Sẽ được khởi tạo trong constructor
	private ByteBuffer byteBuffer = ByteBuffer.allocate(2);
	
	// FPS cho luồng gửi (Thống nhất 10 FPS cho cả hai)
	private static final int SEND_FPS = 10;
	private static final long FRAME_PERIOD = 1000 / SEND_FPS;
	private static final int DELTA_THRESHOLD = 10; // Giảm xuống 10 (như đã thống nhất)

	public SendThread(StudentController par, boolean isScreen) {
		this.par = par;
		this.isScreen = isScreen; // Lưu cờ
		this.baos = new ByteArrayOutputStream(); // Khởi tạo baos cho mỗi luồng
		tmp[0] = Byte.valueOf(par.roomId); // Đặt RoomID (Byte 0) một lần
	}

	@Override
	public void run() {
		int currImg = 0; 
		while (par.running) {
			long startTime = System.currentTimeMillis();
			try {
				// === BƯỚC 3.1: LẤY ẢNH TỪ ĐÚNG HÀNG ĐỢI ===
				BufferedImage currentImage = getCurrentImageFromQueue();
				if (currentImage == null) {
					Thread.sleep(10); // Hàng đợi rỗng, ngủ 1 chút
					continue;
				}

				// === BƯỚC 3.2: DÙNG ĐÚNG "BỘ NHỚ" ===
				BufferedImage prevImage;
				long lastKeyTime;
				if (isScreen) {
					boolean mustSendKeyframe = (System.currentTimeMillis() - par.lastKeyframeTime > Constant.KEYFRAME_INTERVAL)
							|| (par.previousImage == null);

					if (mustSendKeyframe) {
						sendKeyframe(currentImage, currImg);
						par.lastKeyframeTime = System.currentTimeMillis();
					} else {
						List<Rectangle> changedTiles = findChangedTiles(par.previousImage, currentImage);
						// Nếu thay đổi quá nhiều
						if (changedTiles.size() > DELTA_THRESHOLD) {
							sendKeyframe(currentImage, currImg);
							par.lastKeyframeTime = System.currentTimeMillis();
						} else { // Nếu thay đổi ít
							sendDeltaTiles(currentImage, currImg, changedTiles);
						}
					}
					par.previousImage = currentImage;
				} else {
					sendKeyframe(currentImage, currImg);
					par.previousCamImage = currentImage;
				}
				

				// Số thứ tự 0-127 (vì dùng 1 bit cho streamType)
				currImg = (currImg + 1) % 128; 

				// === BƯỚC 3.5: KIỂM SOÁT FPS ===
				long elapsedTime = System.currentTimeMillis() - startTime;
				long sleepTime = FRAME_PERIOD - elapsedTime;
				if (sleepTime > 0) Thread.sleep(sleepTime);

			} catch (Exception ex) {
				ex.printStackTrace(); 
			}
		}
	}

	/**
	 * HÀM MỚI: Lấy ảnh từ đúng hàng đợi
	 */
	private BufferedImage getCurrentImageFromQueue() {
		// 1. Lấy ảnh (thô) từ hàng đợi
		BufferedImage img = isScreen ? par.screenQueue.poll() : par.camQueue.poll();
		if (img == null) return null;
		
		// 2. Tạo bản sao (copy) với định dạng RGB (Sửa lỗi ảnh đen)
		BufferedImage copy = new BufferedImage(
			img.getWidth(), 
			img.getHeight(), 
			BufferedImage.TYPE_INT_RGB
		);
		Graphics g = copy.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return copy;
	}
	private void sendKeyframe(BufferedImage image, int currImg) throws IOException, InterruptedException {
		baos.reset();
		ImageIO.write(image, "jpg", baos); 
		byte[] imageBytes = baos. toByteArray();
		int size = (imageBytes.length + Constant.IMAGE_SEGMENT - 1) / Constant.IMAGE_SEGMENT;
		int packetNum = 0;
		
		for (int i = 0; i < imageBytes.length; i += Constant.IMAGE_SEGMENT) {
			int length = Math.min(Constant.IMAGE_SEGMENT, imageBytes.length - i);

			tmp[Constant.OFFET_IMG_NUM] = (byte) (currImg * 2 + (isScreen ? 0 : 1));
			
			// (Header còn lại giữ nguyên)
			tmp[Constant.OFFSET_PACKET_TYPE] = Constant.PACKET_TYPE_KEYFRAME; 
			tmp[Constant.OFFSET_KEY_TOTAL] = (byte) size;
			tmp[Constant.OFFSET_KEY_NUM] = (byte) packetNum++;
			
			System.arraycopy(imageBytes, i, tmp, Constant.HEADER_SIZE, length);
			par.udpSocket.send(new DatagramPacket(tmp, length + Constant.HEADER_SIZE, Constant.serverAddress, Constant.udpPort));
			Thread.sleep(1);
		}
	}

	/**
	 * SỬA LẠI HEADER (Dán nhãn Byte 1)
	 */
	private void sendDeltaTiles(BufferedImage currImage, int currImg, List<Rectangle> tiles) throws IOException {
		for (Rectangle tileRect : tiles) {
			int x = tileRect.x;
			int y = tileRect.y;
			int w = tileRect.width;
			int h = tileRect.height;
			BufferedImage currTile = currImage.getSubimage(x, y, w, h);
			
			baos.reset();
			ImageIO.write(currTile, "jpg", baos);
			byte[] tileBytes = baos.toByteArray();
			if (tileBytes.length > Constant.IMAGE_SEGMENT) continue; // Bỏ qua nếu ô quá lớn

			// === DÁN NHÃN BYTE 1 (Logic "ghép" của bạn) ===
			// (Dùng tên hằng số của bạn)
			tmp[Constant.OFFET_IMG_NUM] = (byte) (currImg * 2 + (isScreen ? 0 : 1));

			// (Header còn lại giữ nguyên)
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
	/**
	 * HÀM MỚI (Từ Bước 7.6): Tìm và trả về danh sách các ô đã thay đổi
	 */
	private List<Rectangle> findChangedTiles(BufferedImage prevImage, BufferedImage currImage) {
		List<Rectangle> changedTiles = new ArrayList<>();
		int tileWidth = Constant.TITLE_WIDTH; // (Dùng tên hằng số của bạn)
		int tileHeight = Constant.TITLE_HEIGHT; // (Dùng tên hằng số của bạn)
	
		for (int y = 0; y < currImage.getHeight(); y += tileHeight) {
			for (int x = 0; x < currImage.getWidth(); x += tileWidth) {
				int w = Math.min(tileWidth, currImage.getWidth() - x);
				int h = Math.min(tileHeight, currImage.getHeight() - y);
	
				BufferedImage prevTile = prevImage.getSubimage(x, y, w, h);
				BufferedImage currTile = currImage.getSubimage(x, y, w, h);
	
				if (!areTilesSame(prevTile, currTile)) {
					changedTiles.add(new Rectangle(x, y, w, h));
				}
			}
		}
		return changedTiles;
	}

	private boolean areTilesSame(BufferedImage tile1, BufferedImage tile2) {
        // 1. Kiểm tra nhanh: Kích thước và Loại
        if (tile1.getWidth() != tile2.getWidth() || 
            tile1.getHeight() != tile2.getHeight() || 
            tile1.getType() != tile2.getType()) {
            return false;
        }

        try {
            // Lấy bộ đệm "thô" (không biết là Int hay Byte)
            DataBuffer db1 = tile1.getRaster().getDataBuffer();
            DataBuffer db2 = tile2.getRaster().getDataBuffer();
            
            if (db1 instanceof DataBufferInt) {
                int[] data1 = ((DataBufferInt) db1).getData();
                int[] data2 = ((DataBufferInt) db2).getData();
                return Arrays.equals(data1, data2);
            } 
            else if (db1 instanceof DataBufferByte) {
                byte[] data1 = ((DataBufferByte) db1).getData();
                byte[] data2 = ((DataBufferByte) db2).getData();
                return Arrays.equals(data1, data2);
            }
            
            // 4. "SLOW PATH" (DỰ PHÒNG): Nếu là loại ảnh khác
            for (int y = 0; y < tile1.getHeight(); y++) {
                for (int x = 0; x < tile1.getWidth(); x++) {
                    if (tile1.getRGB(x, y) != tile2.getRGB(x, y)) {
                        return false; 
                    }
                }
            }
            return true; 

        } catch (Exception e) {
            e.printStackTrace(); 
            // "SLOW PATH"
            for (int y = 0; y < tile1.getHeight(); y++) {
                for (int x = 0; x < tile1.getWidth(); x++) {
                    if (tile1.getRGB(x, y) != tile2.getRGB(x, y)) {
                        return false; 
                    }
                }
            }
            return true; 
        }
	}
}